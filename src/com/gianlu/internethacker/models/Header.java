package com.gianlu.internethacker.models;

import com.gianlu.internethacker.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Header {
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

    public Header(ByteBuffer data) {
        id = data.getShort();

        byte byte2 = data.get();
        qr = (byte2 >> 7) & 0b00000001;
        opcode = (byte2 >> 3) & 0b00001111;
        aa = (byte2 >> 2) & 0b00000001;
        tc = (byte2 >> 1) & 0b00000001;
        rd = (byte2  /* >> 0 */) & 0b00000001;

        byte byte3 = data.get();
        ra = (byte3 >> 7) & 0b00000001;
        z = (byte3 >> 4) & 0b00000111;
        if (z != 0) throw new RuntimeException("Z should be 0, something went wrong!");
        rcode = (byte3 /* >> 0 */) & 0b00001111;

        qdcount = data.getShort();
        ancount = data.getShort();
        nscount = data.getShort();
        arcount = data.getShort();
    }

    public void write(OutputStream out) throws IOException {
        Utils.putShort(out, id);

        byte b = 0;
        b |= qr << 7;
        b |= opcode << 3;
        b |= aa << 2;
        b |= tc << 1;
        b |= rd /* << 0 */;
        out.write(b);

        b = 0;
        b |= ra << 7;
        b |= z << 4;
        b |= rcode /* << 0 */;
        out.write(b);

        Utils.putShort(out, qdcount);
        Utils.putShort(out, ancount);
        Utils.putShort(out, nscount);
        Utils.putShort(out, arcount);
    }
}
