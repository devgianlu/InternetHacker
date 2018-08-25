package com.gianlu.internethacker.models.rrs;

import com.gianlu.internethacker.models.DnsMessage;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class ARecord extends RData {
    public final Inet4Address addr;

    public ARecord(DnsMessage.LabelsWriter labelsWriter, ByteBuffer buffer, int offset, int rdlength) throws UnknownHostException {
        super(labelsWriter, buffer, offset, rdlength);

        byte[] rdata = new byte[rdlength];
        buffer.get(rdata, offset, rdlength);
        addr = (Inet4Address) InetAddress.getByAddress(rdata);
    }
}
