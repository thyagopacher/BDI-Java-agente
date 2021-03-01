package br.com.agente;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.SUtil;
import jadex.commons.future.IFuture;

public class Main {

	public String sistemaOperacional = System.getProperty("os.name");
	public String versaoSistema = System.getProperty("os.version");
	public String arquiteturaSistema = System.getProperty("os.arch");

	/**
	 * define o caminho ao qual vai verificar o projeto
	 * */
	public String caminho;

	/**
	 * variável responsável por controlar a plataforma do jadex
	 */
	private IExternalAccess platform;
	private IFuture<IComponentManagementService> fut;
	
	/**
	 * contém a inicialização do Agente e também do JFrame para configurações
	 */
	public void iniciaAgenteFrame() {
		try {
			/**
			 * para fazer a inicialização do agente 
			 */
			PlatformConfiguration platformConfig = PlatformConfiguration.getDefaultNoGui();
			platformConfig.setGui(false);
			platformConfig.setWelcome(false);
			platformConfig.setPrintPass(false);
			platformConfig.setUsePass(false);
			platformConfig.setLogging(false);
			platformConfig.setDebugFutures(true);
			platformConfig.setAddress(true);
			platformConfig.setAsyncExecution(true);
			platformConfig.setAutoShutdown(true);

			platformConfig.setKernels(PlatformConfiguration.KERNEL.micro,
					PlatformConfiguration.KERNEL.component, PlatformConfiguration.KERNEL.v3);			
			
			/** Definir mecanismos de conscientização */
			platformConfig.setAwaMechanisms(PlatformConfiguration.AWAMECHANISM.broadcast,
					PlatformConfiguration.AWAMECHANISM.relay);

			/** faz inicialização da plataforma e da retorno */
			platform = Starter.createPlatform(platformConfig).get();

			fut = SServiceProvider.getService(platform, IComponentManagementService.class);
			IComponentManagementService cms = fut.get();
			CreationInfo ci = new CreationInfo(
					SUtil.createHashMap(new String[] { "caminho" }, new Object[] { caminho }));
			cms.createComponent("agente-refatoracao", "br.com.agente.RefatoracaoBDI.class", ci);			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * ele mata a plataforma de agente do jadex
	 */
	public void pararAgente() {
		platform.killComponent();
	}

	public static void main(String[] args) {
			Main m = new Main();
			m.iniciaAgenteFrame();
	}
}
