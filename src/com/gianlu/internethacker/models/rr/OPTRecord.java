package com.gianlu.internethacker.models.rr;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gianlu
 */
public class OPTRecord extends RData {
    public final List<Option> options;

    @DnsInputStreamConstructor
    public OPTRecord(@NotNull DnsInputStream in) {
        options = new ArrayList<>();

        while (in.available() > 0) {
            options.add(new Option(in));
        }
    }

    @Override
    public void write(@NotNull DnsOutputStream out) {
    }

    public static class Option {
        public final short opcode;
        public final short oplength;
        public final byte[] opdata;

        private Option(@NotNull DnsInputStream in) {
            opcode = in.readShort();
            oplength = in.readShort();
            opdata = new byte[oplength];
            in.readBytes(opdata);
        }
    }
}
