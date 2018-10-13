package ca.mcgill.dnsclient;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DnsRequest {

  private String domainName;
  private String queryType;

  public DnsRequest(String domainName, String queryType) {
    this.domainName = domainName;
    this.queryType = queryType;
  }

  public byte[] constructDnsRequest() throws IOException {
    DnsHeader dnsHeader = new DnsHeader();

    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream request = new DataOutputStream(byteStream);

    // QNAME - Domain name labels
    String[] domainLabels = domainName.split("\\.");
    for (int i = 0; i < domainLabels.length; i++) {
      byte[] label = domainLabels[i].getBytes(StandardCharsets.UTF_8);
      request.writeByte(label.length);
      request.write(label);
    }

    // Null label
    request.writeByte(0x00);

    // QTYPE - Query type
    switch (queryType) {
      case "A":
        request.writeShort(0x0001);
        break;
      case "NS":
        request.writeShort(0x0002);
        break;
      default:
        request.writeShort(0x000f);
        break;
    }

    // QCLASS - Class of query
    request.writeShort(0x0001);

    byte[] requestData = byteStream.toByteArray();

    byteStream = new ByteArrayOutputStream();
    byteStream.write(dnsHeader.constructHeader());
    byteStream.write(requestData);

    return byteStream.toByteArray();
  }

}
