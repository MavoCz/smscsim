package net.voldrich.smscsim.spring.message;

import com.cloudhopper.smpp.pdu.PduRequest;

public interface MessageFactory<T extends PduRequest> {
	T createMessage() throws Exception;
}
