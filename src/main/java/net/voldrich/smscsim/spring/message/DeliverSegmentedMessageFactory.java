package net.voldrich.smscsim.spring.message;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.pdu.DeliverSm;
import net.voldrich.smscsim.server.SmppPduUtils;
import net.voldrich.smscsim.spring.BaseSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Not thread safe, must be used by one thread only
 **/
public class DeliverSegmentedMessageFactory extends DeliverBaseMessageFactory {

	protected static final Logger logger = LoggerFactory.getLogger(BaseSender.class);

	private int nextMsgRefNum = 1;

	private int numberOfSegments = 10;

	private int nextSegmentId = 1;

	private int currentMsgRefNum = 1;

	@Override
	public DeliverSm createMessage() throws Exception {
		if (nextSegmentId > numberOfSegments) {
			// we are starting a new segmented message
			currentMsgRefNum = ++nextMsgRefNum;
			nextSegmentId = 1;
		}

		DeliverSm pdu = super.createMessage();

		String shortMessage = "Segment content " + nextSegmentId + ". ";
		SmppPduUtils.setSegmentOptionalParams(pdu, currentMsgRefNum, nextSegmentId, numberOfSegments);
		pdu.setShortMessage(CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_GSM));

		nextSegmentId += 1;
		return pdu;
	}

	public int getNumberOfSegments() {
		return numberOfSegments;
	}

	public void setNumberOfSegments(int numberOfSegments) {
		this.numberOfSegments = numberOfSegments;
	}
}
