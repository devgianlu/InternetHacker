package com.gianlu.internethacker.models;

import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class DnsExtendedHeader extends DnsHeaderWrapper {
    private final short senderPayloadSize;
    private final int z;
    private final int rcode;
    private final int version;

    DnsExtendedHeader(@NotNull DnsBareHeader header, @NotNull DnsBareResourceRecord opt) {
        super(header);
        this.senderPayloadSize = opt.clazz;

        int ttl = opt.ttl;
        rcode = header.rcode | ((ttl >> 16) & 0b1111111100000000);
        version = (ttl >> 16) & 0b11111111;
        z = ttl & 0b1111111111111111;
    }

    @Override
    public short id() {
        return header.id;
    }

    @Override
    public boolean qr() {
        return header.qr != 0;
    }

    @Override
    public int opcode() {
        return header.opcode;
    }

    @Override
    public boolean aa() {
        return header.aa != 0;
    }

    @Override
    public boolean tc() {
        return header.tc != 0;
    }

    @Override
    public boolean rd() {
        return header.rd != 0;
    }

    @Override
    public boolean ra() {
        return header.ra != 0;
    }

    @Override
    public int z() {
        return z;
    }

    @Override
    public int rcode() {
        return rcode;
    }

    @Override
    public short qdcount() {
        return header.qdcount;
    }

    @Override
    public short ancount() {
        return header.ancount;
    }

    @Override
    public short nscount() {
        return header.nscount;
    }

    @Override
    public short arcount() {
        return header.arcount;
    }

    public short senderPayloadSize() {
        return senderPayloadSize;
    }

    public int ednsVersion() {
        return version;
    }
}
