package ca.mcgill.dnsclient;

public class DnsApp {

  public static void main(String args[]) {
    try {
      DnsClient dnsClient = new DnsClient(args);
      dnsClient.sendRcvRequest();
    } catch (Exception e) {
      if (e.getMessage() != null) {
        System.out.println("ERROR\t" + "\t" + e.getMessage());
      } else {
        e.printStackTrace();
      }
    }
  }
}
