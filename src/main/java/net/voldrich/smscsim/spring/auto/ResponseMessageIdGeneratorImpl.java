package net.voldrich.smscsim.spring.auto;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import net.voldrich.smscsim.spring.ResponseMessageIdGenerator;

/**
 * Handles message ID generation and formating.
 **/
@Component
public class ResponseMessageIdGeneratorImpl implements ResponseMessageIdGenerator {
	
	private AtomicLong nextMessageId;
	
	private long initialMessageIdValue;

	public ResponseMessageIdGeneratorImpl() {
		nextMessageId = new AtomicLong();		
	}
	
	/* (non-Javadoc)
	 * @see net.voldrich.smscsim.server.ResponseMessageIdGenerator#getNextMessageId()
	 */
	@Override
	public long getNextMessageId() {
		return nextMessageId.incrementAndGet();				
	}

	public long getInitialMessageIdValue() {
		return initialMessageIdValue;
	}

	public void setInitialMessageIdValue(long initialMessageIdValue) {
		this.initialMessageIdValue = initialMessageIdValue;
		this.nextMessageId.set(initialMessageIdValue);
	}
	
}
