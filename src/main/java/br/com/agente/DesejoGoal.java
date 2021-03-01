package br.com.agente;

import java.util.ArrayList;
import java.util.List;

import br.com.visao.Objeto;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalResult;
import jadex.bdiv3.annotation.GoalTargetCondition;

/**
 * fica responsável por ter o método analisador, usando como referencia a crença
 * que tem a descoberta do que é a classe.
 */
@Goal
public class DesejoGoal {

	@GoalResult
	private Object retorno;
	private Padrao padrao;
	private Crenca crenca;
	private boolean jaViuTodosPadroes = false;
	private boolean jaViuTodosArquivos = false;
	
	public DesejoGoal(Padrao padrao, boolean jaViuTodosPadroes, boolean jaViuTodosArquivos) {
		this.setPadrao(padrao);
		this.jaViuTodosPadroes = jaViuTodosPadroes;
		this.jaViuTodosArquivos = jaViuTodosArquivos;
	} 

	/**
	 * verifica se tem algo aplicável mediante o Pattern
	 * */
	@GoalTargetCondition(beliefs = "retorno")
	public Object analisador() {
		System.out.println(" == É aplicável para o Desejo de " + padrao.getNome() + "? => Classe: " + padrao.getCrenca().getNomeClasse());
		padrao.getCrenca().mapaMetodos();
		retorno = true;
		return true;			
	}

	public Crenca getCrenca() {
		return crenca;
	}

	public void setCrenca(Crenca crenca) {
		this.crenca = crenca;
	}

	public Padrao getPadrao() {
		return padrao;
	}

	public void setPadrao(Padrao padrao) {
		this.padrao = padrao;
	}

}
