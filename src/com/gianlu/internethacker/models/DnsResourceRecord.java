package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import com.gianlu.internethacker.models.rr.AAAARecord;
import com.gianlu.internethacker.models.rr.ARecord;
import com.gianlu.internethacker.models.rr.CNameRecord;
import com.gianlu.internethacker.models.rr.RData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class DnsResourceRecord implements DnsWritable {
    public final List<String> name;
    public final Type type;
    public final Class clazz;
    public final int ttl;
    public final short rdlength;
    public final byte[] rdata;
    private RData data = null;

    DnsResourceRecord(DnsInputStream in) {
        name = in.readLabels();
        type = Type.parse(in.readShort());
        clazz = Class.parse(in.readShort());
        ttl = in.readInt();
        rdlength = in.readShort();
        rdata = new byte[rdlength];
        in.readBytes(rdata);
    }

    private DnsResourceRecord(List<String> name, Type type, Class clazz, int ttl, byte[] rdata) {
        this.name = name;
        this.type = type;
        this.clazz = clazz;
        this.ttl = ttl;
        this.rdlength = (short) rdata.length;
        this.rdata = rdata;
    }

    @NotNull
    public Builder buildUpon() {
        return new Builder(this);
    }

    @NotNull
    public String getName() {
        return String.join(".", name);
    }

    @Override
    public void write(@NotNull DnsOutputStream out) {
        out.writeLabels(name);
        out.writeShort(type.val);
        out.writeShort(clazz.val);
        out.writeInt(ttl);
        out.writeShort(rdlength);
        out.writeBytes(rdata);
    }

    public <R extends RData> R getRData(@NotNull DnsMessage message) {
        if (type.rDataClass == null) throw new IllegalArgumentException(type + " has no RData class associated.");
        if (data == null) {
            try {
                data = (RData) type.rDataClass.getConstructor(DnsInputStream.class)
                        .newInstance(message.createInputStream(rdata));
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException ex) {
                throw new RuntimeException("Something is wrong with the constructors for " + type, ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException("Target threw an exception from " + type, ex.getTargetException());
            }
        }

        // noinspection unchecked
        return (R) data;
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
        A(1, ARecord.class), NS(2, null), MD(3, null), MF(4, null), CNAME(5, CNameRecord.class),
        SOA(6, null), MB(7, null), MG(8, null), MR(9, null), NULL(10, null), WKS(11, null),
        PTR(12, null), HINFO(13, null), MINFO(14, null), MX(15, null), TXT(16, null),
        AAAA(28, AAAARecord.class), CAA(257, null);

        private final short val;
        private final java.lang.Class<?> rDataClass;

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

    public static class Builder {
        private final List<String> name = new ArrayList<>();
        private Type type;
        private Class clazz;
        private int ttl;
        private byte[] rdata;

        private Builder(DnsResourceRecord rr) {
            this.name.addAll(rr.name);
            this.type = rr.type;
            this.clazz = rr.clazz;
            this.ttl = rr.ttl;
            this.rdata = new byte[rr.rdlength];
            System.arraycopy(rr.rdata, 0, this.rdata, 0, rr.rdlength);
        }

        public Builder() {
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setClass(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder setTtl(int ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder setRData(byte[] rdata) {
            this.rdata = rdata;
            return this;
        }

        public Builder setRData(DnsMessage message, RData data) {
            DnsOutputStream out = message.createEmptyStream();
            data.write(out);
            rdata = out.toByteArray();
            return this;
        }

        @NotNull
        public DnsResourceRecord build() {
            return new DnsResourceRecord(name, type, clazz, ttl, rdata);
        }
    }
}
