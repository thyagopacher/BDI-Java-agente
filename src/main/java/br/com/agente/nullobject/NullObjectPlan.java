package br.com.agente.nullobject;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

import br.com.agente.Extrator;
import br.com.agente.ModificadorPlan;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanAborted;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanFailed;
import jadex.bdiv3.annotation.PlanPassed;
import jadex.bdiv3.runtime.IPlan;

/**
 * não funciona sem Plan no funcional do nome da classe possivelmente a intenção
 * final
 */

@Plan
public class NullObjectPlan extends ModificadorPlan implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	EnumSet<Modifier> publicoAbstrato = EnumSet.of(Modifier.PUBLIC, Modifier.ABSTRACT);
	EnumSet<Modifier> publico = EnumSet.of(Modifier.PUBLIC);

	/**
	 * extrator usado para pegar dados do objeto que foi encontrado em comparações
	 * de nulo
	 */
	private Extrator extratorObjeto;

	private FieldDeclaration campoClasse;

	@PlanAPI
	protected IPlan plan;

	public NullObjectPlan() {
		System.out.println("\nPlan instanciado com Pattern Null Object");
	}

	@PlanBody
	public boolean modificador() {
		String nomePacote = getCrenca().getPacote().getNameAsString();
		boolean arquivoAbstratoExiste = false, arquivoNullExiste = false;
		VariableDeclarator variavel = null;
		String nomeClasse = "", nomeObjeto = "", nomeClasseNullObjeto = "", nomeClasseNaoEncontrada = "",
				nomeCampo = "", nomeMetodoAssignTo = "";

		/** métodos da clase visualizada */
		NodeList<BodyDeclaration<?>> membersClasseLida = getCrenca().getExtrator().getCu().getType(0).getMembers();
		getCrenca().getExtrator().getClasseOrigem().setBlockComment("Classe modificada para ter padrão NullObject");
		Map<MethodDeclaration, List<Statement>> ondeModificar = getCrenca().getMapaMetodos();
		for (Map.Entry<MethodDeclaration, List<Statement>> entrada : ondeModificar.entrySet()) {
			MethodDeclaration metodoModificado = entrada.getKey();
			List<Statement> condicionaisEncontrados = entrada.getValue();

			IfStmt ifStmt1 = (IfStmt) condicionaisEncontrados.get(0);
			String campoIf = ifStmt1.getCondition().getChildNodes().get(0).toString();
			campoClasse = getCrenca().getExtrator().getCu().getTypes().get(0).getFields().stream()
					.filter(l -> l.getVariable(0).getNameAsString().equals(campoIf)).findAny().orElse(null);
			
			if (campoClasse != null) {
				variavel = campoClasse.getVariable(0);
				nomeObjeto = variavel.getType().toString();
				nomeCampo = variavel.getNameAsString().toString();
				nomeClasse = "Abstract" + nomeObjeto;
				break;
			}
		}

		try {
			/** Leitura da classe objeto que foi encontrada com comparação nula */
			extratorObjeto = new Extrator(getCrenca().getDiretorio() + "\\" + nomeObjeto + ".java");
			NodeList<BodyDeclaration<?>> metodosObjeto = extratorObjeto.getClasseOrigem().getMembers();

			nomeClasseNaoEncontrada = nomeObjeto + "NotFoundException";
			nomeClasseNullObjeto = "Null" + nomeObjeto;

			/**
			 * verifica e adiciona caso não tenha a classe para ser usada no throw "não
			 * encontrado"
			 */
			File classeNaoEncontrada = new File(getCrenca().getDiretorio() + "\\" + nomeClasseNaoEncontrada);
			if (!classeNaoEncontrada.exists()) {
				CompilationUnit cu = new CompilationUnit();
				cu.addImport("javassist.NotFoundException");
				cu.setPackageDeclaration(getCrenca().getPacote());
				ClassOrInterfaceDeclaration type = cu.addClass(nomeClasseNaoEncontrada);
				type.addExtends("NotFoundException");
				type.setBlockComment("Classe criada para retornar excessão quando não for encontrado o objeto lido");
				BlockStmt corpoConstrutor = new BlockStmt();
				corpoConstrutor.addAndGetStatement("super(msg)");

				ConstructorDeclaration construtor = new ConstructorDeclaration(EnumSet.of(Modifier.PUBLIC),
						nomeClasseNaoEncontrada);
				construtor.setBody(corpoConstrutor);
				construtor.addParameter("String", "msg");
				type.addMember(construtor);
				this.gravarConteudo(getCrenca().getDiretorio() + "/" + nomeClasseNaoEncontrada + ".java",
						cu.toString());
			}

			arquivoAbstratoExiste = new File(getCrenca().getDiretorio() + "\\" + nomeClasse).exists();
			arquivoNullExiste = new File(getCrenca().getDiretorio() + "\\" + nomeClasseNullObjeto).exists();

			if (!arquivoAbstratoExiste || !arquivoNullExiste) {
				CompilationUnit cu1 = null, cu2 = null;
				ClassOrInterfaceDeclaration type1 = null, type2 = null;

				if (!arquivoAbstratoExiste) {
					cu1 = new CompilationUnit();
					cu1.setPackageDeclaration(getCrenca().getPacote());
					/** importações especiais que não existem na classe objeto */
					cu1.addImport(nomePacote + "." + nomeClasseNaoEncontrada);
					cu1.addImport(nomePacote + "." + nomeObjeto);

					/** verifica quais são as importações da classe objeto e pega todas */
					adicionarImports(nomeClasse, cu1);

					cu1.setBlockComment("Classe abstrata para uso na classe NullObjeto e Objeto");
					type1 = cu1.addClass(nomeClasse);

				}

				if (!arquivoNullExiste) {
					cu2 = new CompilationUnit();
					cu2.setPackageDeclaration(getCrenca().getPacote());
					/** importações especiais que não existem na classe objeto */
					cu2.addImport(nomePacote + "." + nomeClasse);
					cu2.addImport(nomePacote + "." + nomeObjeto);

					/** verifica quais são as importações da classe objeto e pega todas */
					adicionarImports(nomeClasseNullObjeto, cu2);

					cu2.setBlockComment(
							"Classe NullObjeto para referenciar o objeto criando um novo ao invés de setar nulo direto");
					type2 = cu2.addClass(nomeClasseNullObjeto);

					type2.addExtends(nomeClasse);
				}

				for (BodyDeclaration<?> member : metodosObjeto) {
					if (member instanceof MethodDeclaration) {
						MethodDeclaration method0 = (MethodDeclaration) member;// metodo original
						EnumSet<Modifier> modificadorMetodo = (method0.getModifiers());

						if (!arquivoAbstratoExiste) {
							MethodDeclaration methodAbstrato = new MethodDeclaration(modificadorMetodo.clone(), method0.getType(), method0.getNameAsString());
							methodAbstrato.addModifier(Modifier.ABSTRACT);
							methodAbstrato.setBody(null);// deixa o corpo do metodo vazio
							type1.addMember(methodAbstrato);
						}
						if (!arquivoNullExiste) {
							MethodDeclaration methodNull = new MethodDeclaration(modificadorMetodo, method0.getType(), method0.getNameAsString());
							BlockStmt body = new BlockStmt();
							if (!method0.getType().toString().equals("void")) {
								if (method0.getType().toString().equals("double")
										|| method0.getType().toString().equals("int")
										|| method0.getType().toString().equals("float")) {
									body.addStatement("return 0;");
								} else if (method0.getType().toString().equals("boolean")) {
									body.addStatement("return true;");
								} else {
									body.addStatement("return null;");
								}
							} else {
								body.addStatement("throw new " + nomeClasseNaoEncontrada + "();");
							}
							methodNull.setBody(body);
							type2.addMember(methodNull);

						}
					}
				}

				if (!arquivoAbstratoExiste) {
					this.adicionarMetodo("isNull", "boolean", type1, "", publicoAbstrato, null);
					this.adicionarMetodo("getReference", nomeObjeto, type1, "", publicoAbstrato, null);
					this.adicionarMetodo("assertNotNull", "void", type1, "", publicoAbstrato,
							NodeList.nodeList(new ClassOrInterfaceType(nomeClasseNaoEncontrada)));

					this.gravarConteudo(getCrenca().getDiretorio() + "/" + nomeClasse + ".java", cu1.toString());
				}

				if (!arquivoNullExiste) {
					this.adicionarMetodo("isNull", "boolean", type2, "return true;");
					this.adicionarMetodo("getReference", nomeObjeto, type2, "return null;");
					this.adicionarMetodo("assertNotNull", "void", type2, "throw new " + nomeClasseNaoEncontrada + "();",
							publico, NodeList.nodeList(new ClassOrInterfaceType(nomeClasseNaoEncontrada)));

					this.gravarConteudo(getCrenca().getDiretorio() + "/" + nomeClasseNullObjeto + ".java",
							cu2.toString());
				}

				/** métodos especiais sendo atribuidos a classe objeto null */
				this.adicionarMetodo("isNull", "boolean", extratorObjeto.getClasseOrigem(), "return false;");
				this.adicionarMetodo("getReference", nomeObjeto, extratorObjeto.getClasseOrigem(), "return this;");
				this.adicionarMetodo("assertNotNull", "void", extratorObjeto.getClasseOrigem(), "");
				
				/**adiciona a extensão da classe Abstrata a classe Objeto*/
				extratorObjeto.getClasseOrigem().addExtends(nomeClasse);
				
				/**
				 * grava classe objeto com 3 novos métodos exigidos e a herança a classe
				 * abstrata
				 */
				this.gravarConteudo(getCrenca().getDiretorio() + "/" + nomeObjeto + ".java",
						extratorObjeto.getCu().toString());

			} /* fim da verificação se existe arquivo NullObjeto e AbstratoObjeto java */

			/** modificando o campo da classe que tem condicionais NULL */
			variavel.setType(nomeClasse);
			variavel.setInitializer("new " + nomeClasseNullObjeto + "()");

			BlockStmt corpoMetodoEspecialClasseCondicionais = new BlockStmt();
			corpoMetodoEspecialClasseCondicionais
					.addStatement("if(" + nomeCampo + " == null){return " + nomeClasseNullObjeto + "();}");
			corpoMetodoEspecialClasseCondicionais.addAndGetStatement("else return " + nomeCampo);
			nomeMetodoAssignTo = "assignTo" + nomeCampo.substring(0, 1).toUpperCase() + nomeCampo.substring(1);
			MethodDeclaration metodoEspecialClasseCondicionais = new MethodDeclaration(EnumSet.of(Modifier.PRIVATE),
					new ClassOrInterfaceType(nomeClasse), nomeMetodoAssignTo);
			metodoEspecialClasseCondicionais.setBody(corpoMetodoEspecialClasseCondicionais);
			getCrenca().getExtrator().getClasseOrigem().addMember(metodoEspecialClasseCondicionais);

			/** verificando se tem inicialização no construtor e modificando */
			String nomeParametroConstrutor = "";
			ConstructorDeclaration construtor = (ConstructorDeclaration) getCrenca().getExtrator().getClasseOrigem()
					.getMembers().stream().filter(l -> l instanceof ConstructorDeclaration).collect(Collectors.toList())
					.get(0);
			if (construtor.getParameters().size() > 0) {
				for (Parameter parametro : construtor.getParameters()) {
					if (parametro.getType().toString().equals(nomeObjeto)) {
						nomeParametroConstrutor = parametro.getNameAsString();
						break;
					}
				}

				BlockStmt corpoConstrutor = construtor.getBody();
				for (Statement linha : corpoConstrutor.getStatements()) {
					if (linha.toString().contains(nomeCampo) && linha.toString().contains("=")) {
						linha.remove();
						corpoConstrutor.addStatement(
								nomeCampo + " = " + nomeMetodoAssignTo + "(" + nomeParametroConstrutor + ");");
						break;
					}
				}
			}

			/**
			 * verifica método a método e todos os condicionais perante a classe que tem
			 * NULL
			 */
			boolean finalizaLacoFora = false;
			List<MethodDeclaration> metodosClasseLida = getCrenca().getExtrator().getClasseOrigem().getMethods();
			for (MethodDeclaration metodo : metodosClasseLida) {
				BlockStmt corpoMetodo = metodo.getBody().get();
				for (Statement linha : corpoMetodo.getStatements()) {
					if (linha.toString().contains(nomeCampo) && linha.toString().contains("=")
							&& getCrenca().isSetter(metodo, campoClasse)) {
						linha.remove();// subtitui o método set
						corpoMetodo.addStatement("this." + nomeCampo + " = " + nomeMetodoAssignTo + "("
								+ nomeParametroConstrutor + ");");
						break;
					} else if (linha.toString().contains(nomeCampo) && linha.toString().contains("return")) {
						linha.remove();// substitui o método get
						corpoMetodo.addStatement("return " + nomeCampo + ".getReference();");
						break;
					} else if (linha instanceof IfStmt) {
						finalizaLacoFora = true;
						break;// estes serão vistos no prox laço pois ele também vai ter os condicionais
					}
				}
				if(finalizaLacoFora) {
					break;
				}
			}

			for (Map.Entry<MethodDeclaration, List<Statement>> entrada : ondeModificar.entrySet()) {
				MethodDeclaration metodoModificado = entrada.getKey();
				BlockStmt corpoMetodo = metodoModificado.getBody().get();
				List<Statement> condicionaisEncontrados = entrada.getValue();
				
				int i = 0;
				for (Statement linha : condicionaisEncontrados) {
					if (linha instanceof IfStmt) {
						if (linha.toString().contains(nomeCampo) && linha.toString().contains("null")) {
							IfStmt condicional = (IfStmt) linha;
							if (linha.toString().contains("!=")) {
								/**retira o { da string pois senão ficaria estranho a função*/
								Statement s = JavaParser.parseStatement(condicional.getThenStmt().toString().replace("{", "").replace("}", ""));
								corpoMetodo.setStatement(i, s);
								linha.remove();
							} else if (linha.toString().contains("==")) {
								corpoMetodo.addStatement(nomeCampo + ".assertNotNull();");
								linha.remove();
							}
						}
					}
					i++;
				}
			}

			getCrenca().getExtrator().getCu().addImport(nomePacote + "." + nomeObjeto);
			getCrenca().getExtrator().getCu().addImport(nomePacote + "." + nomeClasse);
			getCrenca().getExtrator().getCu().addImport(nomePacote + "." + nomeClasseNullObjeto);
			gravarConteudo(getCrenca().getCaminhoClasse(), getCrenca().getExtrator().getCu().toString());
			return true;
		} catch (Exception ex) {
			throw new IllegalStateException("Erro - causado por: " + ex.getMessage());
		}
	}

	public void adicionarImports(String nomeClasse, CompilationUnit cu) {
		for (ImportDeclaration importacao : extratorObjeto.getCu().getImports()) {
			if (!importacao.getNameAsString().contains(nomeClasse)) {
				cu.addImport(importacao);
			}
		}
	}

	public void adicionarMetodo(String nomeMetodo, String tipoMetodo, ClassOrInterfaceDeclaration classeLida,
			String corpoMetodo) {
		adicionarMetodo(nomeMetodo, tipoMetodo, classeLida, corpoMetodo, publico, null);
	}

	/** facilitador para adicionar métodos no objeto com condições nulas. */
	public void adicionarMetodo(String nomeMetodo, String tipoMetodo, ClassOrInterfaceDeclaration classeLida,
			String corpoMetodo, EnumSet<Modifier> modificadores, NodeList<ReferenceType> throwObjeto) {
		boolean jaTemMetodo = classeLida.getMethodsByName(nomeMetodo).size() > 0;
		MethodDeclaration methodObjeto;
		if (!jaTemMetodo) {
			methodObjeto = new MethodDeclaration();
		} else {
			methodObjeto = classeLida.getMethodsByName(nomeMetodo).get(0);
		}
		methodObjeto.setName(nomeMetodo);
		methodObjeto.setType(tipoMetodo);
		methodObjeto.setModifiers(modificadores);

		BlockStmt corpo = new BlockStmt();
		if (!corpoMetodo.isEmpty()) {
			corpo.addStatement(corpoMetodo);
		}else {
			corpo = null;
		}
		if (throwObjeto != null) {
			methodObjeto.setThrownExceptions(throwObjeto);
		}
		methodObjeto.setBody(corpo);
		if (!jaTemMetodo) {
			classeLida.addMember(methodObjeto);
		}
	}

	@PlanPassed
	public void passed() {
		System.out.println("Plan finished successfully.");
	}

	@PlanAborted
	public void aborted() {
		System.out.println("Plan aborted.");
	}

	@PlanFailed
	public void failed(Exception e) {
		System.out.println("Plan failed: " + e);
	}

}
