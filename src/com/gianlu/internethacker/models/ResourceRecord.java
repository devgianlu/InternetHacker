package com.gianlu.internethacker.models;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ResourceRecord {
    public final List<String> name;
    public final Type type;
    public final Class clazz;
    public final int ttl;
    public final short rdlength;
    public final byte[] rdata;

    public ResourceRecord(ByteBuffer data) {
        name = Message.readLabels(data);
        type = Type.parse(data.getShort());
        clazz = Class.parse(data.getShort());
        ttl = data.getInt();
        rdlength = data.getShort();
        rdata = new byte[rdlength];
        data.get(rdata);
    }

    public void write(OutputStream out) {
        // TODO
    }

    public enum Class {
        IN(1),
        CH(3),
        HS(4);

        private final short val;

        Class(int val) {
            this.val = (short) val;
        }

        @NotNull
        public static Class parse(int val) {
            for (Class clazz : values())
                if (clazz.val == val)
                    return clazz;

            throw new IllegalArgumentException("Unknown CLASS for " + val);
        }
    }

    public enum Type {
        A(1), NS(2), MD(3), MF(4), CNAME(5), SOA(6), MB(7), MG(8), MR(9),
        NULL(10), WKS(11), PTR(12), HINFO(13), MINFO(14), MX(15), TXT(16),
        AAAA(28);

        private final int val;

        Type(int val) {
            this.val = val;
        }

        @NotNull
        public static Type parse(int val) {
            for (Type type : values())
                if (type.val == val)
                    return type;

            throw new IllegalArgumentException("Unknown TYPE for " + val);
        }
    }
}
