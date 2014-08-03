package net.voldrich.smscsim.server;

public interface RequestSender<T extends DelayedRecord> {
	public void scheduleDelivery(T record);
	
	public void scheduleDelivery(T record, int minDelayMs, int randomDeltaMs);
}
