package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import com.gianlu.internethacker.models.rr.RData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gianlu
 */
public class DnsStandardResourceRecord extends DnsBareResourceRecord {
    public final byte[] rdata;
    private RData data = null;

    DnsStandardResourceRecord(List<String> name, short type, @NotNull DnsInputStream in) {
        super(name, type, in);

        rdata = new byte[rdlength];
        in.readBytes(rdata);
    }

    public DnsStandardResourceRecord(List<String> name, short type, short clazz, int ttl, byte[] rdata) {
        super(name, type, clazz, ttl, (short) rdata.length);
        this.rdata = rdata;
    }

    @NotNull
    public <R extends RData> R getRData(@NotNull DnsMessage message) {
        return getRData(Type.parse(type), message);
    }

    @NotNull
    private <R extends RData> R getRData(@NotNull Type type, @NotNull DnsMessage message) {
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

    @Override
    public void write(@NotNull DnsOutputStream out) {
        super.write(out);
        out.writeBytes(rdata);
    }

    @NotNull
    public Builder buildUpon() {
        return new Builder(this);
    }

    public static class Builder {
        private final List<String> name = new ArrayList<>();
        private short type;
        private short clazz;
        private int ttl;
        private short rdlength;
        private byte[] rdata;

        private Builder(DnsBareResourceRecord rr) {
            this.name.addAll(rr.name);
            this.type = rr.type;
            this.clazz = rr.clazz;
            this.ttl = rr.ttl;
            this.rdlength = rr.rdlength;
        }

        public Builder() {
        }

        public Builder setType(Type type) {
            this.type = type.val;
            return this;
        }

        public Builder setClass(Class clazz) {
            this.clazz = clazz.val;
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
        public DnsStandardResourceRecord build() {
            return new DnsStandardResourceRecord(name, type, clazz, ttl, rdata);
        }
    }
}
