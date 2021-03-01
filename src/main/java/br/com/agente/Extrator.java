package br.com.agente;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * Classe responsável por fazer a leitura base de outros arquivos
 * */
public class Extrator {

	/**
	 * traduz o arquivo em algo viável a ser usado para ler a classe
	 * */
	private CompilationUnit cu;
	 
	/**
	 * lê arquivo e traz objeto
	 * */
	private File arquivo;
	
	/**
	 * atributo usado para identificar a estrutura da classe via javaparser
	 * */
	private ClassOrInterfaceDeclaration classeOrigem;
	
	public Extrator(String caminhoClasse) {
		try {
			arquivo = new File(caminhoClasse);
			cu = JavaParser.parse(arquivo);
			if(cu.getTypes().size() > 0) {
				if(cu.getType(0) instanceof ClassOrInterfaceDeclaration) {
					classeOrigem = (ClassOrInterfaceDeclaration) cu.getType(0);
					System.out.println("Classe lida no extrator: " + classeOrigem.getNameAsString());
				}
			}
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Erro - Construtor Extrator - causado por: " + e.getMessage());
		}
	}

	public CompilationUnit getCu() {
		return cu;
	}

	public void setCu(CompilationUnit cu) {
		this.cu = cu;
	}

	public File getArquivo() {
		return arquivo;
	}

	public void setArquivo(File arquivo) {
		this.arquivo = arquivo;
	}

	public ClassOrInterfaceDeclaration getClasseOrigem() {
		return classeOrigem;
	}

	public void setClasseOrigem(ClassOrInterfaceDeclaration classeOrigem) {
		this.classeOrigem = classeOrigem;
	}
}
