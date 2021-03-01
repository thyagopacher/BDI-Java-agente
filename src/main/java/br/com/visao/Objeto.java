package br.com.visao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import br.com.agente.Padrao;

/**
 * usado para tablemodel sendo pego em um list e depois disso para fazer as modifica��es
 * */
public class Objeto implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;

  
	private String classe;
	
	/**
	 * qual padrão aplicado aquele artigo estipulou
	 * vem como nome, Crenca e Plano de execução no objeto
	 * */
	private Padrao padrao;
	private boolean aplicado;
	private static String nomeArquivoIgnorados = "objetos-ignorados";
	 
	/**
	 * para ver qual método de artigo foi feito
	 * */
	private String metodo;

	/**
	 * atributo usado para guardar dados de analise e aplicar a refatoração após clicar em executar
	 * */
	private Map<MethodDeclaration, List<Statement>> mapaMetodos;	
	
	public Objeto() {
		
	}
	
	public Objeto(String classe, Padrao padrao, boolean aplicado) {
		this.classe = classe;
		this.padrao = padrao;
		this.aplicado = aplicado;
		this.metodo = padrao.getMetodo();
	}
	
	public Objeto(String classe, Padrao padrao, Map<MethodDeclaration, List<Statement>> mapaMetodos) {
		this.classe = classe;
		this.setMapaMetodos(mapaMetodos);
		this.padrao = padrao;		
		this.aplicado = false;
		this.metodo = padrao.getMetodo();
	}	
	
	public String getClasse() {
		return classe;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
	public Padrao getPadrao() {
		return padrao;
	}
	public void setPadrao(Padrao padrao) {
		this.padrao = padrao;
	}
	public boolean isAplicado() {
		return aplicado;
	}
	public void setAplicado(boolean aplicado) {
		this.aplicado = aplicado;
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}

	public static List<Objeto> lerObjetosIgnorados() {
		try {
			File arquivo = new File(nomeArquivoIgnorados);
			if (arquivo.exists() && arquivo.canRead()) {
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
					return (List<Objeto>) ois.readObject();
				}
			}
		} catch (IOException | ClassNotFoundException ex) {
			System.out.println("Erro ao ler arquivo - causado por: " + ex.getMessage());
		}
		return null;
	}

	public static void gravarObjetosIgnorados(List<Objeto> objetos) {
		try {
			java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new FileOutputStream(new File(nomeArquivoIgnorados)));
			oos.writeObject(objetos);
			oos.close();
		} catch (IOException ex) {
			System.out.println("Erro ao gravar arquivo - causado por: " + ex.getMessage());
		}
	}

	public Map<MethodDeclaration, List<Statement>> getMapaMetodos() {
		return mapaMetodos;
	}

	public void setMapaMetodos(Map<MethodDeclaration, List<Statement>> mapaMetodos) {
		this.mapaMetodos = mapaMetodos;
	}	
}
