package com.gianlu.internethacker.models;

import com.gianlu.internethacker.Utils;
import com.gianlu.internethacker.models.rrs.AAAARecord;
import com.gianlu.internethacker.models.rrs.ARecord;
import com.gianlu.internethacker.models.rrs.RData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DnsResourceRecord {
    public final List<String> name;
    public final Type type;
    public final Class clazz;
    public final int ttl;
    public final short rdlength;
    public final byte[] rdata;
    private RData data = null;

    DnsResourceRecord(ByteBuffer data) {
        name = DnsMessage.readLabels(data);
        type = Type.parse(data.getShort());
        clazz = Class.parse(data.getShort());
        ttl = data.getInt();
        rdlength = data.getShort();
        rdata = new byte[rdlength];
        data.get(rdata);
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
    public <R extends RData> R getRecordData() {
        if (type.rDataClass == null) throw new IllegalStateException(type + " hasn't been mapped to its RData class.");

        if (data == null) {
            try {
                data = type.rDataClass.getConstructor(byte[].class).newInstance((Object) rdata);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
                throw new RuntimeException("Something is wrong with the constructors: " + type, ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException("Failed instantiating " + type, ex.getTargetException());
            }
        }

        // noinspection unchecked
        return (R) data;
    }

    public void write(DnsMessage.LabelsWriter labelsWriter, ByteArrayOutputStream out) throws IOException {
        DnsMessage.writeLabels(out, labelsWriter, name);
        Utils.putShort(out, type.val);
        Utils.putShort(out, clazz.val);
        Utils.putInt(out, ttl);
        Utils.putShort(out, rdlength);
        out.write(rdata);
    }

    public Builder buildUpon() {
        return new Builder(this);
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
        A(1, ARecord.class), NS(2, null), MD(3, null), MF(4, null), CNAME(5, null),
        SOA(6, null), MB(7, null), MG(8, null), MR(9, null), NULL(10, null), WKS(11, null),
        PTR(12, null), HINFO(13, null), MINFO(14, null), MX(15, null), TXT(16, null), AAAA(28, AAAARecord.class),
        CAA(257, null);

        private final short val;
        private final java.lang.Class<? extends RData> rDataClass;

        Type(int val, @Nullable java.lang.Class<? extends RData> rDataClass) {
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

        public Builder setClazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder setTtl(int ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder setRdata(byte[] rdata) {
            this.rdata = rdata;
            return this;
        }

        public DnsResourceRecord build() {
            return new DnsResourceRecord(name, type, clazz, ttl, rdata);
        }
    }
}
