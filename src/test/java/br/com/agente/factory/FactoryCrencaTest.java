package br.com.agente.factory;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import br.com.agente.Extrator;

public class FactoryCrencaTest {

	FactoryCrenca factoryCrenca = new FactoryCrenca();
	Extrator extrator;	
	
	public FactoryCrencaTest() {
		String caminhoClasse = "C:\\programa-java\\projetos-para-testar-refatoracao\\cadastro\\SistEl-master\\src\\src\\com\\arthurassuncao\\sistel\\app\\package-info.java";
		factoryCrenca.defineExtrator(caminhoClasse);
		String diretorio = factoryCrenca.getExtrator().getArquivo().getParentFile().toString();
		factoryCrenca.setDiretorio(diretorio);			
	}
	
	@Test
	public void mapaMetodosTest() {
		try{
			boolean res = false;
			Map<MethodDeclaration, List<Statement>> retorno = factoryCrenca.mapaMetodos();
			res = retorno != null && !retorno.isEmpty();
			assertTrue("Teste função mapaMetodos", res);
		}catch(Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}		
	}

}
