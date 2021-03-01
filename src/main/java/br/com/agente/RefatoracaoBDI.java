package br.com.agente;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import br.com.agente.singleton.SingletonPlan;
import br.com.visao.Objeto;
import br.com.visao.TableModel;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Goals;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.BDIFailureException;
import jadex.bdiv3.runtime.impl.GoalFailureException;
import jadex.bridge.component.IExecutionFeature;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Description;

/**
 * O nome da classe tem que acabar com o "BDI" (por exemplo MyAgent,
 * ChatAgent...) - sÃ³ assim é reconhecido como agente BDI - necessário sempre
 * ter todos os planos e goals na referÃªncia do agente.
 */

@Description("Agente para refotação de projetos com Patterns")
@Agent
@Arguments(@Argument(name = "caminho", description = "caminho do projeto a ser analisado", clazz = String.class, defaultvalue = ""))
@Goals(@Goal(clazz = DesejoGoal.class))
@Plans(@Plan(body = @Body(SingletonPlan.class)))
public class RefatoracaoBDI {

	private int qtdIgnorados = 0;
	private boolean acabouFecharJanela = false;
	private int qtdSegundos = 60, qtdIniciado = 1, qtdClassesLidas = 0;

	@AgentArgument
	public String caminho = "";

	protected List<Objeto> listaAplicacao = new ArrayList<Objeto>();
	protected List<Padrao> padroes;

	/**
	 * crença com situação dinamica, fica esperando uma troca de seu valor em tempo
	 * de execução booleano que diz se já escolheu ou não padrões e o agente torna a
	 * repetir quando o valor for trocado.
	 */
	@Belief(dynamic = true)
	private boolean escolhendoPadroes = false;

	@Belief(dynamic = true)
	public Object retorno;

	/**
	 * Isso permitirá que o mecanismo injete automaticamente o recurso do agente BDI
	 * para a classe do agente POJO
	 */
	@AgentFeature
	protected IBDIAgentFeature bdiFeature;

	@AgentFeature
	protected IExecutionFeature execFeature;

	@AgentCreated
	public void init() {
		System.out.println(" == Agente criado == ");
		padroes = new ArrayList<Padrao>(4);
		padroes.add(new Padrao("factory", "weiEtal"));
		padroes.add(new Padrao("strategy", "weiEtal"));
		padroes.add(new Padrao("singleton", "cinneideEtal"));
		padroes.add(new Padrao("null-object", "gaitaniEtal"));
	}

	@AgentBody
	public void body() throws InterruptedException {
		iniciarAgente();
	}

	/**
	 * ideia de plano local ao agente podendo fazer uso do evento de trigger para
	 * executar a leitura do projeto
	 */
	@Plan(trigger = @Trigger(factchangeds = "escolhendoPadroes"))
	public void iniciarAgente() throws InterruptedException {
		if (!escolhendoPadroes) {
			if (acabouFecharJanela) {
				Thread.sleep(qtdSegundos * 1000);
				++qtdIniciado;
			}

			escolhendoPadroes = true;
			listaAplicacao = new ArrayList<Objeto>();
			System.out.println("======== Iniciando leitura do projeto =====");
			try {
				if (lerProjeto(caminho)) {
					montaTabela();
				}
				System.out.println("Terminou a leitura de tudo... ");
			} catch (GoalFailureException ex) {
				String msg = ex.getMessage();
				System.out.println("Erro ao atingir objetivo (Desejo): " + msg + " - caminho: " + caminho);
				if (msg.contains("No more candidates")) {
					if (listaAplicacao == null || listaAplicacao.isEmpty()) {
						/** faz retornar a verificar depois de qtdSegundos */
						System.out.println(" == Não tem mais candidatos para refatoração == vai esperar:" + qtdSegundos
								+ " segundos. Reiniciado (" + qtdIniciado + ") vezes");
						++qtdIniciado;
						Thread.sleep(qtdSegundos * 1000);
						escolhendoPadroes = false;
					} else if (!listaAplicacao.isEmpty()) {
						montaTabela();// faz montagem de tabelas
					}
				}
			} catch (BDIFailureException ex) {
				System.out.println("Erro ao iniciar agente BDI: " + ex.getMessage());
			}
			System.out.println("======== Final leitura do projeto - Qtd de classes lidas: "+qtdClassesLidas+" =====");
			System.out.println("======== Qtd. de objetos ignorados: " + qtdIgnorados + " =====");
		}

	}

