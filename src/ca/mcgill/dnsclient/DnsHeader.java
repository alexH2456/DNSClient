package ca.mcgill.dnsclient;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class DnsHeader {

  public static byte[] constructHeader() throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream header = new DataOutputStream(byteStream);

    // 1. ID - Generate random 16-bit number for each request
    Random rand = new Random();
    int id = rand.nextInt(0xffff);
    header.writeShort(id);

    // 2. Query flags - QR, OPCODE, AA, TC, RD, RA, Z, RCODE
    header.writeShort(0x0100);

    // 3. Question Count - Number of entries in question section
    header.writeShort(0x0001);

    // 4. Answer Count - Number of entries in answer section
    header.writeShort(0x0000);

    // 5. Authority Records - Number of records in authority section
    header.writeShort(0x0000);

    // 6. Additional Records
    header.writeShort(0x0000);

    return byteStream.toByteArray();
  }

  public static HashMap<String, Integer> analyzeHeader(byte[] header) throws IOException {
    HashMap<String, Integer> headerInfo = new HashMap<>();

    return headerInfo;
  }
}
