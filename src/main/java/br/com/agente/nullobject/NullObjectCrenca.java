package br.com.agente.nullobject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import br.com.agente.Crenca;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.runtime.BDIFailureException;

public class NullObjectCrenca extends Crenca implements java.io.Serializable {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NullObjectCrenca() {
		this.setMetodosAplicaveis(null);
	}

	/**
	 * verifica se o método é um set para variáveis declarados na classe
	 */
	public boolean isSetter(MethodDeclaration m, FieldDeclaration f) {
		if (m.getParameters().size() >= 1 && !m.isPrivate()) {
			List<?> variaveisDeclaradas = m.getBody().get().getChildNodes();
			for (Object s : variaveisDeclaradas) {
				if (s instanceof ExpressionStmt) {
					ExpressionStmt variavelDeclarada = (ExpressionStmt) s;
					String nomeVariavel = variavelDeclarada.getExpression().getChildNodes().get(1).toString();
					if (nomeVariavel.equals(f.getVariable(0).getName().toString())) {
						return m.getParameters().stream().filter(l -> l.getName().toString().equals(nomeVariavel))
								.collect(Collectors.toList()).size() > 0;
					}
				}
			}
		}
		return false;
	}

	/**
	 * busca algum if no método diferente de nulo e dentro dele deve haver uma
	 * invocação de método 1 - != NULL na comparação 2 - não tem else 3 - tem a
	 * chama de algum método dentro do if
	 */
	public boolean isGFIv1Conditional(FieldDeclaration f, Statement s) {
		boolean res = false;
		if (s instanceof IfStmt) {
			IfStmt condicao = (IfStmt) s;
			res = this.checksNullInequality(condicao.getCondition(), f) && !condicao.getElseStmt().isPresent()
					&& this.isFieldInvocationFragment(condicao.getThenStmt().getChildNodes(), f);
		}
		return res;
	}

	/**
	 * procura condicionais que tenha: 1 - != NULL na comparação 2 - else com corpo
	 * e throw dentro deste else
	 */
	public boolean isGFIv2Conditional(FieldDeclaration f, Statement s) {
		boolean res = false;
		if (s instanceof IfStmt) {
			IfStmt condicao = (IfStmt) s;
			boolean temThrowNoElse = condicao.getElseStmt().isPresent() && condicao.getElseStmt().get().getChildNodes()
					.stream().filter(l -> l instanceof ThrowStmt).collect(Collectors.toList()).size() > 0;
			res = this.checksNullInequality(condicao.getCondition(), f)
					&& this.isFieldInvocationFragment(condicao.getThenStmt().getChildNodes(), f) && temThrowNoElse;
		}
		return res;
	}

	/**
	 * procura condicionais que tenha: 1 - == NULL na comparação 2 - não tem else 3
	 * - dentro do if tem throw
	 */
	public boolean isGFIv3Conditional(FieldDeclaration f, Statement s) {
		boolean res = false;
		if (s instanceof IfStmt) {
			IfStmt condicao = (IfStmt) s;

			boolean temThrowNoIf = themThrowNoIf(condicao);
			res = this.checksNullEquality(condicao.getCondition(), f) && temThrowNoIf
					&& !condicao.getElseStmt().isPresent();
		}
		return res;
	}

	/**
	 * procura condicionais que tenha: 1 - == NULL na comparação 2 - tem else e no
	 * corpo deste tem alguma chamada de método
	 */
	public boolean isGFIv4Conditional(FieldDeclaration f, Statement s) {
		boolean res = false;
		if (s instanceof IfStmt) {
			IfStmt condicao = (IfStmt) s;
			boolean temThrowNoIf = themThrowNoIf(condicao);
			res = this.checksNullEquality(condicao.getCondition(), f) && temThrowNoIf
					&& condicao.getElseStmt().isPresent()
					&& this.isFieldInvocationFragment(condicao.getElseStmt().get().getChildNodes(), f);
		}
		return res;
	}

	public boolean themThrowNoIf(IfStmt condicao) {
		return condicao.getThenStmt().getChildNodes().stream().filter(l -> l instanceof ThrowStmt)
				.collect(Collectors.toList()).size() > 0;
	}

	public boolean isFieldInvocationFragment(List<Node> s, FieldDeclaration f) {
		boolean res = false;
		for (Node statement : s) {
			if (statement instanceof ExpressionStmt) {
				ExpressionStmt expressao = (ExpressionStmt) statement;
				if (expressao.getExpression() instanceof MethodCallExpr && this.isFieldInvocation(expressao, f)) {
					return true;
				}
				if (expressao.getExpression() instanceof AssignExpr) {
					return true;
				}
			}

		}
		return res;
	}

