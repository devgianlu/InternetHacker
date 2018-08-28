# InternetHacker

A tool to manipulate web pages on behalf of the visitor being on the same network as this server.


## Quick start
You can use the [InternetHacker](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/InternetHacker.java) class to quickly setup the tool.

```
InternetHacker.create()
                .useDns(new DnsCombinedHacker("google.com", "example.com"))
                .start();
```


## Setup
Using the **DNS server** by itself isn't very useful as the `Host` header will tell the truth about what we're doing and the server may refuse to respond.
Anyway, here's a checklist of things to do to set it up:
- Set the DNS server as system or router default and make sure the changes are applied (restart everything to be sure)
- Check that the server is working by using `dig` or `nslookup`
- Clear the DNS cache of your system
- You're ready

>In the feature, a more effective solution will be to use the DNS and proxy servers in combination: the DNS server will point to the proxy which will effectively do the changes.


## Strategies

### DNS
The [DnsModule](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/DnsModule.java) setup a DNS server on port 53 which, through the help of some [DnsHacker](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/hackers/DnsHacker.java), will fake DNS requests. Some pre-made DNS hackers are available:
- [DnsAddressHacker](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/hackers/DnsAddressHacker.java) will change *A* and *AAAA* records
- [DnsCNameHacker](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/hackers/DnsCNameHacker.java) will change *CNAME* records
- [DnsCombinedHacker](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/hackers/DnsCombinedHacker.java) will change *A*, *AAAA* and *CNAME* records

### Proxy
The [ProxyModule](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/proxyModule.java) setup a proxy server on the given port which, through the help of some [ProxyHacker](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/hackers/ProxyHacker.java), will manipulate the data being transferred in between. Some pre-made proxy hackers are available:
- [ProxyHttpUrlSwapHacker](https://github.com/devgianlu/InternetHacker/blob/master/src/com/gianlu/internethacker/hackers/ProxyHttpUrlSwapHacker.java) will swap the request URL without any redirect. ATM, matching the URL is a bit tricky. 


## Sources
- [RFC1035](https://tools.ietf.org/html/rfc1035)
- [RFC2671](https://tools.ietf.org/html/rfc2671) (obsoleted by 6891)
- [RFC6891](https://tools.ietf.org/html/rfc6891)
