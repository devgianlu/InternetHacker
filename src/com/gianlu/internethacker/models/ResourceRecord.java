package com.gianlu.internethacker.models;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ResourceRecord {
    public final List<String> name;
    public final short type;
    public final short clazz;
    public final int ttl;
    public final short rdlength;
    public final byte[] rdata;

    public ResourceRecord(ByteBuffer data) {
        name = Message.readLabels(data);
        type = data.getShort();
        clazz = data.getShort();
        ttl = data.getInt();
        rdlength = data.getShort();
        rdata = new byte[rdlength];
        data.get(rdata);
    }

    public void write(OutputStream out) {
        // TODO
    }
}
