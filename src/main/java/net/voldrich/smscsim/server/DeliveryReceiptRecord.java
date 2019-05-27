package net.voldrich.smscsim.server;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.Address;
import net.voldrich.smscsim.spring.auto.SmppSessionManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DeliveryReceiptRecord extends DelayedRecord {
	private final Address sourceAddress;
	private final Address destinationAddress;
	private final long messageId;
	private final DateTime submitDate;

	public DeliveryReceiptRecord(SmppSession session, SubmitSm pduRequest, long messageId) {
		super(session);
		this.sourceAddress = pduRequest.getSourceAddress();
		this.destinationAddress = pduRequest.getDestAddress();
		this.messageId = messageId;
		this.submitDate = new DateTime().withZone(DateTimeZone.UTC);
	}

	public Address getSourceAddress() {
		return sourceAddress;
	}

	public Address getDestinationAddress() {
		return destinationAddress;
	}

	public long getMessageId() {
		return messageId;
	}

	public DateTime getSubmitDate() {
		return submitDate;
	}

	@Override
	public SmppSession getUsedSession(SmppSessionManager sessionManager) {
		String systemId = getSession().getConfiguration().getSystemId();
		return sessionManager.getNextServerSession(systemId);
	}

	@Override
	public PduRequest getRequest(int sequenceNumber) throws Exception {
		return SmppPduUtils.createDeliveryReceipt(this, sequenceNumber);
	}

}
