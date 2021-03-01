package br.com.agente.strategy;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.agente.Extrator;

public class StrategyPlanTest {

	StrategyCrenca strategyCrenca = new StrategyCrenca();
	StrategyPlan strategyPlano = new StrategyPlan();
	Extrator extrator;
	
	public StrategyPlanTest(){
		String caminhoClasse = "C:\\programa-java\\exemplo-cavado-Liu-strategy\\src\\br\\com\\padrao\\MovieTicket.java";
		strategyCrenca.defineExtrator(caminhoClasse);
		String diretorio = strategyCrenca.getExtrator().getArquivo().getParentFile().toString();
		strategyCrenca.setDiretorio(diretorio);	
		strategyPlano.setCrenca(strategyCrenca);
	}		
	
	@Test
	public void modificadorTest() {
		/**verifica se a modificação foi feita com sucesso.*/
		boolean res = strategyPlano.modificador();
		assertTrue("Teste função modificador", res);
	}
 
}
