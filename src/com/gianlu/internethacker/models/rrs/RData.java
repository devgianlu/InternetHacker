package com.gianlu.internethacker.models.rrs;

import com.gianlu.internethacker.models.DnsMessage;

import java.nio.ByteBuffer;

public abstract class RData {

    public RData(DnsMessage.LabelsWriter labelsWriter, ByteBuffer buffer, int offset, int rdlength) {
    }
}
