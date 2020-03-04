package net.voldrich.smscsim.spring.auto;

import static net.voldrich.smscsim.server.SmppPduUtils.convertOptionalStringToCOctet;

import com.cloudhopper.smpp.tlv.Tlv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MccMncUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MccMncUtils.class);

  private final static String DEFAULT_TLV_TAG = "0x1400";
  private final static String DEFAULT_COUNTRY_CODE = "123";
  private final static String DEFAULT_NETWORK_CODE = "546";

  public static Tlv SIMULATOR_MCC_MNC_TLV;

  @Autowired
  public MccMncUtils(Environment environment) {
    String tlvTag = getTlvTag(environment);
    String countryCode = getCountryCode(environment);
    String networkCode = getNetworkCode(environment);

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

  public String getTlvTag(Environment environment) {
    return null != environment.getProperty("TLV_TAG") ? environment.getProperty("TLV_TAG")
        : DEFAULT_TLV_TAG;
  }

  public String getCountryCode(Environment environment) {
    return null != environment.getProperty("COUNTRY_CODE") ? environment.getProperty("COUNTRY_CODE")
        : DEFAULT_COUNTRY_CODE;
  }

  public String getNetworkCode(Environment environment) {
    return null != environment.getProperty("NETWORK_CODE") ? environment.getProperty("NETWORK_CODE")
        : DEFAULT_NETWORK_CODE;
  }

  public static Tlv getSimulatorMccMncTlv() {
    return SIMULATOR_MCC_MNC_TLV;
  }

}
