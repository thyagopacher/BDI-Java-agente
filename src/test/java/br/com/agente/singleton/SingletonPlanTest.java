package br.com.agente.singleton;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import br.com.agente.Extrator;

public class SingletonPlanTest {

	SingletonCrenca singletonCrenca = new SingletonCrenca();
	SingletonPlan singletonPlano = new SingletonPlan();
	Extrator extrator;
	
	public SingletonPlanTest(){
		String caminhoClasse = "C:\\programa-java\\exemplo-singleton\\FileLogger.java";
		singletonCrenca.defineExtrator(caminhoClasse);
		String diretorio = singletonCrenca.getExtrator().getArquivo().getParentFile().toString();
		singletonCrenca.setDiretorio(diretorio);	
		singletonPlano.setCrenca(singletonCrenca);
	}		
	
	@Test 
	public void modificadorTest() {
		/**verifica se a modificação foi feita com sucesso.*/
		boolean res = singletonPlano.modificador();
		assertTrue("Teste função modificador", res);
	}
}
