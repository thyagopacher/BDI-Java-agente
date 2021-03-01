package br.com.agente.factory;

import static org.junit.Assert.*;

import org.junit.Test;

import br.com.agente.Extrator;
import br.com.agente.nullobject.NullObjectCrenca;
import br.com.agente.nullobject.NullObjectPlan;

public class FactoryPlanTest {

	FactoryCrenca factorytCrenca = new FactoryCrenca();
	FactoryPlan factoryPlano = new FactoryPlan();
	Extrator extrator;	
	
	public FactoryPlanTest(){
		String caminhoClasse = "C:\\programa-java\\exemplo-cavado-Liu-factory\\src\\br\\com\\padrao\\LoggerFactory.java";
		factorytCrenca.defineExtrator(caminhoClasse);
		String diretorio = factorytCrenca.getExtrator().getArquivo().getParentFile().toString();
		factorytCrenca.setDiretorio(diretorio);	
		factoryPlano.setCrenca(factorytCrenca);
		
	}	
	
	@Test
	public void modificadorTest() {
		/**verifica se a modificação foi feita com sucesso.*/
		boolean res = factoryPlano.modificador();
		assertTrue("Teste função modificador", res);
	}

}
