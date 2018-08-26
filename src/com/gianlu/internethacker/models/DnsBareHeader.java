package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

public final class DnsBareHeader implements DnsWritable {
    public final short id;
    public final int qr;
    public final int opcode;
    public final int aa;
    public final int tc;
    public final int rd;
    public final int ra;
    public final int z;
    public final int rcode;
    public final short qdcount;
    public final short ancount;
    public final short nscount;
    public final short arcount;

    DnsBareHeader(DnsInputStream in) {
        id = in.readShort();

        byte byte2 = in.readByte();
        qr = (byte2 >> 7) & 0b00000001;
        opcode = (byte2 >> 3) & 0b00001111;
        aa = (byte2 >> 2) & 0b00000001;
        tc = (byte2 >> 1) & 0b00000001;
        rd = (byte2  /* >> 0 */) & 0b00000001;

        byte byte3 = in.readByte();
        ra = (byte3 >> 7) & 0b00000001;
        z = (byte3 >> 4) & 0b00000111;
        rcode = (byte3 /* >> 0 */) & 0b00001111;

        qdcount = in.readShort();
        ancount = in.readShort();
        nscount = in.readShort();
        arcount = in.readShort();
    }

    DnsBareHeader(DnsBareHeader header) {
        this.id = header.id;
        this.qr = header.qr;
        this.opcode = header.opcode;
        this.aa = header.aa;
        this.tc = header.tc;
        this.rd = header.rd;
        this.ra = header.ra;
        this.z = header.z;
        this.rcode = header.rcode;
        this.qdcount = header.qdcount;
        this.ancount = header.ancount;
        this.nscount = header.nscount;
        this.arcount = header.arcount;
    }

    DnsBareHeader(short id, int qr, int opcode, int aa, int tc, int rd, int ra, int z, int rcode, short qdcount, short ancount, short nscount, short arcount) {
        this.id = id;
        this.qr = qr;
        this.opcode = opcode;
        this.aa = aa;
        this.tc = tc;
        this.rd = rd;
        this.ra = ra;
        this.z = z;
        this.rcode = rcode;
        this.qdcount = qdcount;
        this.ancount = ancount;
        this.nscount = nscount;
        this.arcount = arcount;
    }

    @Override
    public void write(@NotNull DnsOutputStream out) {
        out.writeShort(id);

        byte b = 0;
        b |= qr << 7;
        b |= opcode << 3;
        b |= aa << 2;
        b |= tc << 1;
        b |= rd /* << 0 */;
        out.write(b);

        b = 0;
        b |= ra << 7;
        b |= 0 /* << 4 */; /* z */
        b |= rcode /* << 0 */;
        out.write(b);

        out.writeShort(qdcount);
        out.writeShort(ancount);
        out.writeShort(nscount);
        out.writeShort(arcount);
    }

    public enum RCode {
        NO_ERROR(0),
        FORMAT_ERROR(1),
        SERVER_FAILURE(2),
        NAME_ERROR(3),
        NOT_IMPLEMENTED(4),
        REFUSED(5);

        public final int val;

        RCode(int val) {
            this.val = val;
        }

        @NotNull
        public static RCode parse(int val) {
            for (RCode code : values())
                if (code.val == val)
                    return code;

            throw new IllegalArgumentException("Unknown RCODE for " + val);
        }
    }

    public enum OpCode {
        QUERY(0),
        IQUERY(1),
        STATUS(2);

        public final int val;

        OpCode(int val) {
            this.val = val;
        }

        @NotNull
        public static OpCode parse(int val) {
            for (OpCode code : values())
                if (code.val == val)
                    return code;

            throw new IllegalArgumentException("Unknown OPCODE for " + val);
        }
    }
}
