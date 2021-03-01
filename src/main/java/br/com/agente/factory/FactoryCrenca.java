package br.com.agente.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;

import br.com.agente.Crenca;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.runtime.BDIFailureException;

import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.body.Parameter;

public class FactoryCrenca extends Crenca implements java.io.Serializable {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FactoryCrenca() {
		this.setMetodosAplicaveis(null);
	}

	@Belief
	public Map<MethodDeclaration, List<Statement>> mapaMetodos() {
		System.out.println("== Testando se é aplicável Factory =>" + this.getNomeClasse());
		Map<MethodDeclaration, List<Statement>> mapaMetodosAnalisados = new HashMap<>();
		try {
			List<?> membros = getExtrator().getClasseOrigem().getMembers().stream()
					.filter(linha -> linha instanceof MethodDeclaration).collect(Collectors.toList());
			for (Object object : membros) {
				MethodDeclaration metodo = (MethodDeclaration) object;
				if (metodo.getType().toString().equals("void")) {
					continue;
				}

				List<Parameter> parametrosMetodo = metodo.getParameters();
				if (parametrosMetodo == null || parametrosMetodo.isEmpty()) {
					System.out.println(" -- Método:" + metodo.getName() + " -- não tem parametros não é usado factory");
					continue;
				}
				if (metodo.getBody() != null && metodo.getBody().isPresent() && !metodo.getBody().get().isEmpty()) {
					List<Statement> linhasMetodo = metodo.getBody().get().getStatements();
					if (linhasMetodo != null && !linhasMetodo.isEmpty()) {
						List<Statement> instrucoesIf = new ArrayList<>();
						for (Statement linha : linhasMetodo) {
							if (linha instanceof IfStmt) {
								IfStmt condicional = ((IfStmt) linha);
								List<Node> camposCondicao = condicional.getCondition().getChildNodes();
								for (Node campo : camposCondicao) {
									for (Parameter parametro : parametrosMetodo) {
										/** adaptado para não pegar condições com OR ou AND */
										String txtCondicao = condicional.getCondition().toString();
										if (parametro.getNameAsString().toString().equals(campo.toString())
												&& !txtCondicao.contains("||") && !txtCondicao.contains("&&")
												&& !txtCondicao.contains("exist")
												&& (txtCondicao.contains("equals") || txtCondicao.contains("=="))) {
											System.out.println("Achou parametro na condicional...");
											if (condicional != null) {
												instrucoesIf.add(condicional);
											} else {
												break;
											}
										}
									}
								}
							}
						}
						if (instrucoesIf != null && !instrucoesIf.isEmpty()) {
							mapaMetodosAnalisados.put(metodo, instrucoesIf);
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
