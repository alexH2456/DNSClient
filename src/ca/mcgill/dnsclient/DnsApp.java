package ca.mcgill.dnsclient;

public class DnsApp {

  public static void main(String args[]) {
    try {
      DnsClient dnsClient = new DnsClient(args);
      dnsClient.sendRcvRequest();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

}
