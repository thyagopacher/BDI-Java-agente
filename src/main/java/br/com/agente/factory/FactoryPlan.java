package br.com.agente.factory;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

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
public class FactoryPlan extends ModificadorPlan  implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	@PlanAPI
	protected IPlan plan;
	
	public FactoryPlan() {
		System.out.println("\nPlan instanciado com Pattern Factory Method - ModificadorPlan");
	} 
 
	@PlanBody
	public boolean modificador() {
		try {
			
			String classeAvaliada = getCrenca().getNomeClasse();
			getCrenca().getExtrator().getClasseOrigem().setBlockComment("Classe modificada para ter padrão Factory Method");
			String nomePacote = getCrenca().getPacote().getNameAsString();
			Map<MethodDeclaration, List<Statement>> ondeModificar = getCrenca().getMapaMetodos();
			for (Map.Entry<MethodDeclaration, List<Statement>> entrada : ondeModificar.entrySet()) {
				MethodDeclaration metodoModificado = entrada.getKey();
				
				/**esvazia o corpo do método visto e deixa ele sem parametro*/
				metodoModificado.setBody(new BlockStmt());
				metodoModificado.getParameter(0).remove();
				
				/**roda os condicionais encontrados perante o método*/
				List<Statement> condicionaisEncontrados = entrada.getValue();
				for (Statement statement : condicionaisEncontrados) {
					IfStmt ifStmt = (IfStmt) statement;
					IfStmt elseThen = null;
					
					while(true) {

						String ifThen = ifStmt.getThenStmt().getChildNodes().toString();
						String separa_then[] = ifThen.split("new ");
						String nomeClasse = separa_then[1].replaceAll("'", "").replace("(", "").replace(");]", "");
						String nomeClasseArquivo = nomeClasse + "Factory";
						
						CompilationUnit cu = new CompilationUnit();
						cu.setPackageDeclaration(nomePacote);
						cu.setBlockComment("Classe gerada pelo Factory Method");
						
						ClassOrInterfaceDeclaration type = cu.addClass(nomeClasseArquivo);						
						// create a method coloca o retorno para ser uma instancia da classe
						BlockStmt block = new BlockStmt();
						block.addStatement("return new " + nomeClasse + "();");

						MethodDeclaration method = new MethodDeclaration();
						method.setName(metodoModificado.getName());
						method.setType(metodoModificado.getType());
						method.setModifiers(EnumSet.of(Modifier.PUBLIC));
						method.setBody(block);
						type.addMember(method);
						type.addExtends(classeAvaliada);
						
						gravarConteudo(getCrenca().getDiretorio() + "/" + nomeClasseArquivo + ".java", cu.toString());
						
						/**na última linha o else vai para o lugar do if fazendo assim rodar todos os possíveis else if*/
						if(ifStmt.getElseStmt().get() instanceof IfStmt) {
							elseThen = (IfStmt) ifStmt.getElseStmt().get();
							ifStmt = elseThen;					
						}else {
							break;
						}
					}
				}
			}
			
			/** seta classe factory pai para abstract */
			getCrenca().getExtrator().getClasseOrigem().addModifier(Modifier.ABSTRACT);
			
			gravarConteudo(getCrenca().getCaminhoClasse(), getCrenca().getExtrator().getCu().toString());
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
