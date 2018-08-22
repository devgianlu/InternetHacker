package com.gianlu.internethacker.models;

import com.gianlu.internethacker.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class DnsHeader {
    public final short id;
    public final boolean qr;
    public final OpCode opcode;
    public final boolean aa;
    public final boolean tc;
    public final boolean rd;
    public final boolean ra;
    public final RCode rcode;
    public final short qdcount;
    public final short ancount;
    public final short nscount;
    public final short arcount;

    public DnsHeader(ByteBuffer data) {
        id = data.getShort();

        byte byte2 = data.get();
        qr = ((byte2 >> 7) & 0b00000001) != 0;
        opcode = OpCode.parse((byte2 >> 3) & 0b00001111);
        aa = ((byte2 >> 2) & 0b00000001) != 0;
        tc = ((byte2 >> 1) & 0b00000001) != 0;
        rd = ((byte2  /* >> 0 */) & 0b00000001) != 0;

        byte byte3 = data.get();
        ra = ((byte3 >> 7) & 0b00000001) != 0;
        int z = (byte3 >> 4) & 0b00000111;
        if (z != 0) throw new RuntimeException("Z should be 0, something went wrong!");
        rcode = RCode.parse((byte3 /* >> 0 */) & 0b00001111);

        qdcount = data.getShort();
        ancount = data.getShort();
        nscount = data.getShort();
        arcount = data.getShort();
    }

    public void write(OutputStream out) throws IOException {
        Utils.putShort(out, id);

        byte b = 0;
        b |= (qr ? 1 : 0) << 7;
        b |= opcode.val << 3;
        b |= (aa ? 1 : 0) << 2;
        b |= (tc ? 1 : 0) << 1;
        b |= (rd ? 1 : 0) /* << 0 */;
        out.write(b);

        b = 0;
        b |= (ra ? 1 : 0) << 7;
        b |= 0 /* << 4 */; /* z */
        b |= rcode.val /* << 0 */;
        out.write(b);

        Utils.putShort(out, qdcount);
        Utils.putShort(out, ancount);
        Utils.putShort(out, nscount);
        Utils.putShort(out, arcount);
    }

    public enum RCode {
        NO_ERROR(0),
        FORMAT_ERROR(1),
        SERVER_FAILURE(2),
        NAME_ERROR(3),
        NOT_IMPLEMENTED(4),
        REFUSED(5);

        private final int val;

        RCode(int val) {
            this.val = val;
        }

        @NotNull
        public static RCode parse(int val) {
            for (RCode code : values())
                if (code.val == val)
                    return code;

            throw new IllegalArgumentException("Unknown RCODE for " + val);
        }
    }

    public enum OpCode {
        QUERY(0),
        IQUERY(1),
        STATUS(2);

        private final int val;

        OpCode(int val) {
            this.val = val;
        }

        @NotNull
        public static OpCode parse(int val) {
            for (OpCode code : values())
                if (code.val == val)
                    return code;

            throw new IllegalArgumentException("Unknown OPCODE for " + val);
        }
    }
}