	/**
	 * verifica senão chama algum método de outro objeto analisando a expressão para
	 * ter objeto.metodo()
	 */
	public boolean isFieldInvocation(ExpressionStmt e, FieldDeclaration f) {
		boolean res = false;
		String nomeCampo = f.getVariable(0).getName().toString();
		res = e.getExpression() instanceof MethodCallExpr && e.getExpression().toString().contains(nomeCampo);
		return res;
	}

	/**
	 * verifica se em um metodo de nome x se tem um objeto dentro dele chamando
	 * método x tal como objeto.x()
	 */
	public boolean invokesFieldsMethod(ExpressionStmt e, FieldDeclaration f, MethodDeclaration m) {
		String nomeMetodo = m.getNameAsString();
		String metodoChamado = e.getExpression().getChildNodes().get(1).toString();
		return this.isFieldInvocation(e, f) && metodoChamado.equals(nomeMetodo);
	}

	/**
	 * verifica se o tipo passado é void
	 * 
	 * @return true - caso tipo passado seja void
	 */
	public boolean isVoidType(Type type) {
		return type != null && type.toString().equals("void");
	}

	/**
	 * verifica se o tipo retornado é string
	 */
	public boolean isLiteralType(Type type) {
		return type instanceof PrimitiveType && !type.toString().equals("void") || !type.toString().equals("String");
	}

	/**
	 * verifica senão tem nenhuma comparação com nulo nos condicionais do método
	 */
	public boolean checksNullEquality(Expression e, FieldDeclaration f) {
		boolean res = false;
		String nomeCampo = f.getVariable(0).getName().toString();
		if (e instanceof BinaryExpr) {
			BinaryExpr ex = (BinaryExpr) e;
			res = ex.getOperator().asString().equals("==") && ((ex.getRight() instanceof NullLiteralExpr
					&& ex.getLeft() instanceof Expression && ex.getLeft().toString().equals(nomeCampo))
					|| (ex.getLeft() instanceof NullLiteralExpr && ex.getRight() instanceof Expression
							&& ex.getRight().toString().equals(nomeCampo)));
		}
		return res;
	}

	/**
	 * verifica se a comparação tem diferente de nulo
	 */
	public boolean checksNullInequality(Expression e, FieldDeclaration f) {
		boolean res = false;
		String nomeCampo = f.getVariable(0).getName().toString();
		if (e instanceof BinaryExpr) {
			BinaryExpr ex = (BinaryExpr) e;
			res = ex.getOperator().asString().equals("!=") && ((ex.getRight() instanceof NullLiteralExpr
					&& ex.getLeft() instanceof Expression && ex.getLeft().toString().equals(nomeCampo))
					|| (ex.getLeft() instanceof NullLiteralExpr && ex.getRight() instanceof Expression
							&& ex.getRight().toString().equals(nomeCampo)));
		}
		return res;
	}

	public boolean emptyOnNull(FieldDeclaration f, MethodDeclaration m) {
		boolean res = false;
		if (isVoidType(f.getVariable(0).getType())) {
			res = true;
		}
		return res;
	}

	public boolean isOptional(FieldDeclaration f, String nomeClasse) {
		VariableDeclarator v = f.getVariable(0);
		String nomeCampo = v.getNameAsString();
		boolean setadoNoConstrutor = false;

		/** verifica se o campo da classe está setado ou inicializado no construtor */
		int qtdConstrutor = getExtrator().getClasseOrigem().getMembers().stream()
				.filter(l -> l instanceof ConstructorDeclaration).collect(Collectors.toList()).size();
		if (qtdConstrutor > 0) {
			ConstructorDeclaration construtor = (ConstructorDeclaration) getExtrator().getClasseOrigem().getMembers()
					.stream().filter(l -> l instanceof ConstructorDeclaration).collect(Collectors.toList()).get(0);
			BlockStmt corpoConstrutor = construtor.getBody();
			for (Statement linha : corpoConstrutor.getStatements()) {
				if ((linha.toString().contains(nomeCampo) && linha.toString().contains("="))) {
					setadoNoConstrutor = true;
					break;
				} else if (linha instanceof ExpressionStmt) {
					ExpressionStmt es = (ExpressionStmt) linha;
					if (es.getExpression() instanceof MethodCallExpr) {
						MethodCallExpr m = (MethodCallExpr) es.getExpression();
						List<MethodDeclaration> elementosMetodo = getExtrator().getClasseOrigem()
								.getMethodsByName(m.getName().toString());
						// verifica se tem algum método com aquele nome
						if (!elementosMetodo.isEmpty()) {
							MethodDeclaration md = elementosMetodo.get(0);
							if (isSetter(md, f)) {
								setadoNoConstrutor = true;
								break;
							}
						}
					}
				}
			}
		}

		boolean ehString = v.getType().toString().equals("String");
		boolean estaInicializadoNulo = !v.getInitializer().isPresent()
				|| v.getInitializer().get().toString().equals("null");
		boolean resFinal = ehString == false && estaInicializadoNulo && setadoNoConstrutor;
		return resFinal;
	}

