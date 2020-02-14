package net.voldrich.smscsim.server;

public class MccMncUtils {

  // TODO: Get from config/env
  private final static String GATEWAY_SERVICE_TLV_TAG = "1400";
  private final static String COUNTRY_CODE = "123";
  private final static String NETWORK_CODE = "546";

  public final static short TAG_MCC_MNC = Short.parseShort(GATEWAY_SERVICE_TLV_TAG.trim(), 16);
  public final static String SIMULATOR_MCC_MNC_VALUE = COUNTRY_CODE + NETWORK_CODE;

}
