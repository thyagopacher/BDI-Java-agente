package br.com.agente;

import br.com.agente.factory.FactoryCrenca;
import br.com.agente.factory.FactoryPlan;
import br.com.agente.nullobject.NullObjectCrenca;
import br.com.agente.nullobject.NullObjectPlan;
import br.com.agente.singleton.SingletonCrenca;
import br.com.agente.singleton.SingletonPlan;
import br.com.agente.strategy.StrategyCrenca;
import br.com.agente.strategy.StrategyPlan;

/**
 * classe responsável para guardar referencia a tudo que o padrão sabe e também como modifica-lo
 * o Pattern aqui referenciado pode ser Singleton, Facade, NullObject, etc...
 * */
public class Padrao implements java.io.Serializable{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String nome;
	 
	private String metodo; 

	/**
	 * Crença trazendo tudo relacionado ao conhecimento do Pattern
	 * */
	private Crenca crenca;
	private ModificadorPlan plano;
	private DesejoGoal desejo;


	public Padrao(String nome) {
		this.nome = nome;
	}

	public Padrao(String nome, String metodo) {
		this.nome = nome;
		this.metodo = metodo;
	}
	
	public Padrao(Crenca crenca, ModificadorPlan plano, String nome, String metodo, String caminhoArquivo) {
		this.crenca = crenca;
		this.plano = plano;
		this.plano.setCrenca(crenca);
		this.nome = nome;
		this.metodo = metodo;
		this.crenca.defineExtrator(caminhoArquivo);
		String diretorio = getCrenca().getExtrator().getArquivo().getParentFile().toString();
		this.crenca.setDiretorio(diretorio);
	}	
	
	/**
	 * verifica qual padrão será instanciado
	 * @author ThyagoHenrique
	 * @param caminhoArquivo - onde está o arquivo que vai ser lido e modificado
	 * @return Padrao - previamente instanciado dentro de si a sua crença e plano
	 * */
	public Padrao getPadrao(String caminhoArquivo) {
		if(this.getNome().equals("singleton")) {
			return new Padrao(new SingletonCrenca(), new SingletonPlan(), this.getNome(), "cinneideEtal", caminhoArquivo);
		}else if(this.getNome().equals("null-object")) {
			return new Padrao(new NullObjectCrenca(), new NullObjectPlan(), this.getNome(), "gaitaniEtal", caminhoArquivo);
		}else if(this.getNome().equals("factory")) {
			return new Padrao(new FactoryCrenca(), new FactoryPlan(), this.getNome(), "weiEtal", caminhoArquivo);
		}else if(this.getNome().equals("strategy")) {
			return new Padrao(new StrategyCrenca(), new StrategyPlan(), this.getNome(), "weiEtal", caminhoArquivo);
		}
		return null;
	}
	
	public Crenca getCrenca() {
		return crenca;
	}
	
	public void setCrenca(Crenca crenca) {
		this.crenca = crenca;
	}
	public ModificadorPlan getPlano() {
		return plano;
	}
	public void setPlano(ModificadorPlan plano) {
		this.plano = plano;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}
	
	
	public DesejoGoal getDesejo() {
		return desejo;
	}

	public void setDesejo(DesejoGoal desejo) {
		this.desejo = desejo;
	}	
	
}
