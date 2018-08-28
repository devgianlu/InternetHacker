package com.gianlu.internethacker.models.rr;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ARecord extends RData {
    public final Inet4Address address;

    @DnsInputStreamConstructor
    public ARecord(@NotNull DnsInputStream in) throws UnknownHostException {
        address = (Inet4Address) InetAddress.getByAddress(in.buffer());
    }

    public ARecord(Inet4Address address) {
        this.address = address;
    }

    @Override
    public void write(@NotNull DnsOutputStream out) {
        out.writeBytes(address.getAddress());
    }
}
