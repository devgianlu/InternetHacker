package com.gianlu.internethacker.models;

import java.nio.ByteBuffer;

public class Header {
    public final int id;
    public final int qr;
    public final int opcode;
    public final int aa;
    public final int tc;
    public final int rd;
    public final int ra;
    public final int z;
    public final int rcode;
    public final int qdcount;
    public final int ancount;
    public final int nscount;
    public final int arcount;

    public Header(ByteBuffer data) {
        id = data.getShort();
        System.out.println("ID: " + id);

        byte byte2 = data.get(); // 0|0000|0|0|1
        qr = (byte2 >> 7) & 0x00000001;
        System.out.println("QR: " + qr);
        opcode = (byte2 >> 3) & 0x0001111;
        System.out.println("OPCODE: " + opcode);
        aa = (byte2 >> 2) & 0x00000001;
        System.out.println("AA: " + aa);
        tc = (byte2 >> 1) & 0x00000001;
        System.out.println("TC: " + tc);
        rd = (byte2  /* >> 0 */) & 0x00000001;
        System.out.println("RD: " + rd);

        byte byte3 = data.get(); // 0|000|0000
        ra = (byte3 >> 7) & 0x00000001;
        System.out.println("RA: " + ra);
        z = (byte3 >> 4) & 0x00000111;
        System.out.println("(Z): " + z);
        rcode = (byte3 /* >> 0 */) & 0x00001111;
        System.out.println("RCODE: " + rcode);

        qdcount = data.getShort();
        System.out.println("QDCOUNT: " + qdcount);

        ancount = data.getShort();
        System.out.println("ANCOUNT: " + ancount);

        nscount = data.getShort();
        System.out.println("NSCOUNT: " + nscount);

        arcount = data.getShort();
        System.out.println("ARCOUNT: " + arcount);
    }
}
