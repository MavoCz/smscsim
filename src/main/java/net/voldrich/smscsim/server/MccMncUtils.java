package net.voldrich.smscsim.server;

import static net.voldrich.smscsim.server.SmppPduUtils.convertOptionalStringToCOctet;

import com.cloudhopper.smpp.tlv.Tlv;

public class MccMncUtils {

  private final static String GATEWAY_SERVICE_MCC_MNC_TLV_TAG = "0x1400";
  private final static String COUNTRY_CODE = "123";
  private final static String NETWORK_CODE = "546";

  private final static short TAG_MCC_MNC = Short.decode(GATEWAY_SERVICE_MCC_MNC_TLV_TAG.trim());
  private final static String SIMULATOR_MCC_MNC_VALUE = COUNTRY_CODE + NETWORK_CODE;

  public final static Tlv simulatorMccMncTlv = new Tlv(TAG_MCC_MNC, convertOptionalStringToCOctet(SIMULATOR_MCC_MNC_VALUE));

  public static Tlv getSimulatorMccMncTlv() {
    return simulatorMccMncTlv;
  }

}
