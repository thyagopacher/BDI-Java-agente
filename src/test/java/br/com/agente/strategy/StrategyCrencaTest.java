package br.com.agente.strategy;

import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import br.com.agente.Extrator;

public class StrategyCrencaTest {

	StrategyCrenca strategyCrenca = new StrategyCrenca();
	Extrator extrator;
	
	public StrategyCrencaTest(){
		String caminhoClasse = "C:\\programa-java\\exemplo-cavado-Liu-strategy\\src\\br\\com\\padrao\\MovieTicket.java";
		strategyCrenca.defineExtrator(caminhoClasse);
		String diretorio = strategyCrenca.getExtrator().getArquivo().getParentFile().toString();
		strategyCrenca.setDiretorio(diretorio);	
	}	
	
	@Test
	public void mapaMetodosTest() {
		try{ 
			boolean res = false;
			Map<MethodDeclaration, List<Statement>> retorno = strategyCrenca.mapaMetodos();
			res = retorno != null && !retorno.isEmpty();
			assertTrue("Teste função mapaMetodos", res);
		}catch(Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}		
	}

}
