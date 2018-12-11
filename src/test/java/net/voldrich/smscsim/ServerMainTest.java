package net.voldrich.smscsim;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.*;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.cloudhopper.smpp.util.DeliveryReceipt;
import com.cloudhopper.smpp.util.DeliveryReceiptException;
import net.voldrich.smscsim.server.SmscServer;
import net.voldrich.smscsim.server.SmscSmppSessionHandler;
import net.voldrich.smscsim.spring.auto.RandomDeliveryReceiptScheduler;
import net.voldrich.smscsim.spring.auto.SmscGlobalConfiguration;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
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
    private SmscGlobalConfiguration smscConfiguration;

    @Before
    public void before() throws SmppChannelException {
        smscConfiguration = context.getBean(SmscGlobalConfiguration.class);
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
            session.sendRequestPdu(createSubmitWithRegisteredDelivery("987654321"), 1000, false);
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
            session1.sendRequestPdu(createSubmitWithRegisteredDelivery("987654321"), 1000, false);
        }

        SmppClient client2 = new SmppClient("localhost", PORT, SYSTEM_ID_2);
        BlockingSmppSessionHandler handler2 = new BlockingSmppSessionHandler();
        SmppSession session2 = client2.connect(handler2);

        for (int i=0; i<NUMBER_OF_SUBMITS_2; i++) {
            session2.sendRequestPdu(createSubmitWithRegisteredDelivery("987654321"), 1000, false);
        }

        handler1.blockUntilReceived(NUMBER_OF_SUBMITS, NUMBER_OF_SUBMITS);
        handler2.blockUntilReceived(NUMBER_OF_SUBMITS_2, NUMBER_OF_SUBMITS_2);
    }

    /**
     * Success rate is 0
     */
    @Test
    public void testAlwaysFailedDelivery() throws Exception {
        SmppClient client = new SmppClient("localhost", PORT, SYSTEM_ID);
        BlockingAndTallyingDeliveryReceiptSessionHandler handler = new BlockingAndTallyingDeliveryReceiptSessionHandler(smscServer.getSessionManager().getNextServerSession() ,smscConfiguration);
        SmppSession session = client.connect(handler);
        int totalMessagesToSendSMSC = 20;
        for(int i = 0; i<totalMessagesToSendSMSC; i++) {
            session.sendRequestPdu(createSubmitWithRegisteredDelivery("TEST00"), 1000, false);
        }

        handler.blockUntilDelivered(totalMessagesToSendSMSC);
        session.close();

        Assert.assertEquals(handler.capturedDeliverSmList.size(), totalMessagesToSendSMSC);
        Assert.assertEquals(handler.failedDeliverSmList.size(), totalMessagesToSendSMSC);
        Assert.assertEquals(handler.successfulDeliverSmList.size(), 0);
    }

    /**
     * Source address does not match pattern specified
     */
    @Test
    public void testChancedBaseSucessButDidNotFollowFormat() throws Exception {
        SmppClient client = new SmppClient("localhost", PORT, SYSTEM_ID);
        BlockingAndTallyingDeliveryReceiptSessionHandler handler = new BlockingAndTallyingDeliveryReceiptSessionHandler(smscServer.getSessionManager().getNextServerSession() ,smscConfiguration);
        SmppSession session = client.connect(handler);
        int totalMessagesToSendSMSC = 20;
        for(int i = 0; i<totalMessagesToSendSMSC; i++) {
            session.sendRequestPdu(createSubmitWithRegisteredDelivery("TSET01"), 1000, false);
        }

        handler.blockUntilDelivered(totalMessagesToSendSMSC);
        session.close();

        Assert.assertEquals(handler.capturedDeliverSmList.size(), totalMessagesToSendSMSC);
        Assert.assertEquals(handler.failedDeliverSmList.size(), 0);
        Assert.assertEquals(handler.successfulDeliverSmList.size(), totalMessagesToSendSMSC);
    }

    /**
     * Success rate is 40% Randomness is tested by using Pearson's chi square test (goodness of fit)
     * repeats 1000 sample by 5 times, 4 of 5 must succeed
     * https://www.khanacademy.org/math/statistics-probability/inference-categorical-data-chi-square-tests/chi-square-goodness-of-fit-tests/v/pearson-s-chi-square-test-goodness-of-fit
     */
    @Test
    public void testChancedDelivery() throws Exception {
        SmppClient client = new SmppClient("localhost", PORT, SYSTEM_ID);

        // decrease delay for faster run time
        RandomDeliveryReceiptScheduler randomDeliveryReceiptScheduler = (RandomDeliveryReceiptScheduler) smscConfiguration.getDeliveryReceiptScheduler();
        randomDeliveryReceiptScheduler.setMinDelayMs(0);
        randomDeliveryReceiptScheduler.setRandomDeltaMs(0);

        BlockingAndTallyingDeliveryReceiptSessionHandler handler = new BlockingAndTallyingDeliveryReceiptSessionHandler(smscServer.getSessionManager().getNextServerSession() ,smscConfiguration);
        SmppSession session = client.connect(handler);
        int successCase = 0;

        for(int i=0;i<5;i++) {
            int totalMessagesToSendSMSC = 1000;
            for (int j = 0; j < totalMessagesToSendSMSC; j++) {
                session.sendRequestPdu(createSubmitWithRegisteredDelivery("TEST40"), 1000, false);
            }

            handler.blockUntilDelivered(totalMessagesToSendSMSC);

            Assert.assertEquals(handler.capturedDeliverSmList.size(), totalMessagesToSendSMSC);

            float chiSquare = ((handler.successfulDeliverSmList.size() - 400) * (handler.successfulDeliverSmList.size() - 400)) / 400f;
            chiSquare += ((handler.failedDeliverSmList.size() - 600) * (handler.failedDeliverSmList.size() - 600)) / 600f;

            System.out.println("==== case"+(i+1)+" ====");
            System.out.println("failed: " + handler.failedDeliverSmList.size());
            System.out.println("success: " + handler.successfulDeliverSmList.size());
            System.out.println("total: " + handler.capturedDeliverSmList.size());
            System.out.println("chi: " + chiSquare);

            // the chiSquare must not go beyond 3.84. Using 5% significant difference and degrees of freedom of 1
            if(chiSquare <= 3.84) {
                successCase++;
            }

            handler.capturedDeliverSmList.clear();
            handler.successfulDeliverSmList.clear();
            handler.failedDeliverSmList.clear();
            handler.deliverSem = new Semaphore(0);
        }
        session.close();
        Assert.assertTrue("4 of 5 cases must pass chi-square test", successCase >= 4);
    }

    private SubmitSm createSubmitWithRegisteredDelivery(String sourceAddress) throws SmppInvalidArgumentException {
        SubmitSm submitSm = new SubmitSm();
        submitSm.setDestAddress(new Address((byte)0,(byte)0, "123456789"));
        submitSm.setSourceAddress(new Address((byte) 0, (byte) 0, sourceAddress));
        String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
        submitSm.setShortMessage(CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM));
        submitSm.setRegisteredDelivery((byte)1);
        return submitSm;
    }

    public static class BlockingAndTallyingDeliveryReceiptSessionHandler extends SmscSmppSessionHandler {

        private List<DeliverSm> capturedDeliverSmList = new ArrayList<>();
        private List<DeliverSm> successfulDeliverSmList = new ArrayList<>();
        private List<DeliverSm> failedDeliverSmList = new ArrayList<>();

        private Semaphore deliverSem = new Semaphore(0);

        public BlockingAndTallyingDeliveryReceiptSessionHandler(SmppServerSession session, SmscGlobalConfiguration config) {
            super(session, config);
        }

        @Override
        public PduResponse firePduRequestReceived(PduRequest pduRequest) {
            if (pduRequest instanceof DeliverSm) {
                logger.info("DeliverSm received: {}", pduRequest);
                capturedDeliverSmList.add((DeliverSm) pduRequest);

                String decodedShortMessage = CharsetUtil.decode(((DeliverSm) pduRequest).getShortMessage(), CharsetUtil.CHARSET_GSM);
                try {
                    DeliveryReceipt deliveryReceipt = DeliveryReceipt.parseShortMessage(decodedShortMessage, DateTimeZone.UTC);
                    if(deliveryReceipt.getState() != SmppConstants.STATE_DELIVERED) {
                        failedDeliverSmList.add((DeliverSm) pduRequest);
                    } else {
                        successfulDeliverSmList.add((DeliverSm) pduRequest);
                    }
                } catch (DeliveryReceiptException e) {
                    throw new RuntimeException(e);
                }

                deliverSem.release();
            } else {
                logger.warn("Unexpected message received: {}", pduRequest);
            }
            PduResponse response = pduRequest.createResponse();

            return response;
        }

        public void blockUntilDelivered(int expectedDeliverSm) throws InterruptedException {
            deliverSem.acquire(expectedDeliverSm);
            logger.info("All delivers received");
        }
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