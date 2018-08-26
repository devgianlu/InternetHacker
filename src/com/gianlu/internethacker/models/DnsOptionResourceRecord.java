package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsInputStream;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Gianlu
 */
public class DnsOptionResourceRecord extends DnsBareResourceRecord {
    public final short opcode;
    public final short oplength;
    public final byte[] opdata;

    DnsOptionResourceRecord(List<String> name, short type, @NotNull DnsInputStream in) {
        super(name, type, in);

        opcode = in.readShort();
        oplength = in.readShort();

        opdata = new byte[oplength];
        in.readBytes(opdata);
    }
}
