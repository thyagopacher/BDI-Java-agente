package br.com.agente.singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import br.com.agente.Crenca;
import br.com.visao.Objeto;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.runtime.BDIFailureException;

public class SingletonCrenca extends Crenca implements java.io.Serializable {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SingletonCrenca() {
		this.setMetodosAplicaveis(null);
	}

	@Belief
	public Map<MethodDeclaration, List<Statement>> mapaMetodos() {
		System.out.println("== Testando se é aplicável no Singleton ==");

		/**
		 * para ver se nenhuma classe definida antes é pai de outra que está sendo lida
		 * agora, e neste caso deve ser retirada da refatoração
		 */
		if (this.getListaAplicacao() != null && !this.getListaAplicacao().isEmpty()) {
			for (int i = 0; i < this.getListaAplicacao().size(); i++) {
				Objeto objetoAnterior = this.getListaAplicacao().get(i);
				/* se a classe atual tiver alguma extensão */
				if (this.getExtrator().getClasseOrigem().getExtendedTypes().size() > 0) {
					for (ClassOrInterfaceType classeExtendida : this.getExtrator().getClasseOrigem()
							.getExtendedTypes()) {
						if (objetoAnterior.getClasse().replace(".java", "").equals(classeExtendida.getNameAsString())) {
							/**
							 * possivel classe anterior está sendo usado como extensão em uma classe
							 * atualmente lida - como filha
							 */
							this.getListaAplicacao().remove(objetoAnterior);
						}
					}
				}
			}
		}

		if (this.getExtrator().getClasseOrigem().getExtendedTypes().size() > 0) {
			System.out.println("Classe " + this.getNomeClasse() + " tem herança e por isso não pode ser Singleton");
			return null;
		} else if (this.getExtrator().getClasseOrigem().isInterface()
				|| this.getExtrator().getClasseOrigem().isAbstract()) {
			System.out.println(
					"Classe " + this.getNomeClasse() + " é interface ou abstrata e por isso não pode ser Singleton");
			return null;
		}

		Map<MethodDeclaration, List<Statement>> mapaMetodosAnalisados = new HashMap<>();
		List<Statement> construtores = new ArrayList<>();
		try {
			List<?> membros = getExtrator().getClasseOrigem().getMembers().stream()
					.filter(linha -> linha instanceof ConstructorDeclaration).collect(Collectors.toList());
			if (!membros.isEmpty()) {
				for (Object object : membros) {
					if (object instanceof ConstructorDeclaration) {
						ConstructorDeclaration construtor = (ConstructorDeclaration) object;
						int qtdParametros = construtor.getParameters().size();

						if (qtdParametros > 0) {
							System.out.println("Construtor da classe " + this.getNomeClasse() + " tem " + qtdParametros
									+ " parametros e por isso não pode ser Singleton");
							return null;
						} else if (construtor.isPrivate()
								&& this.getExtrator().getClasseOrigem().getMethodsByName("getInstance").size() > 0) {
							System.out.println("Classe " + this.getNomeClasse()
									+ " já tem construtor privado e método getInstance e por isso não pode ser Singleton");
							return null;
						} else {
							construtores.add(new ConstructorDeclaration().getBody());
						}
					}
				}
			}
			mapaMetodosAnalisados.put(new MethodDeclaration(), construtores);
			this.setMetodosAplicaveis(mapaMetodosAnalisados);
			return mapaMetodosAnalisados;
		} catch (BDIFailureException ex) {
			throw new IllegalStateException("Erro - causado por: " + ex.getMessage());
		}
	}

}
