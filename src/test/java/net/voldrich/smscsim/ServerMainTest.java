package net.voldrich.smscsim;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import net.voldrich.smscsim.server.SmscServer;
import net.voldrich.smscsim.spring.auto.SmscGlobalConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.Semaphore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
public class ServerMainTest {

    private static final Logger logger = LoggerFactory.getLogger(ServerMainTest.class);

    private static final int PORT = 12345;
    private static final String SYSTEM_ID = "132456";
    private static final String SYSTEM_ID_2 = "789798";

    private static final int NUMBER_OF_SUBMITS = 20;
    private static final int NUMBER_OF_SUBMITS_2 = 50;

    @Autowired
    private ApplicationContext context;

    private SmscServer smscServer;

    @Before
    public void before() throws SmppChannelException {
        SmscGlobalConfiguration smscConfiguration = context.getBean(SmscGlobalConfiguration.class);
        SmppServerConfiguration serverConfig = context.getBean(SmppServerConfiguration.class); // new configuration instance every time
        serverConfig.setPort(PORT); // set this smsc port
        serverConfig.setJmxDomain("SMSC_" + PORT); // set this smsc name so it is not in conflict
        smscServer = new SmscServer(smscConfiguration, serverConfig);
        smscServer.start();
    }

    @After
    public void after() {
        try {
            smscServer.stop();
            smscServer.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Connect 1 session
     * Send number of submit sm messages
     * Check that correct delivery receipt count is delivered to back to session.
     **/
    @Test
    public void testSubmitsAndDeliveryReceipts() throws Exception {
        SmppClient client = new SmppClient("localhost", PORT, SYSTEM_ID);
        BlockingSmppSessionHandler handler = new BlockingSmppSessionHandler();
        SmppSession session = client.connect(handler);

        for (int i=0; i<NUMBER_OF_SUBMITS; i++) {
            session.sendRequestPdu(createSubmitWithRegisteredDelivery(), 1000, false);
        }

        handler.blockUntilReceived(NUMBER_OF_SUBMITS, NUMBER_OF_SUBMITS);

        session.close();
    }

    /**
     * Connect 2 sessions
     * Send different message count from each one
     * Check that correct delivery receipt count is delivered to each session.
     **/
    @Test
    public void testSubmitsAndDeliveryReceipts2() throws Exception {
        SmppClient client1 = new SmppClient("localhost", PORT, SYSTEM_ID);
        BlockingSmppSessionHandler handler1 = new BlockingSmppSessionHandler();
        SmppSession session1 = client1.connect(handler1);

        for (int i=0; i<NUMBER_OF_SUBMITS; i++) {
            session1.sendRequestPdu(createSubmitWithRegisteredDelivery(), 1000, false);
        }

        SmppClient client2 = new SmppClient("localhost", PORT, SYSTEM_ID_2);
        BlockingSmppSessionHandler handler2 = new BlockingSmppSessionHandler();
        SmppSession session2 = client2.connect(handler2);

        for (int i=0; i<NUMBER_OF_SUBMITS_2; i++) {
            session2.sendRequestPdu(createSubmitWithRegisteredDelivery(), 1000, false);
        }

        handler1.blockUntilReceived(NUMBER_OF_SUBMITS, NUMBER_OF_SUBMITS);
        handler2.blockUntilReceived(NUMBER_OF_SUBMITS_2, NUMBER_OF_SUBMITS_2);
    }

    private SubmitSm createSubmitWithRegisteredDelivery() throws SmppInvalidArgumentException {
        SubmitSm submitSm = new SubmitSm();
        submitSm.setDestAddress(new Address((byte)0,(byte)0, "123456789"));
        submitSm.setSourceAddress(new Address((byte) 0, (byte) 0, "987654321"));
        String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
        submitSm.setShortMessage(CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM));
        submitSm.setRegisteredDelivery((byte)1);
        return submitSm;
    }

    /**
     * Simple session handler which enables waiting on specific response / deliver sm count by blocking on semaphore.
     **/
    public static class BlockingSmppSessionHandler extends DefaultSmppSessionHandler {

        private final Semaphore responseSem = new Semaphore(0);
        private final Semaphore deliverSem = new Semaphore(0);

        public BlockingSmppSessionHandler() {
            super(logger);
        }

        @Override
        public void firePduRequestExpired(PduRequest pduRequest) {
            logger.warn("PDU request expired: {}", pduRequest);
        }

        @Override
        public PduResponse firePduRequestReceived(PduRequest pduRequest) {
            if (pduRequest instanceof DeliverSm) {
                logger.info("DeliverSm received: {}", pduRequest);
                deliverSem.release();
            } else {
                logger.warn("Unexpected message received: {}", pduRequest);
            }
            PduResponse response = pduRequest.createResponse();

            return response;
        }

        @Override
        public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
            if (pduAsyncResponse.getResponse().getCommandStatus() == SmppConstants.STATUS_OK) {
                responseSem.release();
            }
        }

        public void blockUntilReceived(int expectedResponses, int expectedDeliverSm) throws InterruptedException {
            logger.info("Waiting for responses");
            responseSem.acquire(expectedResponses);
            logger.info("All responses received, waiting for delivers");
            deliverSem.acquire(expectedDeliverSm);
            logger.info("All delivers received");
        }
    }

}