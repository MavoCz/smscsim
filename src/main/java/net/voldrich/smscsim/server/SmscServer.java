package net.voldrich.smscsim.server;

import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import net.voldrich.smscsim.spring.auto.SmppSessionManager;
import net.voldrich.smscsim.spring.auto.SmscGlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instance of SMSC server listening on one port Each instance os SMSC server has its own executors
 * This is because SMPP library shutdowns the executors when stopped
 */
public class SmscServer {
  private static final Logger logger = LoggerFactory.getLogger(SmscServer.class);

  private DefaultSmppServer smppServer;
  private SmscGlobalConfiguration config;

  public SmscServer(SmscGlobalConfiguration config, SmppServerConfiguration serverConfig) {
    this(config, serverConfig, new SmscServerThreadPoolFactory());
  }

  public SmscServer(
      SmscGlobalConfiguration config,
      SmppServerConfiguration serverConfig,
      SmscServerThreadPoolFactory threadPoolFactory) {
    this.config = config;
    smppServer =
        new DefaultSmppServer(
            serverConfig,
            new SmscSmppServerHandler(config),
            threadPoolFactory.createMainExecutor(),
            threadPoolFactory.createMonitorExecutor());
  }

  public SmppSessionManager getSessionManager() {
    return config.getSessionManager();
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
    logger.info(
        "Server {} counters: {}",
        smppServer.getConfiguration().getJmxDomain(),
        smppServer.getCounters());
  }
}
