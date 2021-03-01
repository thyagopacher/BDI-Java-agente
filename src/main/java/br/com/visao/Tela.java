package br.com.visao;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import br.com.agente.Main;
import fr.jcgay.notification.Application;
import fr.jcgay.notification.Icon;
import fr.jcgay.notification.Notification;
import fr.jcgay.notification.Notifier;
import fr.jcgay.notification.SendNotification;

public class Tela {

	Properties propriedades = System.getProperties(); 
	Main mAgente = new Main();
	private static String caminhoIcone = "";
	private static Image icon = null;
	private static Icon iconeNotificacao = null;
	
	Tela(){
		this.icone();
	}
	
    private static Application application() {
        return Application.builder()
            .id("application/x-vnd-jcgay.send-notification").name("Send Notification").icon(icon()).build();
    }
    
    private static Icon icon() {
    	if(iconeNotificacao == null) {
	    	try {
	    		URL url = Tela.class.getResource("/resources/manutencao.jpg");
	    		return Icon.create(url, "send-notification-cli");
	    	}catch(Exception ex) {
	    		System.err.println(ex.getMessage());
	    	}
    	}
    	return iconeNotificacao;
    }    
    
  /**
   * previamente pegando imagem do icone
   * */
    private Image icone() {
    	if(icon == null) {
			String workDir = System.getProperty("user.dir");
			if(!workDir.contains("target")) {
				workDir += "/target";
			}
			if(workDir.contains("programa-java")) {
				caminhoIcone = workDir + "/classes/resources/manutencao.jpg";
				icon = Toolkit.getDefaultToolkit().getImage(caminhoIcone);
			}else {
				caminhoIcone = "/resources/manutencao.jpg";
				URL url = System.class.getResource(caminhoIcone);
				icon = Toolkit.getDefaultToolkit().getImage(url);
			}
    	}
		return icon;
    }    
    
	public void montaTela() {

		String textoTela = "<h3 style='text-align: left;margin: 0; padding: 0;'>AGENTE INICIADO:</h3>";
		textoTela += "<ol>";
		textoTela += "<li><strong>Sistema Operacional:</strong> " + propriedades.get("os.name") + "</li>";
		textoTela += "<li><strong>Idioma Sistema:</strong> " + propriedades.get("user.language") + "</li>";
		textoTela += "</ol>";
		JLabel label1 = new JLabel("<html><div style='text-align: left;'>"+textoTela+"</div></html>");
		label1.setSize(1000, 100);
		JPanel panel = new JPanel();
		panel.add(label1, BorderLayout.WEST);
		
		JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(50, 30, 300, 50);
        
		/** jframe para controle de ações e onde o agente irá escanear as pastas */
		JFrame jAgente = new JFrame("Agente");
		jAgente.add(scrollPane);
		jAgente.setVisible(false);
		jAgente.dispatchEvent(new WindowEvent(jAgente, WindowEvent.WINDOW_ICONIFIED));
		jAgente.setSize(340, 120);
		jAgente.setResizable(false);
		jAgente.setLocationRelativeTo(null);
		jAgente.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		jAgente.setTitle("Ferramenta para refatoração de código com Designer Patterns");

		jAgente.setIconImage(this.icone());
	
		if (SystemTray.isSupported()) {

			
			/** responsável pela área de icones ocultos */
			SystemTray tray = SystemTray.getSystemTray();

			/** declaração de um icone para adicionar */
			
			TrayIcon trayIcon = new TrayIcon(icon, "Refatoração");
			trayIcon.setToolTip("Estamos analisando seu código fonte, clique aqui para ver mais detalhes...");
			trayIcon.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					System.out.println("Clicou no botão de número: " + e.getButton());
					if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
						jAgente.setVisible(true);
					}else if(e.getButton() == MouseEvent.BUTTON3) {
						/**clique com botão direito faz abrir popup com botão para fechar*/
						JPopupMenu popup = new JPopupMenu();
						
						JMenuItem menu1 = new JMenuItem("Sair");
						menu1.addMouseListener(new MouseAdapter() {
							public void mouseClicked(MouseEvent eItem1) {
								if (eItem1.getClickCount() == 1) {
									jAgente.dispose();
									tray.remove(trayIcon);
									popup.setVisible(false);
									mAgente.pararAgente();
								}
							}
						});
						popup.add(menu1);
						popup.show(null, e.getX(), e.getY() - 20);
					}
				}
			});
			 
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				JOptionPane.showMessageDialog(null,
						"Erro - ao adicionar icone em barra - causado por:" + e.getMessage(), "Erro",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					Notifier notificao = new SendNotification().setApplication(application()).initNotifier();
			        Notification.Builder notification = Notification.builder()
			                .title("Atenção")
			                .icon(icon())
			                .message("Análise de refatoração iniciada no código fonte...");
			        notificao.send(notification.build());		        
				}
			});
			t.start();			
			
            System.err.println("SystemTray not supported");
        }
	}

	public static void main(String[] args) {
		/**um caminho setado primariamente para testar o main*/
		String caminho = "C:\\programa-java\\projetos-para-testar-refatoracao\\cadastro\\almoxarifado-master";
		if(args.length > 0) {
			/**caso tenha passado por parametro ele pega o caminho*/
			caminho = args[0];
		}
		Tela t = new Tela();
		t.mAgente.caminho = caminho;
		t.mAgente.iniciaAgenteFrame();		
		t.montaTela();
	}
}
