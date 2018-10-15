# DNS Client
Command-line program for making DNS requests using sockets in Java. Supports A, MX and NS query types.

### Command syntax:

`java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|ns] @server name`

- timeout: (optional) how long to wait for response (Default: 5)
- max-retries: (optional) maximum number of retries the client will attempt after timing out (Default: 3)
- port: (optional) UDP port number of the specified DNS server (Default: 53)
- -mx or -ns: indicate whether to send an MX or NS query. Client will send a type A query if not specified.
- server: (required) IPV4 address of the DNS server
- name: (required) domain name to query for

