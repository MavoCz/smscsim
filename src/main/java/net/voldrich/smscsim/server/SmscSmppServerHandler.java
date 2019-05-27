package net.voldrich.smscsim.server;

import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import net.voldrich.smscsim.spring.auto.SmppSessionManager;
import net.voldrich.smscsim.spring.auto.SmscGlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmscSmppServerHandler implements SmppServerHandler {

  private static final Logger logger = LoggerFactory.getLogger(SmscSmppServerHandler.class);

  private SmppSessionManager sessionManager;

  private SmscGlobalConfiguration config;

  public SmscSmppServerHandler(SmscGlobalConfiguration config) {
    this.config = config;
    this.sessionManager = config.getSessionManager();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void sessionBindRequested(
      Long sessionId, SmppSessionConfiguration sessionConfiguration, final BaseBind bindRequest)
      throws SmppProcessingException {
    // test name change of sessions
    // this name actually shows up as thread context....
    sessionConfiguration.setName("Application.SMPP." + sessionConfiguration.getSystemId());
  }

  @Override
  public void sessionCreated(
      Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse)
      throws SmppProcessingException {
    logger.info("Session created: {}", session);
    // need to do something it now (flag we're ready)
    // we need to create one session handler instance per session
    SmscSmppSessionHandler smppSessionHandler = new SmscSmppSessionHandler(session, config);
    session.serverReady(smppSessionHandler);
    sessionManager.addServerSession(session);
  }

  @Override
  public void sessionDestroyed(Long sessionId, SmppServerSession session) {
    logger.info("Session destroyed: {}", session);
    // print out final stats
    if (session.hasCounters()) {
      logger.info(" final session rx-submitSM: {}", session.getCounters().getRxSubmitSM());
    }

    sessionManager.removeServerSession(session);
    // make sure it's really shutdown
    session.destroy();
  }
}
