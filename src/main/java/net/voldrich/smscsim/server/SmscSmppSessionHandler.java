package net.voldrich.smscsim.server;

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
import java.lang.ref.WeakReference;
import java.util.UUID;
import net.voldrich.smscsim.spring.DeliveryReceiptScheduler;
import net.voldrich.smscsim.spring.auto.DelayedRequestSenderImpl;
import net.voldrich.smscsim.spring.auto.SmscGlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmscSmppSessionHandler extends DefaultSmppSessionHandler {

  private static final Logger logger = LoggerFactory.getLogger(SmscSmppSessionHandler.class);

  private WeakReference<SmppSession> sessionRef;

  private DelayedRequestSenderImpl deliverSender;

  private DeliveryReceiptScheduler deliveryReceiptScheduler;

  public SmscSmppSessionHandler(SmppServerSession session, SmscGlobalConfiguration config) {
    this.sessionRef = new WeakReference<SmppSession>(session);
    this.deliverSender = config.getDeliverSender();
    this.deliveryReceiptScheduler = config.getDeliveryReceiptScheduler();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public PduResponse firePduRequestReceived(PduRequest pduRequest) {
    SmppSession session = sessionRef.get();

    if (pduRequest instanceof SubmitSm) {
      SubmitSm submitSm = (SubmitSm) pduRequest;
      SubmitSmResp submitSmResp = submitSm.createResponse();
      String messageId = UUID.randomUUID().toString();
      submitSmResp.setMessageId(messageId);

      try {
        // We can not wait in this thread!!
        // It would block handling of other messages and performance would drop drastically!!
        // create and enqueue delivery receipt
        if (submitSm.getRegisteredDelivery() > 0 && deliverSender != null) {
          String sourceAddress = submitSm.getSourceAddress().getAddress();
          DeliveryReceiptRecord record;
          if (sourceAddress.matches("TEST\\d\\d")) {
            record = chanceDelivery(session, submitSm, sourceAddress, messageId);
          } else if (sourceAddress.matches("ERROR\\d\\d\\d")) {
            record = fakeErrorDelivery(session, submitSm, sourceAddress, messageId);
          } else {
            record = new DeliveryReceiptRecord(session, submitSm, messageId);
          }
          record.setDeliverTime(deliveryReceiptScheduler.getDeliveryTimeMillis());
          deliverSender.scheduleDelivery(record);
        }
      } catch (Exception e) {
        logger.error("Error when handling submit", e);
      }

      // submitSmResp.setCommandStatus(SmppConstants.STATUS_X_T_APPN);
      return submitSmResp;
      // return null;
    } else if (pduRequest instanceof Unbind) {
      // session.destroy();
      // TO DO refine, this throws exceptions
      session.unbind(1000);
      return pduRequest.createResponse();
    }

    return pduRequest.createResponse();
  }


  private DeliveryReceiptRecord fakeErrorDelivery(SmppSession session, SubmitSm submitSm,
      String sourceAddress, String messageId) {
    String errorCode = sourceAddress.replaceAll("ERROR", "");
    return new FailedDeliveryReceiptRecord(session, submitSm, messageId, errorCode);
  }

  private DeliveryReceiptRecord chanceDelivery(SmppSession session, SubmitSm submitSm,
      String sourceAddress, String messageId) {
    double successRate = Double.parseDouble(sourceAddress.replaceAll("TEST", ""));
    double rng = Math.random() * 100;
    if (rng <= successRate) {
      return new DeliveryReceiptRecord(session, submitSm, messageId);
    } else {
      return new FailedDeliveryReceiptRecord(session, submitSm, messageId);
    }
  }

  @Override
  public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
    if (pduAsyncResponse.getResponse().getCommandStatus() != SmppConstants.STATUS_OK) {
      // TODO
      // error, resend the request again?
      // pduAsyncResponse.getRequest().setReferenceObject(value)
    }
  }
}
