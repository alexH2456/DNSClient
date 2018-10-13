package ca.mcgill.dnsclient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class DnsClient {

  private int timeout = 5000;
  private int maxRetries = 3;
  private int retries = 0;
  private int dnsPort = 53;
  private byte[] server = new byte[4];
  private String name;
  private String queryType = "A";

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

      byte[] sendData = DnsRequest.constructDnsRequest(name, queryType);
      byte[] receiveData = new byte[1024];

      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, dnsPort);
      clientSocket.send(sendPacket);

      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      clientSocket.receive(receivePacket);

      clientSocket.close();

      HashMap<String, Integer> response = DnsResponse.analyzeResponse(receivePacket.getData());


    } catch (SocketException e) {
      System.out.println("ERROR\tCreating socket: " + e.getMessage());
    } catch (SocketTimeoutException e) {
      System.out.println("ERROR\tTimeout on DNS request: Retrying...");
      retries += 1;
      sendRcvRequest();
    } catch (UnknownHostException e) {
      System.out.println("ERROR\tUnknown host");
    } catch (Exception e) {
      e.printStackTrace();
    }
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
          queryType = "MX";
          break;
        case "-ns":
          queryType = "NS";
          break;
        default:
          if (arg.startsWith("@")) {
            String address = arg.substring(1);
            String[] addrFields = address.split("\\.");
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
          }
          break;
      }
    }
    if (name == null || server == null) {
      throw new IllegalArgumentException("Must specify DNS server and domain name");
    }
  }
}
