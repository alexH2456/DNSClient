package ca.mcgill.dnsclient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class DnsClient {

  private int timeout = 5000;
  private int maxRetries = 3;
  private int retries = 0;
  private int dnsPort = 53;
  private String ipAddress;
  private byte[] server = new byte[4];
  private String name;
  private QueryType queryType = QueryType.A;

  public DnsClient(String args[]) {
    parseArgs(args);
  }

  public void sendRcvRequest() {
    if (retries > maxRetries) {
      System.out.println("ERROR\tMax number of retries " + maxRetries + " exceeded");
      retries = 0;
      return;
    }
    try {
      DatagramSocket clientSocket = new DatagramSocket();
      clientSocket.setSoTimeout(timeout);
      InetAddress ipAddress = InetAddress.getByAddress(server);

      DnsRequest dnsRequest = new DnsRequest(name, queryType);
      byte[] sendData = dnsRequest.constructDnsRequest();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, dnsPort);

      DnsResponse dnsResponse = new DnsResponse();
      byte[] receiveData = new byte[1024];
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

      printRequestInfo();
      long start = System.currentTimeMillis();
      clientSocket.send(sendPacket);
      clientSocket.receive(receivePacket);
      long stop = System.currentTimeMillis();
      System.out.println("Response received after " + (stop - start) / 1000.0 + " seconds (" + retries + " retries)");

      clientSocket.close();
      dnsResponse.parseResponse(receivePacket.getData());

    } catch (SocketException e) {
      System.out.println("ERROR\tFailed to create socket: " + e.getMessage());
    } catch (SocketTimeoutException e) {
      System.out.println("ERROR\tTimeout on DNS request: Retrying...");
      retries += 1;
      sendRcvRequest();
    } catch (UnknownHostException e) {
      System.out.println("ERROR\tUnknown host: " + e.getMessage());
    } catch (Exception e) {
      if (e.getMessage() == null) {
        e.printStackTrace();
      } else {
        System.out.println("ERROR\t" + e.getMessage());
      }
    }
  }

  private void printRequestInfo() {
    System.out.println("DnsClient sending request for " + name);
    System.out.println("Server: " + ipAddress);
    System.out.println("Request type: " + queryType);
  }

  private void parseArgs(String[] args) {
    List<String> argsList = Arrays.asList(args);
    ListIterator<String> argsIterator = argsList.listIterator();

    while (argsIterator.hasNext()) {
      String arg = argsIterator.next();
      switch (arg) {
        case "-t":
          timeout = Integer.parseInt(argsIterator.next()) * 1000;
          break;
        case "-r":
          maxRetries = Integer.parseInt(argsIterator.next());
          break;
        case "-p":
          dnsPort = Integer.parseInt(argsIterator.next());
          break;
        case "-mx":
          queryType = QueryType.MX;
          break;
        case "-ns":
          queryType = QueryType.NS;
          break;
        default:
          if (arg.startsWith("@")) {
            ipAddress = arg.substring(1);
            String[] addrFields = ipAddress.split("\\.");
            if (addrFields.length != 4) {
              throw new IllegalArgumentException("Invalid IP address length");
            }

            for (int i = 0; i < addrFields.length; i++) {
              if (addrFields[i].length() > 3) {
                throw new IllegalArgumentException("Invalid IP address format");
              }
              int num = Integer.parseInt(addrFields[i]);
              if (num < 0 || num > 255) {
                throw new IllegalArgumentException("Given IP field out of range");
              }
              server[i] = (byte) num;
            }
            name = argsIterator.next();
            String[] labels = name.split("\\.");
            for (String label : labels) {
              if (label.getBytes(StandardCharsets.UTF_8).length > 63) {
                throw new IllegalArgumentException("Illegal domain name, label too long");
              }
            }
          }
          break;
      }
    }
    if (name == null || server == null) {
      throw new IllegalArgumentException("Must specify DNS server and domain name");
    }
  }
}
