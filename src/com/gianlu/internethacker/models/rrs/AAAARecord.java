package com.gianlu.internethacker.models.rrs;

import com.gianlu.internethacker.models.DnsMessage;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class AAAARecord extends RData {
    public final Inet6Address addr;

    public AAAARecord(DnsMessage.LabelsWriter labelsWriter, ByteBuffer buffer, int offset, int rdlength) throws UnknownHostException {
        super(labelsWriter, buffer, offset, rdlength);

        byte[] rdata = new byte[rdlength];
        buffer.get(rdata, offset, rdlength);
        addr = (Inet6Address) InetAddress.getByAddress(rdata);
    }
}
