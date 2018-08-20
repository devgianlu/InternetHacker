package com.gianlu.internethacker.models;

import java.nio.ByteBuffer;
import java.util.List;

public class Question {
    public final List<String> qname;
    public final int qtype;
    public final int qclass;

    public Question(ByteBuffer data) {
        qname = Message.readLabels(data);
        System.out.println("QNAME: " + qname);

        qtype = data.getShort();
        System.out.println("QTYPE: " + qtype);

        qclass = data.getShort();
        System.out.println("QCLASS: " + qclass);
    }
}
