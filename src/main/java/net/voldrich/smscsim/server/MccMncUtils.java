package net.voldrich.smscsim.server;

public class MccMncUtils {

  // TODO: Get from config/env
  private final static String GATEWAY_SERVICE_TLV_TAG = "0x1400";
  private final static String COUNTRY_CODE = "123";
  private final static String NETWORK_CODE = "546";

  public final static short TAG_MCC_MNC = Short.decode(GATEWAY_SERVICE_TLV_TAG.trim());
  public final static String SIMULATOR_MCC_MNC_VALUE = COUNTRY_CODE + NETWORK_CODE;

}
