package com.gianlu.internethacker.io;

import com.gianlu.internethacker.models.LabelsManager;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DnsInputStream extends ByteArrayInputStream {
    private final LabelsManager labelsManager;

    public DnsInputStream(LabelsManager labelsManager, byte[] buf) {
        super(buf);
        this.labelsManager = labelsManager;
    }

    public List<String> readLabels() {
        return new ArrayList<>(readLabels(pos));
    }

    /**
     * Reads a set of labels from a DNS message
     *
     * @param offset where to start reading.
     * @return the desired domain-name split into labels
     */
    @NotNull
    private Labels readLabels(int offset) {
        Labels labels = new Labels();

        int pos = offset;
        byte length;
        while ((length = buf[pos++]) != 0) {
            if (((length >> 6) & 0b00000011) == 0b00000011) {
                int loc = buf[pos++] | ((length & 0b00111111) << 8);
                labels.addAll(readLabels(loc));
                break;
            } else {
                int startingFrom = pos - 1;
                byte[] tmp = new byte[length];
                for (int i = 0; i < length; i++)
                    tmp[i] = buf[pos++];

                String label = new String(tmp);
                labels.add(label, startingFrom);
            }
        }

        this.pos = pos;

        labels.writeTo(labelsManager);

        return labels;
    }

    public int readInt() {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 /* << 0 */));
    }

    public short readShort() {
        int ch1 = read();
        int ch2 = read();
        return (short) ((ch1 << 8) + (ch2 /* << 0 */));
    }

    public byte readByte() {
        return (byte) read();
    }

    public int readBytes(byte[] buffer) {
        try {
            return read(buffer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class Labels extends ArrayList<String> {
        private final List<Integer> locations = new ArrayList<>();

        private Labels() {
        }

        void add(String s, int loc) {
            locations.add(loc);
            super.add(s);
        }

        public void addAll(Labels labels) {
            locations.addAll(labels.locations);
            super.addAll(labels);
        }

        void writeTo(LabelsManager labelsManager) {
            for (int i = size() - 1; i >= 0; i--)
                labelsManager.register(this, i, locations.get(i));
        }
    }
}
