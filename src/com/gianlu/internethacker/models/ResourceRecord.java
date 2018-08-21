package com.gianlu.internethacker.models;

import com.sun.istack.internal.NotNull;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class ResourceRecord {

    public ResourceRecord(ByteBuffer data) {
        List<String> name = Message.readLabels(data);
        System.out.println("NAME: " + name);

        short type = data.getShort();
        System.out.println("TYPE: " + type);
        short clazz = data.getShort();
        System.out.println("CLASS: " + clazz);
        int ttl = data.getInt();
        System.out.println("TTL: " + ttl);
        short rdlength = data.getShort();
        System.out.println("RDLENGTH: " + rdlength);

        byte[] rdata = new byte[rdlength];
        data.get(rdata);
        System.out.println("RDATA: " + Arrays.toString(rdata)); // TODO: Must be handled differently by every RDATA type
    }

    private ResourceRecord() {
    }

    public void write(OutputStream out) {

    }

    public static class Builder {

        public Builder() {
        }

        @NotNull
        public ResourceRecord build() {
            return new ResourceRecord();
        }
    }
}
