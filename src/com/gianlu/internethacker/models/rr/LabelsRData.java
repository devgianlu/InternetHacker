package com.gianlu.internethacker.models.rr;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class LabelsRData extends RData {
    private final List<String> labels;

    public LabelsRData(List<String> labels) {
        this.labels = labels;
    }

    public LabelsRData(DnsInputStream in) {
        labels = in.readLabels();
    }

    public List<String> getLabels() {
        return labels;
    }

    private String domain = null;

    @NotNull
    public String getDomain() {
        if (domain == null) domain = String.join(".", labels);
        return domain;
    }

    @Override
    public final void write(@NotNull DnsOutputStream out) {
        out.writeLabels(labels);
    }
}
