package com.gianlu.internethacker.models.rr;

import com.gianlu.internethacker.io.DnsInputStream;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CNameRecord extends LabelsRData {
    public CNameRecord(List<String> labels) {
        super(labels);
    }

    @DnsInputStreamConstructor
    public CNameRecord(@NotNull DnsInputStream in) {
        super(in.readLabels());
    }
}
