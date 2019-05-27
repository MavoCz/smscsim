package net.voldrich.smscsim.server;

public interface RequestSender<T extends DelayedRecord> {
  void scheduleDelivery(T record);

  void scheduleDelivery(T record, int minDelayMs, int randomDeltaMs);
}
