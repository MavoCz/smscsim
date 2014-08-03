package net.voldrich.smscsim.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.voldrich.smscsim.spring.auto.SmscGlobalConfiguration;
import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;

public class SmscServer {
	private static final Logger logger = LoggerFactory.getLogger(SmscServer.class);

	private DefaultSmppServer smppServer;
	
	public SmscServer(SmscGlobalConfiguration config, SmppServerConfiguration serverConfig) {
		smppServer = new DefaultSmppServer(
        		serverConfig, 
        		new SmscSmppServerHandler(config), 
        		config.getThreadPool().getExecutor(), 
        		config.getThreadPool().getMonitorExecutor());		
	}
	
	
	public void destroy() throws Exception {
		smppServer.destroy();
		smppServer = null;
	}
	
	public void start() throws SmppChannelException {
		smppServer.start();
	}
	
	public void stop() {
		smppServer.stop();
	}


	public void printMetrics() {
		logger.info("Server {} counters: {}", smppServer.getConfiguration().getJmxDomain(), smppServer.getCounters());
	}

}
