package br.com.agente.singleton;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import br.com.agente.Extrator;

public class SingletonCrencaTest {

	SingletonCrenca singletonCrenca = new SingletonCrenca();
	Extrator extrator;
	
	public SingletonCrencaTest(){
		String caminhoClasse = "C:\\programa-java\\projetos-para-testar-refatoracao\\cadastro\\SistEl-master\\src\\src\\com\\arthurassuncao\\sistel\\classes\\Arquivo.java";
		singletonCrenca.defineExtrator(caminhoClasse);
		String diretorio = singletonCrenca.getExtrator().getArquivo().getParentFile().toString();
		singletonCrenca.setDiretorio(diretorio);	
	}	
	
	@Test
	public void mapaMetodosTest() {
		try{
			boolean res = false;
			Map<MethodDeclaration, List<Statement>> retorno = singletonCrenca.mapaMetodos();
			res = retorno != null && !retorno.isEmpty();
			assertTrue("Teste função mapaMetodos", res);
		}catch(Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}		
	}
}
