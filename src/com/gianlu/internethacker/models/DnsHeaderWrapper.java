package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public abstract class DnsHeaderWrapper implements DnsWritable {
    protected final DnsBareHeader header;

    DnsHeaderWrapper(@NotNull DnsBareHeader header) {
        this.header = header;
    }

    @NotNull
    static DnsHeaderWrapper parse(@NotNull DnsMessage message, @NotNull DnsBareHeader bareHeader) {
        for (DnsResourceRecord rr : message.additional)
            if (rr.getType() == DnsResourceRecord.Type.OPT)
                return new DnsExtendedHeader(bareHeader, rr);

        return new DnsStandardHeader(bareHeader);
    }

    @Override
    public final void write(@NotNull DnsOutputStream out) {
        header.write(out);
    }

    public abstract short id();

    public abstract boolean qr();

    public abstract int opcode();

    public abstract boolean aa();

    public abstract boolean tc();

    public abstract boolean rd();

    public abstract boolean ra();

    public abstract int z();

    public abstract int rcode();

    public abstract short qdcount();

    public abstract short ancount();

    public abstract short nscount();

    public abstract short arcount();

    @NotNull
    public Builder buildUpon() {
        return new Builder(this.header);
    }

    public static class Builder {
        private short id;
        private int qr;
        private int opcode;
        private int aa;
        private int tc;
        private int rd;
        private int ra;
        private int z;
        private int rcode;
        private short qdcount;
        private short ancount;
        private short nscount;
        private short arcount;

        private Builder(DnsBareHeader header) {
            id = header.id;
            qr = header.qr;
            opcode = header.opcode;
            aa = header.aa;
            tc = header.tc;
            rd = header.rd;
            ra = header.ra;
            z = header.z;
            rcode = header.rcode;
            qdcount = header.qdcount;
            ancount = header.ancount;
            nscount = header.nscount;
            arcount = header.arcount;
        }

        public Builder() {
        }

        public Builder setID(short id) {
            this.id = id;
            return this;
        }

        public Builder setZ(int z) {
            this.z = z;
            return this;
        }

        public Builder setQR(boolean qr) {
            this.qr = qr ? 1 : 0;
            return this;
        }

        public Builder setOpCode(DnsBareHeader.OpCode opcode) {
            this.opcode = opcode.val;
            return this;
        }

        public Builder setAA(boolean aa) {
            this.aa = aa ? 1 : 0;
            return this;
        }

        public Builder setTC(boolean tc) {
            this.tc = tc ? 1 : 0;
            return this;
        }

        public Builder setRD(boolean rd) {
            this.rd = rd ? 1 : 0;
            return this;
        }

        public Builder setRA(boolean ra) {
            this.ra = ra ? 1 : 0;
            return this;
        }

        public Builder setRCode(DnsBareHeader.RCode rcode) {
            this.rcode = rcode.val;
            return this;
        }

        public Builder setQDCount(short qdcount) {
            this.qdcount = qdcount;
            return this;
        }

        public Builder setANCount(short ancount) {
            this.ancount = ancount;
            return this;
        }

        public Builder setNSCount(short nscount) {
            this.nscount = nscount;
            return this;
        }

        public Builder setARCount(short arcount) {
            this.arcount = arcount;
            return this;
        }

        @NotNull
        public DnsBareHeader build() {
            return new DnsBareHeader(id, qr, opcode, aa, tc, rd, ra, z, rcode, qdcount, ancount, nscount, arcount);
        }
    }
}
