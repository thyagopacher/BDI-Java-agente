package br.com.agente.strategy;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.TypeParameter;

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
public class StrategyPlan extends ModificadorPlan implements java.io.Serializable {

	@PlanAPI
	protected IPlan plan;

	public StrategyPlan() {
		System.out.println("\nPlan instanciado com Pattern Strategy");
	}

	@PlanBody
	public boolean modificador() {
		try {
			String nomePacote = getCrenca().getPacote().getNameAsString();
			String separaCondicao[] = null;
			getCrenca().getExtrator().getClasseOrigem().setBlockComment("Classe modificada para ter padrão Strategy");

			Map<MethodDeclaration, List<Statement>> ondeModificar = getCrenca().getMapaMetodos();
			for (Map.Entry<MethodDeclaration, List<Statement>> entrada : ondeModificar.entrySet()) {
				MethodDeclaration metodoModificado = entrada.getKey();
				List<Statement> condicionaisEncontrados = entrada.getValue();

				IfStmt ifStmt1 = (IfStmt) condicionaisEncontrados.get(0);
				String retorno1 = ifStmt1.getThenStmt().toString().replaceAll("[0-9]", "")
						.replaceAll("[-+=*;%$#@!{}.]", "").replace("return ", "");
				String nomeParametro = retorno1.trim();

				/** precisa criar antes a classe abstract */
				CompilationUnit cu1 = new CompilationUnit();
				ClassOrInterfaceDeclaration type1 = cu1.addClass("Strategy");
				MethodDeclaration method1 = new MethodDeclaration();
				method1.setName(metodoModificado.getName());
				method1.setType(metodoModificado.getType());
				method1.setModifiers(EnumSet.of(Modifier.PUBLIC));
				method1.addParameter(metodoModificado.getType(), nomeParametro);
				type1.addMember(method1);
				type1.addModifier(Modifier.ABSTRACT);
				gravarConteudo(getCrenca().getDiretorio() + "/Strategy.java", cu1.toString());

				for (Statement statement : condicionaisEncontrados) {
					/** criar um arquivo ConcreteStrategy + code com cada if */
					IfStmt ifStmt = (IfStmt) statement;
					IfStmt elseThen = null;

					while (true) {
						String condicao = ifStmt.getCondition().toString();
						if (condicao.contains(" == ")) {
							separaCondicao = condicao.split(" == ");
						} else if (condicao.contains(" >= ")) {
							separaCondicao = condicao.split(" >= ");
						} else if (condicao.contains(" <= ")) {
							separaCondicao = condicao.split(" <= ");
						} else if (condicao.contains(" != ")) {
							separaCondicao = condicao.split(" != ");
						}

						String nomeClasse = separaCondicao[1].replaceAll("'", "");
						String nomeClasseArquivo = "ConcreteStrategy" + nomeClasse;

						CompilationUnit cu = new CompilationUnit();
						cu1.addImport(nomePacote + ".Strategy;");
						cu.setPackageDeclaration(getCrenca().getPacote());
						cu.setBlockComment("Classe Concreta de Strategy gerada com base no padrão Strategy");
						ClassOrInterfaceDeclaration type = cu.addClass(nomeClasseArquivo);

						/** criar um método */
						BlockStmt block = new BlockStmt();
						block.addStatement(ifStmt.getThenStmt().toString().replace("{", "").replace("}", ""));
						MethodDeclaration method = new MethodDeclaration();
						method.setName(metodoModificado.getName());
						method.setType(metodoModificado.getType());
						method.setModifiers(EnumSet.of(Modifier.PUBLIC));
						method.addParameter(metodoModificado.getType(), nomeParametro);
						method.setBody(block);
						type.addMember(method);
						type.addExtends("Strategy");

						gravarConteudo(getCrenca().getDiretorio() + "/" + nomeClasseArquivo + ".java", cu.toString());
						
						/**na última linha o else vai para o lugar do if fazendo assim rodar todos os possíveis else if*/
						if(ifStmt.getElseStmt().isPresent() && ifStmt.getElseStmt().get() instanceof IfStmt) {
							elseThen = (IfStmt) ifStmt.getElseStmt().get();
							ifStmt = elseThen;					
						}else {
							break;
						}						
					}
				}

				/** reescreve Método da classe refatorada */
				BlockStmt block = new BlockStmt();
				block.addStatement("return strategy." + metodoModificado.getName() + "(" + nomeParametro + ");");
				metodoModificado.setBody(block);
				Parameter parametro = new Parameter();
				parametro.setName("strategy");
				parametro.setType("Strategy");
				metodoModificado.setParameter(0, parametro);
				getCrenca().getExtrator().getCu().addImport(nomePacote + ".Strategy;");
				gravarConteudo(getCrenca().getCaminhoClasse(), getCrenca().getExtrator().getCu().toString());
			}

			return true;
		} catch (Exception ex) {
			throw new IllegalStateException("Erro - causado por: " + ex.getMessage());
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
