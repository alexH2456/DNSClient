package ca.mcgill.dnsclient;

import java.io.IOException;
import java.util.Arrays;

public class DnsResponse {

  private String domainName;
  private int queryType;
  private int classType;
  private int ttl;
  private int rdLength;
  private int rData;
  private int preference;
  private int exchange;
  private DnsHeader header;
  private ResponseCode responseCode;

  public DnsResponse() {
    this.header = new DnsHeader();
  }

  public void parseResponse(byte[] response) throws Exception {
    header.parseHeader(Arrays.copyOfRange(response, 0, 12));
    String queryBits = Integer.toBinaryString(header.getQueryFlags());
    if (queryBits.charAt(0) != '1') {
      throw new Exception("Query bit in header does not match expected (1)");
    }
    boolean auth = queryBits.charAt(5) == '1';
    String rcode = queryBits.substring(12, 16);
    switch (rcode) {
      case "0001":
        responseCode = ResponseCode.FORMAT_ERROR;
        throw new Exception("Format error: the name server was unable to interpret the query");
      case "0010":
        responseCode = ResponseCode.SERVER_FAILURE;
        throw new Exception("Server failure: the name server was unable to process this query due to a problem with the name server");
      case "0011":
        responseCode = ResponseCode.NAME_ERROR;
        System.out.println("NOTFOUND");
        return;
      case "0100":
        responseCode = ResponseCode.NOT_IMPLEMENTED;
        throw new Exception("Not implemented: the name server does not support the requested kind of query");
      case "0101":
        responseCode = ResponseCode.REFUSED;
        throw new Exception("Refused: the name server refuses to perform the requested operation for policy reasons");
      default:
          responseCode = ResponseCode.NO_ERROR;
          break;
    }
  }

  public DnsHeader getHeader() {
    return header;
  }

  public int getClassType() {
    return classType;
  }

  public int getExchange() {
    return exchange;
  }

  public int getPreference() {
    return preference;
  }

  public int getQueryType() {
    return queryType;
  }

  public int getrData() {
    return rData;
  }

  public int getRdLength() {
    return rdLength;
  }

  public int getTtl() {
    return ttl;
  }

  public String getDomainName() {
    return domainName;
  }
}
