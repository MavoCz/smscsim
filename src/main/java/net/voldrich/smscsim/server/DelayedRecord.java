package net.voldrich.smscsim.server;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.PduRequest;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import net.voldrich.smscsim.spring.auto.SmppSessionManager;

public abstract class DelayedRecord implements Delayed {
	
	private final SmppSession session;
	
	private long deliveryTimeMs;

	public DelayedRecord() {
		session = null;
	}
	
	public DelayedRecord(SmppSession session) {
		this.session = session;
	}
	
	public void setDeliverTime(int minDelayMs, int randomDeltaMs) {
		this.deliveryTimeMs = System.currentTimeMillis() + minDelayMs + (int) (Math.random() * randomDeltaMs);
	}

    public void setDeliverTime(long deliveryTimeMs) {
        this.deliveryTimeMs = deliveryTimeMs;
    }
	
	public DelayedRecord(SmppSession session, long deliverAfterMs) {		
		this.session = session;
		this.deliveryTimeMs = System.currentTimeMillis() + deliverAfterMs;
	}
	
	@Override
	public int compareTo(Delayed o) {
		if (this.deliveryTimeMs < ((DelayedRecord) o).deliveryTimeMs) {
			return -1;
		} else if (this.deliveryTimeMs > ((DelayedRecord) o).deliveryTimeMs) {
			return 1;
		}
		return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(deliveryTimeMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public SmppSession getSession() {
		return session;
	}
	
	public abstract SmppSession getUsedSession(SmppSessionManager sessionManager);
	
	public abstract PduRequest getRequest(int sequenceNumber) throws Exception;

}
