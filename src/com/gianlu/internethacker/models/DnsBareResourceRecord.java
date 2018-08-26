package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import com.gianlu.internethacker.models.rr.AAAARecord;
import com.gianlu.internethacker.models.rr.ARecord;
import com.gianlu.internethacker.models.rr.CNameRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class DnsBareResourceRecord implements DnsWritable {
    public final List<String> name;
    public final short type;
    public final short clazz;
    public final int ttl;
    public final short rdlength;
    private String nameStr;

    protected DnsBareResourceRecord(List<String> name, short type, @NotNull DnsInputStream in) {
        this.name = name;
        this.type = type;
        this.clazz = in.readShort();
        this.ttl = in.readInt();
        this.rdlength = in.readShort();
    }

    protected DnsBareResourceRecord(List<String> name, short type, short clazz, int ttl, short rdlength) {
        this.name = name;
        this.type = type;
        this.clazz = clazz;
        this.ttl = ttl;
        this.rdlength = rdlength;
    }

    @NotNull
    public static DnsBareResourceRecord parse(@NotNull DnsInputStream in) {
        List<String> name = in.readLabels();
        short type = in.readShort();
        if (Type.parse(type) == Type.OPT) return new DnsOptionResourceRecord(name, type, in);
        else return new DnsStandardResourceRecord(name, type, in);
    }

    @NotNull
    public String getName() {
        if (nameStr == null) nameStr = String.join(".", name);
        return nameStr;
    }

    @NotNull
    public Type getType() {
        return Type.parse(type);
    }

    @NotNull
    public Class getClazz() {
        return Class.parse(clazz);
    }

    @Override
    public void write(@NotNull DnsOutputStream out) {
        out.writeLabels(name);
        out.writeShort(type);
        out.writeShort(clazz);
        out.writeInt(ttl);
        out.writeShort(rdlength);
    }

    public enum Class {
        IN(1),
        CH(3),
        HS(4);

        public final short val;

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
        A(1, ARecord.class), NS(2, null), MD(3, null), MF(4, null), CNAME(5, CNameRecord.class),
        SOA(6, null), MB(7, null), MG(8, null), MR(9, null), NULL(10, null), WKS(11, null),
        PTR(12, null), HINFO(13, null), MINFO(14, null), MX(15, null), TXT(16, null),
        AAAA(28, AAAARecord.class), OPT(41, null), CAA(257, null);

        public final short val;
        final java.lang.Class<?> rDataClass;

        Type(int val, @Nullable java.lang.Class<?> rDataClass) {
            this.val = (short) val;
            this.rDataClass = rDataClass;
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
