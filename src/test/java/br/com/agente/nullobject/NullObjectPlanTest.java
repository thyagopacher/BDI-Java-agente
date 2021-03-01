package br.com.agente.nullobject;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.agente.Extrator;

public class NullObjectPlanTest {

	NullObjectCrenca nullObjectCrenca = new NullObjectCrenca();
	NullObjectPlan nullObjectPlano = new NullObjectPlan();
	Extrator extrator;
	
	 
	public NullObjectPlanTest(){
		String caminhoClasse = "C:\\programa-java\\exemplo-cavada-gaitani-nullobject\\src\\br\\com\\padrao\\ShoppingCart.java";
		nullObjectCrenca.defineExtrator(caminhoClasse);
		String diretorio = nullObjectCrenca.getExtrator().getArquivo().getParentFile().toString();
		nullObjectCrenca.setDiretorio(diretorio);	
		nullObjectPlano.setCrenca(nullObjectCrenca);
		
	}	
	
	@Test
	public void modificadorTest() {
		/**verifica se a modificação foi feita com sucesso.*/
		boolean res = nullObjectPlano.modificador();
		assertTrue("Teste função modificador", res);
	}

}
