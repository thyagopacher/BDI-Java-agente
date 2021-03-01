package singleton;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import br.com.agente.ModificadorPlan;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanAborted;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanFailed;
import jadex.bdiv3.annotation.PlanPassed;
import jadex.bdiv3.runtime.IPlan;
import jadex.bdiv3.runtime.impl.PlanFailureException;

/**
 * não funciona sem Plan no funcional do nome da classe possivelmente a intenção
 * final
 */

@Plan
public class SingletonPlan extends ModificadorPlan{

	private static final EnumSet<Modifier> PRIVATE_STATIC = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC);		
	private static final EnumSet<Modifier> PUBLIC_STATIC = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);	
	
	@PlanAPI
	protected IPlan plan;
	
	public SingletonPlan() {
		System.out.println("Plan instanciado com Pattern Singleton");
	}

	@PlanBody
	public boolean modificador() {
		try {
			getCrenca().getExtrator().getClasseOrigem().setBlockComment("Classe modificada para ter padrão Singleton");
			
			/**atributo estático e private*/
			getCrenca().getExtrator().getClasseOrigem().addField(getCrenca().getType(), "singleton", (Modifier[]) Arrays.asList(Modifier.PRIVATE, Modifier.STATIC).toArray());
			
			/**procura se tem construtor*/
			List<?> membros = getCrenca().getExtrator().getClasseOrigem().getMembers().stream().filter(linha -> linha instanceof ConstructorDeclaration).collect(Collectors.toList());
			if(!membros.isEmpty()) {
				for (Object object : membros) {
					((ConstructorDeclaration) object).setModifiers(Modifier.PRIVATE.toEnumSet());
				}
			}else {
				/**cria um construtor caso não tenha nenhum do tipo privado*/
				getCrenca().getExtrator().getClasseOrigem().addConstructor(Modifier.PRIVATE);
			}
			
			/**corpo do método Singleton*/
			BlockStmt block = new BlockStmt();
			block.addStatement("if(singleton == null){singleton = new "+getCrenca().getNomeClasse()+"();}").addStatement("return singleton;");
			
			/**declaração do método para retornar instância única*/
			MethodDeclaration method = new MethodDeclaration(PUBLIC_STATIC, getCrenca().getType(), "getInstance");
			method.setBody(block).setBlockComment("Método Singleton para retornar a instância única");
			getCrenca().getExtrator().getClasseOrigem().addMember(method);
			gravarConteudo(getCrenca().getExtrator().getArquivo().toPath().toString(), getCrenca().getExtrator().getCu().toString());
			
		} catch (Exception ex) {
			throw new PlanFailureException();
		}
		return false;
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
