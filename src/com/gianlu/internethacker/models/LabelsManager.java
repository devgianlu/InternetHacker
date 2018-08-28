package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelsManager {
    private Map<String, Integer> map = new HashMap<>();

    public LabelsManager() {
    }

    @NotNull
    private static String buildDomain(List<String> labels, int from) {
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < labels.size(); i++) {
            if (i != from) builder.append('.');
            builder.append(labels.get(i));
        }
        return builder.toString();
    }

    private short search(List<String> labels, int from) {
        Integer pos = map.get(buildDomain(labels, from));
        return (short) (pos == null ? -1 : pos);
    }

    public void register(List<String> labels, int from, int loc) {
        if (loc < 64) map.put(buildDomain(labels, from), loc);
    }

    private short searchFull(List<String> labels) {
        return search(labels, 0);
    }

    /**
     * Write labels to a DNS message.
     *
     * @param out    the {@link java.io.OutputStream} to write to
     * @param labels the labels to write.
     */
    public void writeLabels(DnsOutputStream out, List<String> labels) throws IOException {
        short loc = searchFull(labels);
        if (loc != -1) {
            out.writePointer(loc);
            return;
        }

        for (int i = 0; i < labels.size(); i++) {
            loc = i == 0 ? -1 : search(labels, i);
            if (loc == -1) {
                register(labels, i, out.size());
                String label = labels.get(i);
                out.write(label.length());
                out.write(label.getBytes());
            } else {
                out.writePointer(loc);
                return;
            }
        }

        out.write(0);
    }
}
