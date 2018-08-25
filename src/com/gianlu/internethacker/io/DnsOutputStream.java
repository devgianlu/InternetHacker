package com.gianlu.internethacker.io;

import com.gianlu.internethacker.models.LabelsManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class DnsOutputStream extends ByteArrayOutputStream {
    private final LabelsManager labelsManager;

    public DnsOutputStream(LabelsManager labelsManager) {
        this.labelsManager = labelsManager;
    }

    public void writeLabels(List<String> labels) {
        try {
            labelsManager.writeLabels(this, labels);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeShort(short val) {
        write((val >>> 8) & 0xFF);
        write(val & 0xFF);
    }

    public void writeInt(int val) {
        write((val >>> 24) & 0xFF);
        write((val >>> 16) & 0xFF);
        write((val >>> 8) & 0xFF);
        write(val & 0xFF);
    }
}
