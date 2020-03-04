package net.voldrich.smscsim.server;

import static net.voldrich.smscsim.server.SmppPduUtils.convertOptionalStringToCOctet;

import com.cloudhopper.smpp.tlv.Tlv;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MccMncUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MccMncUtils.class);

  private final static String DEFAULT_TLV_TAG = "0x1400";
  private final static String DEFAULT_COUNTRY_CODE = "123";
  private final static String DEFAULT_NETWORK_CODE = "546";

  public static Tlv SIMULATOR_MCC_MNC_TLV;

  public MccMncUtils(Properties properties) {
    String tlvTag = getTlvTag(properties);
    String countryCode = getCountryCode(properties);
    String networkCode = getNetworkCode(properties);

    LOGGER
        .info("Tlv tag: {}, country code: {}, network code: {}", tlvTag, countryCode, networkCode);

    SIMULATOR_MCC_MNC_TLV = createSimulatorMccMncTlv(tlvTag, countryCode, networkCode);
  }

  private static Tlv createSimulatorMccMncTlv(String tlvTag, String countryCode,
      String networkCode) {
    final short tlvTagToShort = Short.decode(tlvTag.trim());
    final String simulatorMccMncValue = countryCode + networkCode;

    return new Tlv(tlvTagToShort, convertOptionalStringToCOctet(simulatorMccMncValue));
  }

  public static Tlv getSimulatorMccMncTlv() {
    return SIMULATOR_MCC_MNC_TLV;
  }

  private String getTlvTag(Properties properties) {
    return null != properties.getProperty("TLV_TAG") ? properties.getProperty("TLV_TAG")
        : DEFAULT_TLV_TAG;
  }

  private String getCountryCode(Properties properties) {
    return null != properties.getProperty("COUNTRY_CODE") ? properties.getProperty("COUNTRY_CODE")
        : DEFAULT_COUNTRY_CODE;
  }

  private String getNetworkCode(Properties properties) {
    return null != properties.getProperty("NETWORK_CODE") ? properties.getProperty("NETWORK_CODE")
        : DEFAULT_NETWORK_CODE;
  }

}
