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
  private int currentIdx;

  public DnsRecord() {
    this.numBytes = 0;
  }

  public void parseRecord(byte[] response, int start) throws Exception {
    currentIdx = start;

    name = buildName(response, currentIdx);

    // Get query type
    int qType = DnsUtils.bytesToUnsignedInt(new byte[]{response[currentIdx], response[currentIdx + 1]});
    currentIdx += 2;
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
    classType = DnsUtils.bytesToUnsignedInt(new byte[]{response[currentIdx], response[currentIdx + 1]});
    currentIdx += 2;
    if (classType != 0x0001) {
      throw new Exception("Invalid class type in response");
    }

    // Get TTL
    ttl = DnsUtils.bytesToUnsignedInt(new byte[]{response[currentIdx], response[currentIdx + 1], response[currentIdx + 2], response[currentIdx + 3]});
    currentIdx += 4;

    // Get RData length
    rdLength = DnsUtils.bytesToUnsignedInt(new byte[]{response[currentIdx], response[currentIdx + 1]});
    currentIdx += 2;

    // Parse RDATA
    byte[] buff = Arrays.copyOfRange(response, currentIdx, currentIdx + rdLength);
    if (queryType == QueryType.A) {
      InetAddress ip = InetAddress.getByAddress(buff);
      ipAddress = ip.getHostAddress();
    } else if (queryType == QueryType.NS) {
      nameServer = buildRData(response, currentIdx);
    } else if (queryType == QueryType.CNAME) {
      alias = buildRData(response, currentIdx);
    } else {
      preference = DnsUtils.bytesToUnsignedInt(new byte[]{response[currentIdx], response[currentIdx + 1]});
      exchange = buildRData(response, currentIdx + 2);
    }
    currentIdx += rdLength;

    numBytes = (currentIdx - start);
  }

  private String buildName(byte[] response, int ptr) {
    int compressed;
    StringBuilder strBuilder = new StringBuilder();

    compressed = isCompressed(response, ptr);
    if (compressed != 0) {
      ptr = compressed;
    }
    while (response[ptr] != 0) {
      int labelLength = response[ptr];
      byte[] labelBytes = Arrays.copyOfRange(response, ptr + 1, ptr + 1 + labelLength);
      strBuilder.append(new String(labelBytes));
      strBuilder.append(".");
      ptr += labelLength + 1;

      if (compressed == 0) {
        currentIdx += labelLength + 1;
      }
    }
    if (compressed != 0) {
      currentIdx += 2;
    }
    strBuilder.deleteCharAt(strBuilder.length() - 1);
    return strBuilder.toString();
  }

  private String buildRData(byte[] response, int ptr) {
    int compressed;
    StringBuilder strBuilder = new StringBuilder();

    while (response[ptr] != 0) {
      compressed = isCompressed(response, ptr);
      if (compressed != 0) {
        ptr = compressed;
      }
      int labelLength = response[ptr];
      byte[] labelBytes = Arrays.copyOfRange(response, ptr + 1, ptr + 1 + labelLength);
      strBuilder.append(new String(labelBytes));
      strBuilder.append(".");
      ptr += labelLength + 1;
    }
    strBuilder.deleteCharAt(strBuilder.length() - 1);
    return strBuilder.toString();
  }

  private int isCompressed(byte[] buff, int ptr) {
    int compressed = 0;
    String ptrStr;
    byte[] ptrBuff = new byte[]{buff[ptr], buff[ptr + 1]};

    StringBuilder strBuilder = new StringBuilder(
        Integer.toBinaryString(DnsUtils.bytesToUnsignedInt(ptrBuff)));
    while (strBuilder.length() != 16) {
      strBuilder.insert(0, '0');
    }
    ptrStr = strBuilder.toString();

    if (ptrStr.charAt(0) == '1' && ptrStr.charAt(1) == '1') {
      compressed = Integer.parseInt(ptrStr.substring(2, 16), 2);
    }
    return compressed;
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

  public String getName() {
    return name;
  }

  public QueryType getQueryType() {
    return queryType;
  }

  public String getAlias() {
    return alias;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getNameServer() {
    return nameServer;
  }

}
