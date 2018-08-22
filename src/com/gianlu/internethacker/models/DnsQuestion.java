package com.gianlu.internethacker.models;


import com.gianlu.internethacker.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class DnsQuestion {
    public final List<String> qname;
    public final QType qtype;
    public final QClass qclass;

    public DnsQuestion(ByteBuffer data) {
        qname = DnsMessage.readLabels(data);
        qtype = QType.parse(data.getShort());
        qclass = QClass.parse(data.getShort());
    }

    public void write(OutputStream out) throws IOException {
        DnsMessage.writeLabels(out, qname);
        Utils.putShort(out, qtype.val);
        Utils.putShort(out, qclass.val);
    }

    public enum QType {
        A(1), NS(2), MD(3), MF(4), CNAME(5), SOA(6), MB(7), MG(8), MR(9),
        NULL(10), WKS(11), PTR(12), HINFO(13), MINFO(14), MX(15), TXT(16),
        AAAA(28), AXFR(252), MAILB(253), MAILA(254), ALL(255);

        private final short val;

        QType(int val) {
            this.val = (short) val;
        }

        @NotNull
        public static QType parse(int val) {
            for (QType type : values())
                if (type.val == val)
                    return type;

            throw new IllegalArgumentException("Unknown QTYPE for " + val);
        }
    }

    public enum QClass {
        IN(1),
        CH(3),
        HS(4),
        ANY(255);

        private final short val;

        QClass(int val) {
            this.val = (short) val;
        }

        @NotNull
        public static QClass parse(int val) {
            for (QClass clazz : values())
                if (clazz.val == val)
                    return clazz;

            throw new IllegalArgumentException("Unknown QCLASS for " + val);
        }
    }
}
