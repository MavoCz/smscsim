package net.voldrich.smscsim.server;

public class FormatUtils {
  public static String formatAsHex(long msgId) {
    String msgIdStr = String.format("%07x", msgId);
    return msgIdStr.toLowerCase(); // just to be sure :)
  }

  public static String formatAsHexUppercase(long msgId) {
    return formatAsHex(msgId).toUpperCase();
  }

  public static String formatAsDec(long msgId) {
    return String.format("%d", msgId);
  }
}
