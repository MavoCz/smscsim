package net.voldrich.smscsim;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.voldrich.smscsim.server.SmscServer;
import net.voldrich.smscsim.spring.auto.SmscGlobalConfiguration;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.cloudhopper.smpp.SmppServerConfiguration;

/**
 * SMSC SMPP simulator which supports:
 * <ul>
 * <li>For each submit request it receives it sends a delivery receipt with configurable delay. </li>
 * <li>Supports listening on multiple ports and multiple applications. </li>
 * <li>Sending of segmented SMPP messages, supports segmentation set via optional parameter or via UDH00 or UDH08</li>
 * <li>Outgoing (MO) messages are sent to connected set of RX and TRX connections with the same application/system ID.
 * RoundRobin is used to rotate between them.</li>
 * <li>Controlling MO messages sent by simulator with JMX commands - start, stop, send message stream and so on</li>
 * </ul>
 *
 * Sample usage: java -Xms32m -Xmx1024m -jar smscsim.jar -ll INFO -p 34567 34568 34569
 *
 * @author Matous Voldrich
 */
public class ServerMain {
	private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
     
    public static final ServerMainParameters PARAMS = new ServerMainParameters();
    
	private List<SmscServer> smppServers = new ArrayList<SmscServer>();
	
	private ClassPathXmlApplicationContext context;

    static public void main(String[] args) throws Exception {    	
    	if (!parseParameters(args)) {
			return;
		}
		LogManager.getRootLogger().setLevel(Level.toLevel(PARAMS.getLogLevel()));
        
    	startServer();
    }
    
	private static boolean parseParameters(String[] args) {
		JCommander jCommander = new JCommander(PARAMS);
		try {
			jCommander.parse(args);
			if (PARAMS.isHelp()) {
				jCommander.usage();
				return false;
			}
		} catch (ParameterException ex) {
			logger.error(ex.toString(), ex);
			jCommander.usage();
			return false;
		}
		return true;
	}
	
	private static void startServer() throws Exception {
		ServerMain server = new ServerMain();
    	server.start();
                
        System.out.println("Press enter to stop SMPP servers");
        System.in.read();
        
        server.stop();
        server.printMetrics();      
        server.destroy();
	}
    
    public ServerMain() {
    	context = new ClassPathXmlApplicationContext("/context.xml");
    	SmscGlobalConfiguration smscConfiguration = context.getBean(SmscGlobalConfiguration.class);
    	for (Integer port : PARAMS.getSmscPortsAsIntegers()) {
    		SmppServerConfiguration serverConfig = context.getBean(SmppServerConfiguration.class); // new configuration instance every time
    		serverConfig.setPort(port); // set this smsc port
    		serverConfig.setJmxDomain("SMSC_" + port); // set this smsc name so it is not in conflict
			SmscServer smscServer = new SmscServer(smscConfiguration, serverConfig);
			smppServers.add(smscServer);
        }        
	}
    
    public void start() throws Exception {
    	logger.info("Starting SMPP servers...");
    	for (SmscServer smppServer: smppServers) {
    		smppServer.start();
    	}
        logger.info("SMPP servers started");
    }
    
    public void stop() throws Exception {
    	logger.info("Stopping SMPP servers...");
    	for (SmscServer smppServer: smppServers) {
    		smppServer.stop();    		
    	}
        logger.info("SMPP servers stopped");
    }
    
    private void destroy() throws Exception {		
		logger.info("Destroying SMPP servers...");
    	for (SmscServer smppServer: smppServers) {
    		smppServer.destroy();    		
    	}
    	logger.info("Destroying Spring context ...");
    	context.close();
    	logger.info("Done destroying");
	}
    
    public void printMetrics() {
    	logger.info("SMPP server metrics");
        for (SmscServer smppServer: smppServers) {
        	smppServer.printMetrics();
        }
    }
    
}
