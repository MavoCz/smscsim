package net.voldrich.smscsim.spring.message;

import org.springframework.beans.factory.annotation.Autowired;

import net.voldrich.smscsim.server.SmppPduUtils;
import net.voldrich.smscsim.spring.auto.SmppSessionManager;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.type.Address;

public class DeliverBaseMessageFactory implements MessageFactory<DeliverSm> {

	@Autowired
	private SmppSessionManager sessionManager;

	private int sourceAddressTon = 0;
	private int sourceAddressNpi = 0;
	private String sourceAddressDigits = "123456789";

	private int destAddressTon = 0;
	private int destAddressNpi = 0;
	private String destAddressDigits = "987654321";

	@Override
	public DeliverSm createMessage() throws Exception {
		Address sourceAddress = new Address((byte) sourceAddressTon, (byte) sourceAddressNpi, sourceAddressDigits);
		Address destinationAddress = new Address((byte) destAddressTon, (byte) destAddressNpi, destAddressDigits);
		DeliverSm pdu = SmppPduUtils.createDeliverSm(sourceAddress, destinationAddress, sessionManager.getNextSequenceNumber());
		return pdu;
	}

	public int getSourceAddressTon() {
		return sourceAddressTon;
	}

	public void setSourceAddressTon(int sourceAddressTon) {
		this.sourceAddressTon = sourceAddressTon;
	}

	public int getSourceAddressNpi() {
		return sourceAddressNpi;
	}

	public void setSourceAddressNpi(int sourceAddressNpi) {
		this.sourceAddressNpi = sourceAddressNpi;
	}

	public String getSourceAddressDigits() {
		return sourceAddressDigits;
	}

	public void setSourceAddressDigits(String sourceAddressDigits) {
		this.sourceAddressDigits = sourceAddressDigits;
	}

	public int getDestAddressTon() {
		return destAddressTon;
	}

	public void setDestAddressTon(int destAddressTon) {
		this.destAddressTon = destAddressTon;
	}

	public int getDestAddressNpi() {
		return destAddressNpi;
	}

	public void setDestAddressNpi(int destAddressNpi) {
		this.destAddressNpi = destAddressNpi;
	}

	public String getDestAddressDigits() {
		return destAddressDigits;
	}

	public void setDestAddressDigits(String destAddressDigits) {
		this.destAddressDigits = destAddressDigits;
	}

}
