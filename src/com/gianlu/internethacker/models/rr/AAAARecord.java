package com.gianlu.internethacker.models.rr;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AAAARecord extends RData {
    public final Inet6Address address;

    @DnsInputStreamConstructor
    public AAAARecord(@NotNull DnsInputStream in) throws UnknownHostException {
        address = (Inet6Address) InetAddress.getByAddress(in.buffer());
    }

    public AAAARecord(Inet6Address address) {
        this.address = address;
    }

    @Override
    public void write(@NotNull DnsOutputStream out) {
        out.writeBytes(address.getAddress());
    }
}
