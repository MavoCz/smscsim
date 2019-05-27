package net.voldrich.smscsim.spring.auto;

import net.voldrich.smscsim.spring.DeliveryReceiptScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmscGlobalConfiguration {

  @Autowired private DelayedRequestSenderImpl deliverSender;

  @Autowired private SmppSessionManager sessionManager;

  @Autowired private DeliveryReceiptScheduler deliveryReceiptScheduler;

  public DelayedRequestSenderImpl getDeliverSender() {
    return deliverSender;
  }

  public void setDeliverSender(DelayedRequestSenderImpl deliverSender) {
    this.deliverSender = deliverSender;
  }

  public SmppSessionManager getSessionManager() {
    return sessionManager;
  }

  public void setSessionManager(SmppSessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  public DeliveryReceiptScheduler getDeliveryReceiptScheduler() {
    return deliveryReceiptScheduler;
  }

  public void setDeliveryReceiptScheduler(DeliveryReceiptScheduler deliveryReceiptScheduler) {
    this.deliveryReceiptScheduler = deliveryReceiptScheduler;
  }
}
