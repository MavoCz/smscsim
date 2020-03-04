package net.voldrich.smscsim.server;

import static net.voldrich.smscsim.server.SmppPduUtils.convertOptionalStringToCOctet;

import com.cloudhopper.smpp.tlv.Tlv;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MccMncUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MccMncUtils.class);

  public static Tlv SIMULATOR_MCC_MNC_TLV;

  public MccMncUtils(Properties properties) {
    String tlvTag = getNullSafeProperty(properties, "TLV_TAG", "0x1400");
    String countryCode = getNullSafeProperty(properties, "COUNTRY_CODE", "123");
    String networkCode = getNullSafeProperty(properties, "NETWORK_CODE", "456");

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

  private String getNullSafeProperty(Properties properties, String key, String defaultVal) {
    return null != properties.getProperty(key) ? properties.getProperty(key) : defaultVal;
  }

}
