package com.gianlu.internethacker.models;

import com.gianlu.internethacker.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DnsMessage {
    public final DnsHeader header;
    public final List<DnsQuestion> questions;
    public final List<DnsResourceRecord> answers;
    public final List<DnsResourceRecord> authorities;
    public final List<DnsResourceRecord> additional;

    public DnsMessage(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        header = new DnsHeader(buffer);

        questions = new ArrayList<>(header.qdcount);
        for (int i = 0; i < header.qdcount; i++)
            questions.add(new DnsQuestion(buffer));

        answers = new ArrayList<>(header.ancount);
        for (int i = 0; i < header.ancount; i++)
            answers.add(new DnsResourceRecord(buffer));

        authorities = new ArrayList<>(header.nscount);
        for (int i = 0; i < header.nscount; i++)
            authorities.add(new DnsResourceRecord(buffer));

        additional = new ArrayList<>(header.arcount);
        for (int i = 0; i < header.arcount; i++)
            additional.add(new DnsResourceRecord(buffer));
    }

    /**
     * Reads a set of labels from a DNS message
     *
     * @param data   a {@link ByteBuffer} to read the data from, position is not important.
     * @param offset where to start reading
     * @return the desired domain-name split into labels
     */
    @NotNull
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

    /**
     * Write labels to a DNS message.
     *
     * @param out          the {@link java.io.OutputStream} to write to.
     * @param labelsWriter an instance of {@link LabelsWriter}, must be the same for the whole message.
     * @param labels       the labels to write.
     */
    static void writeLabels(ByteArrayOutputStream out, LabelsWriter labelsWriter, List<String> labels) throws IOException {
        short loc = labelsWriter.searchFull(labels);
        if (loc != -1) {
            Utils.putDnsLabelPointer(out, loc);
            return;
        }

        for (int i = 0; i < labels.size(); i++) {
            loc = i == 0 ? -1 : labelsWriter.search(labels, i);
            if (loc == -1) {
                labelsWriter.register(labels, i, out.size());
                String label = labels.get(i);
                out.write(label.length());
                out.write(label.getBytes());
            } else {
                Utils.putDnsLabelPointer(out, loc);
                return;
            }
        }

        out.write(0);
    }

    public void write(ByteArrayOutputStream out) throws IOException {
        header.write(out);

        LabelsWriter labelsWriter = new LabelsWriter();

        for (DnsQuestion question : questions)
            question.write(labelsWriter, out);

        for (DnsResourceRecord rr : answers)
            rr.write(labelsWriter, out);

        for (DnsResourceRecord rr : authorities)
            rr.write(labelsWriter, out);

        for (DnsResourceRecord rr : additional)
            rr.write(labelsWriter, out);
    }

    static class LabelsWriter {
        private Map<String, Integer> map = new HashMap<>();

        private LabelsWriter() {
        }

        @NotNull
        private static String buildDomain(List<String> labels, int from) {
            StringBuilder builder = new StringBuilder();
            for (int i = from; i < labels.size(); i++)
                builder.append(labels.get(i));
            return builder.toString();
        }

        short search(List<String> labels, int from) {
            Integer pos = map.get(buildDomain(labels, from));
            return (short) (pos == null ? -1 : pos);
        }

        void register(List<String> labels, int from, int loc) {
            map.put(buildDomain(labels, from), loc);
        }

        short searchFull(List<String> labels) {
            return search(labels, 0);
        }
    }
}
