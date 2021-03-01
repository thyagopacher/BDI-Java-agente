package singleton;

import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ConstructorDeclaration;

import br.com.agente.Crenca;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.runtime.BDIFailureException;

public class CrencaSingleton extends Crenca {


	public CrencaSingleton() {
		// TODO Auto-generated constructor stub
	}

	
	@Belief
	public boolean ehAplicavel() {
		System.out.println("== Testando se é aplicável ==");
		try {
			List<?> membros = getExtrator().getClasseOrigem().getMembers().stream().filter(linha -> linha instanceof ConstructorDeclaration).collect(Collectors.toList());
			for (Object object : membros) {
				if(object instanceof ConstructorDeclaration) {
					int qtdNaoPublico = ((ConstructorDeclaration) object).getModifiers().stream().filter(l -> !l.asString().equals("public")).collect(Collectors.toList()).size();
					if(qtdNaoPublico > 0) {
						return false;
					}
				}
			}
			return !getExtrator().getClasseOrigem().isAbstract() && !getExtrator().getClasseOrigem().isInterface();
		} catch (BDIFailureException ex) {
			throw new IllegalStateException("Erro - causado por: " + ex.getMessage());
		} 
	}

}
