package ca.mcgill.dnsclient;

import ca.mcgill.dnsclient.utils.DnsUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Formatter;
import java.util.Random;

public class DnsHeader {

  private int id;
  private int queryFlags;
  private int questionCount;
  private int answerCount;
  private int authRecords;
  private int addRecords;

  public DnsHeader() {
    Random rand = new Random();
    this.id = rand.nextInt(0xffff);
    this.queryFlags = 0x0100;
    this.questionCount = 0x0001;
    this.answerCount = 0x0000;
    this.authRecords = 0x0000;
    this.addRecords = 0x0000;
  }

  public byte[] constructHeader() throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream header = new DataOutputStream(byteStream);

    // 1. ID - Generate random 16-bit number for each request
    header.writeShort(id);

    // 2. Query flags - QR, OPCODE, AA, TC, RD, RA, Z, RCODE
    header.writeShort(queryFlags);

    // 3. Question Count - Number of entries in question section
    header.writeShort(questionCount);

    // 4. Answer Count - Number of entries in answer section
    header.writeShort(answerCount);

    // 5. Authority Records - Number of records in authority section
    header.writeShort(authRecords);

    // 6. Additional Records
    header.writeShort(addRecords);

    return byteStream.toByteArray();
  }

  public void parseHeader(byte[] header, int requestId) throws Exception {
    byte[] buff;

    // Find ID
    buff = new byte[]{header[0], header[1]};
    id = DnsUtils.bytesToUnsignedInt(buff);
    if (id != requestId) {
      throw new Exception("Failed to verify ID of in response header");
    }

    // Query flags
    buff = new byte[]{header[2], header[3]};
    queryFlags = DnsUtils.bytesToUnsignedInt(buff);

    // Question count
    buff = new byte[]{header[4], header[5]};
    questionCount = DnsUtils.bytesToUnsignedInt(buff);

    // Answer count
    buff = new byte[]{header[6], header[7]};
    answerCount = DnsUtils.bytesToUnsignedInt(buff);

    // Auth records
    buff = new byte[]{header[8], header[9]};
    authRecords = DnsUtils.bytesToUnsignedInt(buff);

    // Additional records
    buff = new byte[]{header[10], header[11]};
    addRecords = DnsUtils.bytesToUnsignedInt(buff);
  }

  public int getId() {
    return id;
  }

  public int getQueryFlags() {
    return queryFlags;
  }

  public int getAddRecords() {
    return addRecords;
  }

  public int getAnswerCount() {
    return answerCount;
  }

  public int getQuestionCount() {
    return questionCount;
  }

  public int getAuthRecords() {
    return authRecords;
  }
}
