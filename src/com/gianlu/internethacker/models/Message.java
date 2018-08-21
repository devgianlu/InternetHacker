package com.gianlu.internethacker.models;

import com.gianlu.internethacker.DnsModule;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Message {
    public final Header header;
    public final List<Question> questions;
    public final List<ResourceRecord> answers;
    public final List<ResourceRecord> authorities;
    public final List<ResourceRecord> additional;

    public Message(byte[] data) {
        System.out.println("BITS: " + DnsModule.toBitString(data));
        ByteBuffer buffer = ByteBuffer.wrap(data);

        header = new Header(buffer);

        questions = new ArrayList<>(header.qdcount);
        for (int i = 0; i < header.qdcount; i++)
            questions.add(new Question(buffer));

        answers = new ArrayList<>(header.ancount);
        for (int i = 0; i < header.ancount; i++)
            answers.add(new ResourceRecord(buffer));

        authorities = new ArrayList<>(header.nscount);
        for (int i = 0; i < header.nscount; i++)
            authorities.add(new ResourceRecord(buffer));

        additional = new ArrayList<>(header.arcount);
        for (int i = 0; i < header.arcount; i++)
            additional.add(new ResourceRecord(buffer));
    }

    private Message(@NotNull Header header, @NotNull List<Question> questions, List<ResourceRecord> answers, List<ResourceRecord> authorities, List<ResourceRecord> additional) {
        this.header = header;
        this.questions = questions;
        this.answers = answers;
        this.authorities = authorities;
        this.additional = additional;
    }

    private static List<String> readLabels(ByteBuffer data, int offset) {
        List<String> labels = new ArrayList<>();

        int pos = offset;
        byte length;
        while ((length = data.get(pos++)) != 0) {
            if (((length >> 6) & 0b00000011) == 0b00000011) {
                int loc = data.get(pos++) | ((length & 0b00111111) << 8);
                labels.addAll(readLabels(data, loc));
                break;
            } else {
                byte[] buffer = new byte[length];
                for (int i = 0; i < length; i++)
                    buffer[i] = data.get(pos++);
                labels.add(new String(buffer));
            }
        }

        data.position(pos);

        return labels;
    }

    static List<String> readLabels(ByteBuffer data) {
        return readLabels(data, data.position());
    }

    static void writeLabels(OutputStream out, List<String> labels) throws IOException {
        for (String label : labels) {
            out.write(label.length());
            out.write(label.getBytes());
        }

        out.write(0);
    }

    public void write(OutputStream out) throws IOException {
        header.write(out);

        for (Question question : questions)
            question.write(out);

        for (ResourceRecord rr : answers)
            rr.write(out);

        for (ResourceRecord rr : authorities)
            rr.write(out);

        for (ResourceRecord rr : additional)
            rr.write(out);
    }

    public static class Builder {
        private final Header header;
        private final List<Question> questions = new ArrayList<>();
        private final List<ResourceRecord> answers = new ArrayList<>();
        private final List<ResourceRecord> authorities = new ArrayList<>();
        private final List<ResourceRecord> additional = new ArrayList<>();

        public Builder(@NotNull Header header) {
            this.header = header;
        }

        public void addQuestion(@NotNull Question question) {
            questions.add(question);
        }

        public void addAnswer(@NotNull ResourceRecord rr) {
            answers.add(rr);
        }

        public void addAuthority(@NotNull ResourceRecord rr) {
            authorities.add(rr);
        }

        public void addAdditional(@NotNull ResourceRecord rr) {
            additional.add(rr);
        }

        @NotNull
        public Message build() {
            return new Message(header, questions, answers, authorities, additional);
        }
    }
}
