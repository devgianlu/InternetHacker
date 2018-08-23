package com.gianlu.internethacker;

import com.gianlu.internethacker.hackers.DnsAddressHacker;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        InternetHacker.create()
                .useDns(new DnsAddressHacker("google.com", (Inet4Address) InetAddress.getByName("127.0.0.1")))
                .start();
    }
}
