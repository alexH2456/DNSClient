package ca.mcgill.dnsclient;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DnsRequest {

  private String domainName;
  private QueryType queryType;
  private DnsHeader header;

  public DnsRequest(String domainName, QueryType queryType) {
    this.domainName = domainName;
    this.queryType = queryType;
    this.header = new DnsHeader();
  }

  public byte[] constructDnsRequest() throws IOException {

    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream request = new DataOutputStream(byteStream);

    // QNAME - Domain name labels
    String[] domainLabels = domainName.split("\\.");
    for (String domainLabel : domainLabels) {
      byte[] label = domainLabel.getBytes(StandardCharsets.UTF_8);
      request.writeByte(label.length);
      request.write(label);
    }

    // Null label
    request.writeByte(0x00);

    // QTYPE - Query type
    if (queryType == QueryType.A) {
      request.writeShort(0x0001);
    } else if (queryType == QueryType.NS) {
      request.writeShort(0x0002);
    } else {
      request.writeShort(0x000f);
    }

    // QCLASS - Class of query
    request.writeShort(0x0001);

    byte[] requestData = byteStream.toByteArray();

    byteStream = new ByteArrayOutputStream();
    byteStream.write(header.constructHeader());
    byteStream.write(requestData);

    return byteStream.toByteArray();
  }

  public DnsHeader getHeader() {
    return header;
  }

  public String getDomainName() {
    return domainName;
  }

  public QueryType getQueryType() {
    return queryType;
  }

}