	/**
	 * realiza a leitura do projeto e seus diretórios de maneira recursiva
	 * 
	 * @param caminho - onde está o arquivo / diretório a ser lido
	 * @return boolean - para dizer se acabou de ler o projeto e foi bem
	 */
	public boolean lerProjeto(String caminho) {
		boolean ehAplicavel = false, jaViuTodosPadroes = false;
		int qtdPadroesVistos = 0;
		int qtdArquivosVistosDiretorio = 0;
		Map<MethodDeclaration, List<Statement>> mapaMetodos = null;
		List<Objeto> objetosIgnoradosAnteriormente = Objeto.lerObjetosIgnorados();
		boolean temobjetosIgnoradosAnteriormente = objetosIgnoradosAnteriormente != null
				&& !objetosIgnoradosAnteriormente.isEmpty();
		boolean jaViuTodosArquivos = false;
		File[] files = new File(caminho).listFiles();

		if (files != null && files.length > 0) {
			for (File arquivo : files) {
				if (arquivo.isDirectory()) {
					this.lerProjeto(caminho + "/" + arquivo.getName());
				} else if (!arquivo.isDirectory() && arquivo.getName().endsWith(".java")) {
					qtdClassesLidas++;
					String diretorio = arquivo.getParentFile().toString();
					System.out.println("Diretório testado: " + diretorio);
					int qtdArquivosDiretorio = new File(diretorio).listFiles().length;
					jaViuTodosArquivos = qtdArquivosVistosDiretorio == qtdArquivosDiretorio - 1;

					qtdPadroesVistos = 0;
					for (Padrao padrao : padroes) {
						System.out.println(
								"======== Padrao testado: " + padrao.getNome() + " - no arquivo: " + arquivo.getName());
						padrao = padrao.getPadrao(arquivo.toPath().toString());
						if (padrao.getCrenca().getExtrator().getClasseOrigem() == null) {
							break;// para de rodar padrões perante o arquivo, pois não achou nenhuma clase nele
						}
						jaViuTodosPadroes = qtdPadroesVistos == padroes.size() - 1;
						/*
						 * usado para fazer comparação de extends com classes que já foram ditas boas e
						 * suas possiveis filhas
						 */
						padrao.getCrenca().setListaAplicacao(listaAplicacao);

						retorno = bdiFeature
								.dispatchTopLevelGoal(new DesejoGoal(padrao, jaViuTodosPadroes, jaViuTodosArquivos))
								.get();
						if (padrao.getCrenca().getMetodosAplicaveis() != null
								&& !padrao.getCrenca().getMetodosAplicaveis().isEmpty()) {
							if (retorno instanceof Boolean && (boolean) retorno == true
									&& padrao.getNome().equals("singleton")) {
								ehAplicavel = true;
							} else if (retorno instanceof Boolean) {
								mapaMetodos = padrao.getCrenca().getMetodosAplicaveis();
								ehAplicavel = true;
							}
						} else {
							ehAplicavel = false;
						}
						if (ehAplicavel) {
							Objeto objetoAnalisado = new Objeto(arquivo.getName().toString(), padrao, mapaMetodos);
							int qtdContemObjeto = 0;
							if (temobjetosIgnoradosAnteriormente) {
								qtdContemObjeto = objetosIgnoradosAnteriormente.stream()
										.filter(l -> l.getClasse().equals(objetoAnalisado.getClasse()))
										.collect(Collectors.toList()).size();
							}
							if ((!temobjetosIgnoradosAnteriormente || qtdContemObjeto == 0)
									&& padrao.getCrenca().getMapaMetodos() != null) {
								listaAplicacao.add(objetoAnalisado);
							} else {
								qtdIgnorados++;
							}
							System.out.println(
									"======== Guardar para montar tabela Pattern - " + padrao.getNome() + " - Classe: "
											+ padrao.getCrenca().getNomeClasse() + " e apresentar ao developer. =====");
							// padrao.getPlano().modificador();
						}
						qtdPadroesVistos++;

					}
					qtdArquivosVistosDiretorio++;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	/**
	 * para fazer a montagem de tabela quando tiver lista de aplicação pega como
	 * variável global.
	 */
	public void montaTabela() {
		if (!listaAplicacao.isEmpty()) {
			System.out.println(
					"======== Abrindo monta tabela - função encontrou " + listaAplicacao.size() + " resultados =====");
			JFrame frame = new JFrame("Refactoring Code");
			JTable tabela = new JTable(new TableModel(listaAplicacao));

			/** colocar uma ordenação ao clicar em cada coluna */
			tabela.setAutoCreateRowSorter(true);

			int qtdLista = listaAplicacao.stream().filter(distinctByKey(p -> p.getClasse())).collect(Collectors.toList()).size();
			
			JScrollPane scroll = new JScrollPane(tabela);
			scroll.setBorder(BorderFactory.createTitledBorder("Select which classes to apply patterns"));

			JButton botao = new JButton("Submit");
			botao.setToolTipText("Click to execute algorithm on file(s) selected");
			botao.addActionListener(new java.awt.event.ActionListener() {
				/**
				 * aplica refatoração as classes e métodos escolhidos
				 */
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					JOptionPane.showMessageDialog(null, "Apply refactoring on project...", "Attention",
							JOptionPane.INFORMATION_MESSAGE);
					/** fecha o jframe aqui pois já clicou em executar. */
					frame.dispose();

					/** seleciona aqui só os objetos que foram checked */
					List<Objeto> objetosModifica = listaAplicacao.stream().filter(l -> l.isAplicado())
							.collect(Collectors.toList());
					if (objetosModifica != null && !objetosModifica.isEmpty()) {
						boolean classeModificada = false;
						for (Objeto objeto : objetosModifica) {
							classeModificada = objeto.getPadrao().getPlano().modificador();
							if (!classeModificada) {
								JOptionPane.showMessageDialog(null, "A classe não foi possível ser modificada !!!",
										"Attention", JOptionPane.ERROR_MESSAGE);
								break;/** para o for para não dar msg em loop para as várias classes selecionadas. */
							}
						}

						/**
						 * caso tenha passado por todas as modificações em true ele da msg de sucesso
						 */
						if (classeModificada) {
							JOptionPane.showMessageDialog(null, "Classes selecionadas modificadas com sucesso !!!",
									"Attention", JOptionPane.INFORMATION_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(null, "You did not have none class checked for modification",
								"Attention", JOptionPane.INFORMATION_MESSAGE);
					}

					List<Objeto> objetosIgnorados = listaAplicacao.stream().filter(l -> l.isAplicado() != true)
							.collect(Collectors.toList());
					System.out.println("-> Ficou com " + objetosIgnorados.size() + " sendo ignorados");
					Objeto.gravarObjetosIgnorados(objetosIgnorados);// grava os objetos que não foram selecionados
					escolhendoPadroes = false;// já escolheu os padrões a serem aplicados
				}
			});

			JLabel label = new JLabel();
			label.setBorder(new EmptyBorder(10, 10, 10, 10));// top,left,bottom,right
			label.setText("<html><body> - <strong>Classes para aplicar Patterns:</strong> " + qtdLista + "<br> - <strong>Quantidade de classes lidas:</strong> " + qtdClassesLidas + "</body></html>");
			frame.add(scroll, BorderLayout.CENTER);
			frame.add(label, BorderLayout.PAGE_START);
			frame.add(botao, BorderLayout.PAGE_END);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(800, 600);
			frame.setVisible(true);
			Image icon = null;

			String workDir = System.getProperty("user.dir");
			if (!workDir.contains("target")) {
				workDir += "/target";
			}
			if (workDir.contains("programa-java")) {
				icon = Toolkit.getDefaultToolkit().getImage(workDir + "/classes/resources/manutencao.jpg");
			} else {
				URL url = System.class.getResource("/resources/manutencao.jpg");
				icon = Toolkit.getDefaultToolkit().getImage(url);
			}
			frame.setIconImage(icon);

			/**
			 * centraliza localização do jframe
			 */
			frame.setLocationRelativeTo(null);

			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					escolhendoPadroes = false;// já escolheu os padrões a serem aplicados
					acabouFecharJanela = true;
					System.out.println(" == Clicou em fechar a janela == ");
				}
			});
		}
	}
}
