package ca.mcgill.dnsclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class DnsClient {

  private int timeout = 5000;
  private int maxRetries = 3;
  private int dnsPort = 53;
  private byte[] server = new byte[4];
  private String name;
  private int dnsPacketSize = 512;
  private String queryType = "A";

  public DnsClient(String args[]) {
    try {
      parseArgs(args);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid arguments");
    }
  }

  private void parseArgs(String[] args) {
    List<String> argsList = Arrays.asList(args);
    ListIterator<String> argsIterator = argsList.listIterator();

    while (argsIterator.hasNext()) {
      String arg = argsIterator.next();
      switch (arg) {
        case "-t":
          timeout = Integer.parseInt(argsIterator.next());
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
              server[i] = (byte)num;
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

  public void sendRcvRequest() {
    try {
      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress ipAddress = InetAddress.getByAddress(server);

      byte[] sendData = new byte[dnsPacketSize];
      byte[] receiveData = new byte[dnsPacketSize];

      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, dnsPort);
      clientSocket.send(sendPacket);

      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      clientSocket.receive(receivePacket);

      clientSocket.close();

      String sentPacket = new String(sendPacket.getData());
      String receivedPacket = new String(receivePacket.getData());

      System.out.println("SENT: " + sentPacket + "\nRECIEVED: " + receivedPacket);

    } catch (SocketException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
