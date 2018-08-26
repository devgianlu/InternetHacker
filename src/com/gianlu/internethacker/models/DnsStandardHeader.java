package com.gianlu.internethacker.models;

import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class DnsStandardHeader extends DnsHeaderWrapper {
    DnsStandardHeader(@NotNull DnsBareHeader header) {
        super(header);
        if (header.z != 0) throw new IllegalArgumentException("Z must be 0, but is " + header.z);
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
        return header.z;
    }

    @Override
    public int rcode() {
        return header.rcode;
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
}
