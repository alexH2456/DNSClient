package ca.mcgill.dnsclient;

import ca.mcgill.dnsclient.utils.DnsUtils;
import java.net.InetAddress;
import java.util.Arrays;

public class DnsRecord {

  private String name;
  private QueryType queryType;
  private int classType;
  private int ttl;
  private int rdLength;
  private String ipAddress;
  private String nameServer;
  private String alias;
  private int preference;
  private String exchange;
  private int numBytes;

  public DnsRecord() {
    this.numBytes = 0;
  }

  public void parseRecord(byte[] response, int startIdx) throws Exception {
    int firstIdx = startIdx;

    byte[] buff;
    String ptrStr;

    buff = new byte[]{response[startIdx], response[startIdx + 1]};
    StringBuilder ptrStrBuilder = new StringBuilder(Integer.toBinaryString(DnsUtils.bytesToUnsignedInt(buff)));
    while (ptrStrBuilder.length() != 16) {
      ptrStrBuilder.insert(0, '0');
    }
    ptrStr = ptrStrBuilder.toString();

    // Domain name parsing with compression
    if (ptrStr.charAt(0) == '1' && ptrStr.charAt(1) == '1') {
      int ptr = Integer.parseInt(ptrStr.substring(2, 16), 2);
      name = buildName(response, ptr);
      startIdx += 2;
    } else {
      int ptr = startIdx;
      StringBuilder newName = new StringBuilder();
      while (response[ptr] != 0) {
        int length = response[ptr];
        byte[] labelBytes = Arrays.copyOfRange(response, ptr + 1, ptr + 1 + length);
        String label = new String(labelBytes);
        newName.append(label);
        newName.append(".");
        ptr += length;
      }
      newName.deleteCharAt(newName.length() - 1);
      name = newName.toString();

      startIdx += numBytes;
    }

    // Get query type
    int qType = DnsUtils.bytesToUnsignedInt(new byte[]{response[startIdx], response[startIdx + 1]});
    startIdx += 2;
    switch (qType) {
      case 0x0001:
        queryType = QueryType.A;
        break;
      case 0x0002:
        queryType = QueryType.NS;
        break;
      case 0x000f:
        queryType = QueryType.MX;
        break;
      case 0x0005:
        queryType = QueryType.CNAME;
        break;
      default:
        throw new Exception("Unknown/Unsupported query type in response");
    }

    // Get class type
    classType = DnsUtils.bytesToUnsignedInt(new byte[]{response[startIdx], response[startIdx + 1]});
    startIdx += 2;
    if (classType != 0x0001) {
      throw new Exception("Invalid class type in response");
    }

    // Get TTL
    ttl = DnsUtils.bytesToUnsignedInt(new byte[]{response[startIdx], response[startIdx + 1], response[startIdx + 2], response[startIdx + 3]});
    startIdx += 4;

    // Get RData length
    rdLength = DnsUtils.bytesToUnsignedInt(new byte[]{response[startIdx], response[startIdx + 1]});
    startIdx += 2;
    numBytes += (startIdx - firstIdx) + 1 + rdLength;

    // Parse RDATA
    buff = Arrays.copyOfRange(response, startIdx, startIdx + rdLength);
    if (queryType == QueryType.A) {
      InetAddress ip = InetAddress.getByAddress(buff);
      ipAddress = ip.getHostAddress();
    } else if (queryType == QueryType.NS) {
      nameServer = buildName(buff, 0);
    } else if (queryType == QueryType.CNAME) {
      alias = buildName(buff, 0);
    } else {
      preference = DnsUtils.bytesToUnsignedInt(new byte[]{buff[0], buff[1]});
      exchange = buildName(buff, 2);
    }
  }

  private String buildName(byte[] buff, int ptr) {
    StringBuilder newName = new StringBuilder();
    while (buff[ptr] != 0) {
      int labelLength = buff[ptr];
      if (labelLength < 0) {
        System.out.println(labelLength);
      }
      byte[] labelBytes = Arrays.copyOfRange(buff, ptr + 1, ptr + 1 + labelLength);
      String label = new String(labelBytes);
      newName.append(label);
      newName.append(".");
      ptr += labelLength + 1;
    }
    newName.deleteCharAt(newName.length() - 1);
    return newName.toString();
  }

  public int getClassType() {
    return classType;
  }

  public String getExchange() {
    return exchange;
  }

  public int getPreference() {
    return preference;
  }

  public int getRdLength() {
    return rdLength;
  }

  public int getTtl() {
    return ttl;
  }

  public int getNumBytes() {
    return numBytes;
  }
}