	/**
	 * pré condições para verificar os campos nos ifs
	 */
	public boolean violatesOptionalFieldPreconditions(FieldDeclaration f) {
		try {
			String nomeClasse = f.getVariable(0).getType().toString();
			String nomeArquivo = nomeClasse + ".java";
			File arquivo = new File(this.getDiretorio() + "\\" + nomeArquivo);

			if (arquivo.exists()) {
				CompilationUnit cu = JavaParser.parse(arquivo);
				if (cu.getType(0) instanceof ClassOrInterfaceDeclaration) {
					ClassOrInterfaceDeclaration classe = (ClassOrInterfaceDeclaration) cu.getType(0);

					boolean naoTemExtensao = classe.getExtendedTypes().isEmpty();
					boolean naoEhInterface = !classe.isInterface();

					return !naoTemExtensao || !naoEhInterface || !f.isPrivate() || classe.isAbstract();
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			throw new IllegalStateException("Erro - causado por: " + ex.getMessage());
		}
	}

	/**
	 * verifica se o campo está em algum if do método e com comparação nula ou
	 * diferente de nulo
	 */
	public boolean isGuardedInvocation(FieldDeclaration f, MethodDeclaration m) {
		boolean res = false;
		if (f != null && m != null) {
			List<Node> linhas = m.getChildNodes();
			for (Node node : linhas) {
				if (node instanceof IfStmt) {
					IfStmt condicao = (IfStmt) node;
					if (checksNullEquality(condicao.getCondition(), f)
							|| checksNullInequality(condicao.getCondition(), f)) {
						return true;
					}
				}
			}
		}
		return res;
	}

	@Belief
	public Map<MethodDeclaration, List<Statement>> mapaMetodos() {
		boolean isGFI = false;
		System.out.println("== Testando se é aplicável Null Object => " + this.getNomeClasse());
		Map<MethodDeclaration, List<Statement>> mapaMetodosAnalisados = new HashMap<>();
		try {
			List<FieldDeclaration> camposClasse = this.getExtrator().getClasseOrigem().getFields();
			for (FieldDeclaration campo : camposClasse) {
				boolean ehOptional = isOptional(campo, this.getNomeClasse());
				boolean violaPreCondicoes = violatesOptionalFieldPreconditions(campo);
				if (ehOptional == false || violaPreCondicoes == true) {
					continue;
				}

				List<MethodDeclaration> metodos = this.getExtrator().getClasseOrigem().getMethods();
				if (!metodos.isEmpty()) {
					for (MethodDeclaration metodo : metodos) {
						/** para cada método um novo conjunto de ifs possíveis. */
						List<Statement> instrucoesIf = new ArrayList<>();
						if (metodo.getBody() != null && metodo.getBody().isPresent()
								&& !metodo.getBody().get().isEmpty()) {
							List<Statement> linhasMetodo = metodo.getBody().get().getStatements();
							if (linhasMetodo != null && !linhasMetodo.isEmpty()) {
								for (Statement linha : linhasMetodo) {
									if (linha instanceof IfStmt) {
										isGFI = false;

										IfStmt condicional = ((IfStmt) linha);
										if (isGFIv1Conditional(campo, condicional)) {
											isGFI = true;
											instrucoesIf.add(condicional);
										} else if (isGFIv2Conditional(campo, condicional)) {
											isGFI = true;
											instrucoesIf.add(condicional);
										} else if (isGFIv3Conditional(campo, condicional)) {
											isGFI = true;
											instrucoesIf.add(condicional);
										} else if (isGFIv4Conditional(campo, condicional)) {
											isGFI = true;
											instrucoesIf.add(condicional);
										}
									}
								}
								if (instrucoesIf != null && !instrucoesIf.isEmpty()) {
									System.out.println("Método: " + metodo.getNameAsString());
									mapaMetodosAnalisados.put(metodo, instrucoesIf);
								}
							}
						}
					}
				}
			}

			this.setMetodosAplicaveis(mapaMetodosAnalisados);
			return mapaMetodosAnalisados;
		} catch (BDIFailureException ex) {
			throw new IllegalStateException("Erro - causado por: " + ex.getMessage());
		}
	}

}
