package br.com.agente;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;

import br.com.visao.Objeto;
import jadex.bdiv3.annotation.Belief;

/**
 * fica responsável pela descoberta da estrutura da classe avaliada 
 * */
public abstract class Crenca {

	private String diretorio;
	private String caminhoClasse;
	private String nomeClasse;
	private Extrator extrator;
	private Type type;
	private PackageDeclaration pacote;
	private Map<MethodDeclaration, List<Statement>> metodosAplicaveis = null;
	private List<Objeto> listaAplicacao = new ArrayList<Objeto>();
	
	@Belief 
	public boolean isClass() {
		return true;
	}  

	public String getCaminhoClasse() {
		return caminhoClasse;
	}

	public void setCaminhoClasse(String caminhoClasse) {
		this.caminhoClasse = caminhoClasse; 
	} 

	/**
	 * analisa se a classe é aplicável a ter o Designer Pattern
	 * @return boolean true caso de para adaptar a classe para ter o Designer Pattern.
	 * */
	public abstract Map<MethodDeclaration, List<Statement>> mapaMetodos();

	public Extrator getExtrator() {
		return extrator;
	}

	/**
	 * faz a definição de extrator com new no corpo
	 * */
	public void defineExtrator(String caminho) {
		this.caminhoClasse = caminho;
		this.extrator = new Extrator(caminho);
		if(this.extrator.getClasseOrigem() != null) {
			this.nomeClasse = this.extrator.getClasseOrigem().getNameAsString();
		}
	}
	
	public void setExtrator(Extrator extrator) {
		this.extrator = extrator;
	}

	public String getNomeClasse() {
		return nomeClasse;
	}

	public void setNomeClasse(String nomeClasse) {
		this.nomeClasse = nomeClasse;
	}
	
	
	/**
	 * retorna o tipo de classe referente a classe que está sendo vista
	 * */
	public Type getType() {
		if(type == null) {
			type = JavaParser.parseClassOrInterfaceType(this.getNomeClasse());
		}
		return type;
	}	
	
	public Map<MethodDeclaration, List<Statement>> getMapaMetodos(){
		if(getMetodosAplicaveis() == null) {
			setMetodosAplicaveis(this.mapaMetodos());
		}
		return getMetodosAplicaveis();
	}

	public Map<MethodDeclaration, List<Statement>> getMetodosAplicaveis() {
		return metodosAplicaveis;
	}

	public void setMetodosAplicaveis(Map<MethodDeclaration, List<Statement>> metodosAplicaveis) {
		this.metodosAplicaveis = metodosAplicaveis;
	}

	public String getDiretorio() {
		return diretorio;
	}

	public void setDiretorio(String diretorio) {
		this.diretorio = diretorio;
	}

	public PackageDeclaration getPacote() {
		if(pacote == null) {
			pacote = extrator.getCu().getPackageDeclaration().get();
		}
		return pacote;
	}

	public void setPacote(PackageDeclaration pacote) {
		this.pacote = pacote;
	}
	
	public boolean isSetter(MethodDeclaration m, FieldDeclaration f) {
		return false;
	}

	public List<Objeto> getListaAplicacao() {
		return listaAplicacao;
	}

	public void setListaAplicacao(List<Objeto> listaAplicacao) {
		this.listaAplicacao = listaAplicacao;
	}
}
