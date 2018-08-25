package com.gianlu.internethacker.models.rrs;

import com.gianlu.internethacker.models.DnsMessage;

import java.nio.ByteBuffer;
import java.util.List;

public class CNAMERecord extends RData {
    public final List<String> cname;

    public CNAMERecord(DnsMessage.LabelsWriter labelsWriter, ByteBuffer buffer, int offset, int rdlength) {
        super(labelsWriter, buffer, offset, rdlength);

        cname = DnsMessage.readLabels(labelsWriter, buffer, offset);
    }
}
