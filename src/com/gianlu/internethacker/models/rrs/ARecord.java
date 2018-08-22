package com.gianlu.internethacker.models.rrs;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ARecord extends RData {
    public final Inet4Address addr;

    public ARecord(byte[] rdata) throws UnknownHostException {
        super(rdata);

        addr = (Inet4Address) InetAddress.getByAddress(rdata);
    }
}
