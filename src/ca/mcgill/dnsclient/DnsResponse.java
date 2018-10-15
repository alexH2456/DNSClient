package ca.mcgill.dnsclient;

import java.util.ArrayList;
import java.util.Arrays;

public class DnsResponse {

  private int startIdx;
  private DnsHeader header;
  private ResponseCode responseCode;
  private ArrayList<DnsRecord> dnsRecords;
  private ArrayList<DnsRecord> addRecords;
  private int requestId;
  private boolean authoritative;

  public DnsResponse(int startIdx, int requestId) {
    this.header = new DnsHeader();
    this.dnsRecords = new ArrayList<>();
    this.addRecords = new ArrayList<>();
    this.startIdx = startIdx;
    this.requestId = requestId;
  }

  public void parseResponse(byte[] response) throws Exception {
    header.parseHeader(Arrays.copyOfRange(response, 0, 12), requestId);
    String queryBits = Integer.toBinaryString(header.getQueryFlags());
    if (queryBits.charAt(0) != '1') {
      throw new Exception("Query bit in header does not match expected (1)");
    }
    authoritative = queryBits.charAt(5) == '1';
    String rcode = queryBits.substring(12, 16);
    switch (rcode) {
      case "0000":
        responseCode = ResponseCode.NO_ERROR;
        break;
      case "0001":
        responseCode = ResponseCode.FORMAT_ERROR;
        throw new Exception("Format error: the name server was unable to interpret the query");
      case "0010":
        responseCode = ResponseCode.SERVER_FAILURE;
        throw new Exception(
            "Server failure: the name server was unable to process this query due to a problem with the name server");
      case "0011":
        responseCode = ResponseCode.NAME_ERROR;
        System.out.println("NOTFOUND");
        return;
      case "0100":
        responseCode = ResponseCode.NOT_IMPLEMENTED;
        throw new Exception(
            "Not implemented: the name server does not support the requested kind of query");
      case "0101":
        responseCode = ResponseCode.REFUSED;
        throw new Exception(
            "Refused: the name server refuses to perform the requested operation for policy reasons");
      default:
        responseCode = ResponseCode.UNKNOWN;
        throw new Exception("Unknown response code in header: " + rcode);
    }
    if (header.getQuestionCount() != 1) {
      throw new Exception("Response header question count does not match");
    }
    int answerCount = header.getAnswerCount();
    int addCount = header.getAddRecords();

    // Parse answer records
    int idx = startIdx;
    int parsedAnswers = 0;
    while (parsedAnswers != answerCount) {
      DnsRecord newRecord = new DnsRecord();
      newRecord.parseRecord(response, idx);
      dnsRecords.add(newRecord);
      parsedAnswers += 1;
      idx += newRecord.getNumBytes();
    }

    // Parse additional records (if any)
    int parsedAdd = 0;
    while (parsedAdd != addCount && header.getAuthRecords() == 0) {
      DnsRecord newRecord = new DnsRecord();
      newRecord.parseRecord(response, idx);
      addRecords.add(newRecord);
      parsedAdd += 1;
      idx += newRecord.getNumBytes();
    }
  }

  public void printResponse() {
    if (!dnsRecords.isEmpty()) {
      System.out.println("\n*** Answer Section (" + dnsRecords.size() + " records) ***");
      printRecords(dnsRecords);
    }
    if (!addRecords.isEmpty()) {
      System.out.println("\n*** Additional Section (" + dnsRecords.size() + " records) ***");
      printRecords(addRecords);
    }
  }

  private void printRecords(ArrayList<DnsRecord> records) {
    String type;
    String result;
    int ttl;
    for (DnsRecord record : dnsRecords) {
      String auth = authoritative ? "auth" : "noauth";
      ttl = record.getTtl();
      if (record.getQueryType() == QueryType.A) {
        type = "IP";
        result = record.getIpAddress();
      } else if (record.getQueryType() == QueryType.CNAME) {
        type = "CNAME";
        result = record.getAlias();
      } else if (record.getQueryType() == QueryType.MX) {
        type = "MX";
        result = record.getExchange() + "\t" + record.getPreference();
      } else {
        type = "NS";
        result = record.getNameServer();
      }
      System.out.println(type + "\t" + result + "\t" + ttl + "\t" + auth);
    }
  }

  public DnsHeader getHeader() {
    return header;
  }

  public ResponseCode getResponseCode() {
    return responseCode;
  }

  public ArrayList<DnsRecord> getDnsRecords() {
    return dnsRecords;
  }

  public boolean isAuthoritative() {
    return authoritative;
  }
}
