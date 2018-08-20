package com.gianlu.internethacker.models;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Message {
    public final Header header;
    public final List<Question> questions;

    public Message(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        header = new Header(buffer);

        if (header.qr == 0) {
            questions = new ArrayList<>(header.qdcount);
            for (int i = 0; i < header.qdcount; i++)
                questions.add(new Question(buffer));
        } else {
            throw new RuntimeException("You shouldn't be parsing a response!");
        }
    }

    static List<String> readLabels(ByteBuffer data) {
        List<String> labels = new ArrayList<>();

        int length;
        while ((length = data.get()) != 0) {
            byte[] buffer = new byte[length];
            data.get(buffer, 0, length);
            labels.add(new String(buffer));
        }

        return labels;
    }
}
