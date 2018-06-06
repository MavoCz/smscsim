package net.voldrich.smscsim.server;

import java.nio.ByteBuffer;

import org.joda.time.DateTime;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.cloudhopper.smpp.util.DeliveryReceipt;

public class SmppPduUtils {

	public static DeliverSm createDeliveryReceipt(DeliveryReceiptRecord deliveryReceiptRecord, int sequenceNumber) throws Exception {
		DeliverSm pdu0 = new DeliverSm();

		pdu0.setSequenceNumber(sequenceNumber);
		pdu0.setSourceAddress(deliveryReceiptRecord.getSourceAddress());
		pdu0.setDestAddress(deliveryReceiptRecord.getDestinationAddress());
		pdu0.setEsmClass((byte) 0x04);
		pdu0.setProtocolId((byte) 0x00);
		pdu0.setPriority((byte) 0x00);
		pdu0.setScheduleDeliveryTime(null);
		pdu0.setValidityPeriod(null);
		pdu0.setRegisteredDelivery((byte) 0x00);
		pdu0.setReplaceIfPresent((byte) 0x00);
		pdu0.setDataCoding((byte) 0x00);
		pdu0.setDefaultMsgId((byte) 0x00);

		DeliveryReceipt deliveryReceipt = new DeliveryReceipt(FormatUtils.formatAsDec(deliveryReceiptRecord.getMessageId()), 1, 1,
				deliveryReceiptRecord.getSubmitDate(), new DateTime(), SmppConstants.STATE_DELIVERED, "000", "-");
		String shortMessage = deliveryReceipt.toShortMessage();
		pdu0.setShortMessage(CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_GSM));

		// order is important
		// pdu0.addOptionalParameter(new
		// Tlv(SmppConstants.TAG_SOURCE_NETWORK_TYPE, new byte[] { (byte)0x01
		// }));
		// pdu0.addOptionalParameter(new
		// Tlv(SmppConstants.TAG_DEST_NETWORK_TYPE, new byte[] { (byte)0x01 }));
		// NOTE: VERY IMPORTANT -- THIS IS A C-STRING!
		pdu0.addOptionalParameter(new Tlv(SmppConstants.TAG_RECEIPTED_MSG_ID, convertOptionalStringToCOctet(FormatUtils.formatAsHex(deliveryReceiptRecord
				.getMessageId()))));
		pdu0.addOptionalParameter(new Tlv(SmppConstants.TAG_MSG_STATE, new byte[] { SmppConstants.STATE_DELIVERED }));

		pdu0.calculateAndSetCommandLength();

		return pdu0;
	}

	public static DeliverSm createDeliverSm(Address sourceAddress, Address destinationAddress, int sequenceNumber) throws SmppInvalidArgumentException {
		DeliverSm pdu0 = new DeliverSm();

		pdu0.setSequenceNumber(sequenceNumber);
		pdu0.setSourceAddress(sourceAddress);
		pdu0.setDestAddress(destinationAddress);
		pdu0.setProtocolId((byte) 0x00);
		pdu0.setPriority((byte) 0x00);
		pdu0.setScheduleDeliveryTime(null);
		pdu0.setValidityPeriod(null);
		pdu0.setRegisteredDelivery((byte) 0x00);
		pdu0.setReplaceIfPresent((byte) 0x00);
		pdu0.setDataCoding((byte) 0x00);
		pdu0.setDefaultMsgId((byte) 0x00);

		return pdu0;
	}

	/**
	 * Taken from
	 * 
	 * @see http://memoirniche.wordpress.com/2010/04/10/smpp-submit-pdu/
	 **/
	public static void setSegmentUdh00AndMessage(DeliverSm pdu, int msgRefNum, int segmentNum, int totalSegmentCount, String shortMessage)
			throws SmppInvalidArgumentException {
		// add UDHI bit to ESM class
		byte esmClass = 0;
		esmClass |= SmppConstants.ESM_CLASS_UDHI_MASK;
		pdu.setEsmClass(esmClass);

		byte[] udhHeader = new byte[6];
		udhHeader[0] = 0x05; // the total length of data in UDH, not including
								// the first byte of header (i.e. the byte
								// containing the total length).
		udhHeader[1] = 0x00; // IE identifier for concatenated messages
		udhHeader[2] = 0x03; // length of data in IE
		udhHeader[3] = (byte) msgRefNum;
		udhHeader[4] = (byte) totalSegmentCount;
		udhHeader[5] = (byte) segmentNum;

		byte[] message = CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_GSM);
		ByteBuffer bb = ByteBuffer.allocate(udhHeader.length + message.length);
		bb.put(udhHeader);
		bb.put(message);

		pdu.setShortMessage(bb.array());
	}

	public static void setSegmentUdh08AndMessage(DeliverSm pdu, int msgRefNum, int segmentNum, int totalSegmentCount, String shortMessage)
			throws SmppInvalidArgumentException {
		// add UDHI bit to ESM class
		byte esmClass = 0;
		esmClass |= SmppConstants.ESM_CLASS_UDHI_MASK;
		pdu.setEsmClass(esmClass);

		byte[] udhHeader = new byte[7];
		udhHeader[0] = 0x06; // the total length of data in UDH, not including
								// the first byte of header (i.e. the byte
								// containing the total length).
		udhHeader[1] = 0x08; // IE identifier for concatenated messages
		udhHeader[2] = 0x05; // length of data in IE
		byte[] unsignedShortMsgRefNum = intToUnsignedShort(msgRefNum);
		udhHeader[3] = unsignedShortMsgRefNum[0];
		udhHeader[4] = unsignedShortMsgRefNum[1];
		udhHeader[5] = (byte) totalSegmentCount;
		udhHeader[6] = (byte) segmentNum;

		byte[] message = CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_GSM);
		ByteBuffer bb = ByteBuffer.allocate(udhHeader.length + message.length);
		bb.put(udhHeader);
		bb.put(message);

		pdu.setShortMessage(bb.array());
	}

	public static void setSegmentOptionalParams(DeliverSm pdu, int msgRefNum, int segmentNum, int totalSegmentCount) {
		pdu.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_MSG_REF_NUM, intToUnsignedShort(msgRefNum)));
		pdu.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_SEGMENT_SEQNUM, intToUnsignedByte(segmentNum)));
		pdu.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_TOTAL_SEGMENTS, intToUnsignedByte(totalSegmentCount)));
	}

	public static void setMessagePayloadOptionalParams(DeliverSm pdu, byte[] messagePayload) {
		pdu.addOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, messagePayload));
	}

	public static final byte[] intToUnsignedShort(int value) throws NumberFormatException {
		byte[] ret = new byte[2];
		ret[0] = (byte) ((value & 0xff00) >>> 8);
		ret[1] = (byte) (value & 0xff);
		return ret;
	}

	public static final byte[] intToUnsignedByte(int value) throws NumberFormatException {
		byte[] ret = new byte[1];
		ret[0] = (byte) value;
		return ret;
	}

	public static byte[] convertOptionalStringToCOctet(String str) {
		if (str == null)
			return null;

		byte[] value = str.getBytes();
		byte[] nullTerminatedValue = new byte[value.length + 1];
		System.arraycopy(value, 0, nullTerminatedValue, 0, value.length);
		nullTerminatedValue[value.length] = 0;
		return nullTerminatedValue;
	}
}
