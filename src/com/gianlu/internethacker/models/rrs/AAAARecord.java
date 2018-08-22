package com.gianlu.internethacker.models.rrs;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AAAARecord extends RData {
    public final Inet6Address addr;

    public AAAARecord(byte[] rdata) throws UnknownHostException {
        super(rdata);

        addr = (Inet6Address) InetAddress.getByAddress(rdata);
    }
}
