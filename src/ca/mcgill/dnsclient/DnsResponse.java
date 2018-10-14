package ca.mcgill.dnsclient;

import ca.mcgill.dnsclient.utils.DnsUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DnsResponse {

  private String domainName;
  private DnsHeader header;
  private ResponseCode responseCode;
  private ArrayList<DnsRecord> dnsRecords;

  public DnsResponse() {
    this.header = new DnsHeader();
    this.dnsRecords = new ArrayList<>();
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
    if (header.getQuestionCount() != 1) {
      throw new Exception("Response header question count does not match");
    }
    int answerCount = header.getAnswerCount();
    int addCount = header.getAddRecords();

    // Parse answer records
    int idx = 27;
    int parsedAnswers = 0;
    while (parsedAnswers != answerCount) {
      DnsRecord newRecord = new DnsRecord();
      newRecord.parseRecord(response, idx);
      dnsRecords.add(newRecord);
      parsedAnswers += 1;
      idx += newRecord.getNumBytes() + 1;
    }
    System.out.println("");
  }

  public DnsHeader getHeader() {
    return header;
  }

  public String getDomainName() {
    return domainName;
  }

  public ResponseCode getResponseCode() {
    return responseCode;
  }
}
