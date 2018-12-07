package net.voldrich.smscsim.server;

import java.lang.ref.WeakReference;

import net.voldrich.smscsim.spring.DeliveryReceiptScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.voldrich.smscsim.spring.ResponseMessageIdGenerator;
import net.voldrich.smscsim.spring.auto.DelayedRequestSenderImpl;
import net.voldrich.smscsim.spring.auto.SmscGlobalConfiguration;
import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.pdu.Unbind;

public class SmscSmppSessionHandler extends DefaultSmppSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SmscSmppSessionHandler.class);

    private WeakReference<SmppSession> sessionRef;

    private DelayedRequestSenderImpl deliverSender;

    private ResponseMessageIdGenerator messageIdGenerator;

    private DeliveryReceiptScheduler deliveryReceiptScheduler;

    public SmscSmppSessionHandler(SmppServerSession session, SmscGlobalConfiguration config) {
        this.sessionRef = new WeakReference<SmppSession>(session);
        this.deliverSender = config.getDeliverSender();
        this.messageIdGenerator = config.getMessageIdGenerator();
        this.deliveryReceiptScheduler = config.getDeliveryReceiptScheduler();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        SmppSession session = sessionRef.get();

        if (pduRequest instanceof SubmitSm) {
            SubmitSm submitSm = (SubmitSm) pduRequest;
            SubmitSmResp submitSmResp = submitSm.createResponse();
            long messageId = messageIdGenerator.getNextMessageId();
            submitSmResp.setMessageId(FormatUtils.formatAsHex(messageId));
            try {
                // We can not wait in this thread!!
                // It would block handling of other messages and performance would drop drastically!!
                // create and enqueue delivery receipt
                if(submitSm.getRegisteredDelivery() > 0 && deliverSender != null && submitSm.getSourceAddress().getAddress().equals("TEST")) {
                    logger.info("Source address is: 'TEST', responding with failed delivery receipt");
                    FailedDeliveryReceiptRecord record = new FailedDeliveryReceiptRecord(session, submitSm, messageId);
                    record.setDeliverTime(deliveryReceiptScheduler.getDeliveryTimeMillis());
                    deliverSender.scheduleDelivery(record);
                } else if (submitSm.getRegisteredDelivery() > 0 && deliverSender != null) {
                    DeliveryReceiptRecord record = new DeliveryReceiptRecord(session, submitSm, messageId);
                    record.setDeliverTime(deliveryReceiptScheduler.getDeliveryTimeMillis());
                    deliverSender.scheduleDelivery(record);
                }
            } catch (Exception e) {
                logger.error("Error when handling submit", e);
            }

            //submitSmResp.setCommandStatus(SmppConstants.STATUS_X_T_APPN);
            return submitSmResp;
            //return null;
        } else if (pduRequest instanceof Unbind) {
            //session.destroy();  
            // TO DO refine, this throws exceptions
            session.unbind(1000);
            return pduRequest.createResponse();
        }

        return pduRequest.createResponse();
    }

    @Override
    public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
        if (pduAsyncResponse.getResponse().getCommandStatus() != SmppConstants.STATUS_OK) {
            // TODO
            // error, resend the request again?
            //pduAsyncResponse.getRequest().setReferenceObject(value)
        }
    }

}
