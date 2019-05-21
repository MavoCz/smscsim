package net.voldrich.smscsim.server;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.util.DeliveryReceipt;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class FailedDeliveryReceiptRecord extends DeliveryReceiptRecord {

    public FailedDeliveryReceiptRecord(SmppSession session, SubmitSm pduRequest, long messageId) {
        super(session, pduRequest, messageId);
    }

    // rip-off from the above method while I don't have a good understanding of SMPP & SMS
    @Override
    public PduRequest getRequest(int sequenceNumber) throws Exception {
        DeliverSm pdu0 = new DeliverSm();

        pdu0.setSequenceNumber(sequenceNumber);
        pdu0.setSourceAddress(getSourceAddress());
        pdu0.setDestAddress(getDestinationAddress());
        pdu0.setEsmClass((byte) 0x04);
        pdu0.setProtocolId((byte) 0x00);
        pdu0.setPriority((byte) 0x00);
        pdu0.setScheduleDeliveryTime(null);
        pdu0.setValidityPeriod(null);
        pdu0.setRegisteredDelivery((byte) 0x00);
        pdu0.setReplaceIfPresent((byte) 0x00);
        pdu0.setDataCoding((byte) 0x00);
        pdu0.setDefaultMsgId((byte) 0x00);

        DeliveryReceipt deliveryReceipt = new DeliveryReceipt(FormatUtils.formatAsHex(getMessageId()), 1, 1,
                getSubmitDate(), new DateTime().withZone(DateTimeZone.UTC), SmppConstants.STATE_REJECTED, "500", "-");
        String shortMessage = deliveryReceipt.toShortMessage();
        pdu0.setShortMessage(CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_GSM));

        // order is important
        // pdu0.addOptionalParameter(new
        // Tlv(SmppConstants.TAG_SOURCE_NETWORK_TYPE, new byte[] { (byte)0x01
        // }));
        // pdu0.addOptionalParameter(new
        // Tlv(SmppConstants.TAG_DEST_NETWORK_TYPE, new byte[] { (byte)0x01 }));
        // NOTE: VERY IMPORTANT -- THIS IS A C-STRING!
        pdu0.addOptionalParameter(new Tlv(SmppConstants.TAG_RECEIPTED_MSG_ID, SmppPduUtils.convertOptionalStringToCOctet(FormatUtils.formatAsHex(getMessageId()))));
        pdu0.addOptionalParameter(new Tlv(SmppConstants.TAG_MSG_STATE, new byte[] { SmppConstants.STATE_DELIVERED }));

        pdu0.calculateAndSetCommandLength();

        return pdu0;
    }
}
