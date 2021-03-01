package br.com.agente;

import java.io.FileWriter;
import java.io.IOException;

public abstract class ModificadorPlan {
	
	/**
	 * serve para definir qual crença está sendo trabalhada.
	 * */
	private Crenca crenca;	
		
	public abstract boolean modificador();
	
 
	/**
	 * grava o conteúdo dentro do arquivo sendo enviado
	 * @param caminhoArquivo - onde o arquivo irá ser gravado
	 * @param texto - todo o texto modificado da classe
	 * @exception - caso de problemas na gravação do arquivo
	 * */
	public void gravarConteudo(String caminhoArquivo, String texto) {
		try {
			FileWriter fileWriter1 = new FileWriter(caminhoArquivo);
			fileWriter1.write(texto);
			fileWriter1.flush();
			fileWriter1.close();
		} catch (IOException ex) {
			throw new IllegalStateException("Erro ao gravar arquivo - causado por: " + ex.getMessage());
		}
	}	

	public Crenca getCrenca() {
		return crenca;
	}

	public void setCrenca(Crenca crenca) {
		this.crenca = crenca;
	}
	
}
