package net.voldrich.smscsim.server;

public class MccMncUtils {

  // TODO: Get from config/env
  private final static String GATEWAY_SERVICE_TLV_TAG = "1400";
  private final static String MOBILE_COUNTRY_CODE = "63";
  private final static String MOBILE_NETWORK_CODE = "0998";

  public final static short TAG_MCC_MNC = Short.parseShort(GATEWAY_SERVICE_TLV_TAG.trim(), 16);
  public final static String SIMULATOR_MCC_MNC_VALUE = MOBILE_COUNTRY_CODE + MOBILE_NETWORK_CODE;

}
