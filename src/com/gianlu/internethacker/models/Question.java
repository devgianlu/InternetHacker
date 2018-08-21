package com.gianlu.internethacker.models;


import com.gianlu.internethacker.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class Question {
    public final List<String> qname;
    public final short qtype;
    public final short qclass;

    public Question(ByteBuffer data) {
        qname = Message.readLabels(data);
        qtype = data.getShort();
        qclass = data.getShort();
    }

    public Question(String qname, short qtype, short qclass) {
        this.qname = Arrays.asList(Utils.split(qname, '.'));
        this.qtype = qtype;
        this.qclass = qclass;
    }

    public void write(OutputStream out) throws IOException {
        Message.writeLabels(out, qname);
        Utils.putShort(out, qtype);
        Utils.putShort(out, qclass);
    }
}
