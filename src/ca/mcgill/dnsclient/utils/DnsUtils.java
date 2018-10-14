package ca.mcgill.dnsclient.utils;

import java.util.Formatter;

public class DnsUtils {

  public static int bytesToUnsignedInt(byte[] buff) {
    Formatter format = new Formatter();

    for (byte b : buff) {
      format.format("%02x", b);
    }
    return Integer.decode("0x" + format.toString());
  }

}
