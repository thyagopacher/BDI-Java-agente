package br.com.agente.nullobject;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import br.com.agente.Extrator;

public class NullObjectCrencaTest {

	NullObjectCrenca nullObject = new NullObjectCrenca();
	Extrator extrator;
	
	public NullObjectCrencaTest(){
		String caminhoClasse = "C:\\programa-java\\buscaJava\\buscaJava\\src\\exemplos\\HLAC.java";
		nullObject.defineExtrator(caminhoClasse);
		String diretorio = nullObject.getExtrator().getArquivo().getParentFile().toString();
		nullObject.setDiretorio(diretorio);	
	}
	
	@Test
	public void isSettertest() {
		try{
			MethodDeclaration metodo = extrator.getClasseOrigem().getMethods().stream().filter(l -> l.getName().toString().equals("setBuyer")).collect(Collectors.toList()).get(0);
			FieldDeclaration field = extrator.getClasseOrigem().getFieldByName("buyer").get();
			boolean res = nullObject.isSetter(metodo, field);
			assertTrue("Teste função isSetter", res);
		}catch(Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}
	}
	
	@Test
	public void isVoidTypeTest() {
		try{
			MethodDeclaration metodo = extrator.getClasseOrigem().getMethods().stream().filter(l -> l.getName().toString().equals("setBuyer")).collect(Collectors.toList()).get(0);
			boolean res = nullObject.isVoidType(metodo.getType());
			assertTrue("Teste função isVoidType", res);
		}catch(Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}		
	}
	
	@Test
	public void isLiteralTypeTest() {
		try{
			MethodDeclaration metodo = extrator.getClasseOrigem().getMethods().stream().filter(l -> l.getName().toString().equals("setBuyer")).collect(Collectors.toList()).get(0);
			boolean res = nullObject.isLiteralType(metodo.getType());
			assertTrue("Teste função isLiteralType", res);
		}catch(Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}		
	}
	
	
	@Test
	public void mapaMetodosTest() {
		try{
			boolean res = false;
			Map<MethodDeclaration, List<Statement>> retorno = nullObject.mapaMetodos();
			res = retorno != null && !retorno.isEmpty();
			assertTrue("Teste função mapaMetodos", res);
		}catch(Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}		
	}
}
