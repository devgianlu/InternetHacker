package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DnsMessage implements DnsWritable {
    public final DnsHeader header;
    public final List<DnsQuestion> questions;
    public final List<DnsResourceRecord> answers;
    public final List<DnsResourceRecord> authorities;
    public final List<DnsResourceRecord> additional;
    private final LabelsManager labelsManager = new LabelsManager();

    private DnsMessage(DnsHeader header, List<DnsQuestion> questions, List<DnsResourceRecord> answers, List<DnsResourceRecord> authorities, List<DnsResourceRecord> additional) {
        this.header = header;
        this.questions = questions;
        this.answers = answers;
        this.authorities = authorities;
        this.additional = additional;
    }

    public DnsMessage(byte[] data) {
        DnsInputStream in = new DnsInputStream(labelsManager, data);

        header = new DnsHeader(in);

        questions = new ArrayList<>(header.qdcount);
        for (int i = 0; i < header.qdcount; i++)
            questions.add(new DnsQuestion(in));

        answers = new ArrayList<>(header.ancount);
        for (int i = 0; i < header.ancount; i++)
            answers.add(new DnsResourceRecord(in));

        authorities = new ArrayList<>(header.nscount);
        for (int i = 0; i < header.nscount; i++)
            authorities.add(new DnsResourceRecord(in));

        additional = new ArrayList<>(header.arcount);
        for (int i = 0; i < header.arcount; i++)
            additional.add(new DnsResourceRecord(in));
    }

    @NotNull
    public Builder buildUpon() {
        return new Builder(this);
    }

    @NotNull
    public DnsOutputStream write() throws IOException {
        DnsOutputStream out = new DnsOutputStream(labelsManager);
        write(out);
        return out;
    }

    @Override
    public void write(@NotNull DnsOutputStream out) throws IOException {
        header.write(out);

        for (DnsQuestion question : questions)
            question.write(out);

        for (DnsResourceRecord rr : answers)
            rr.write(out);

        for (DnsResourceRecord rr : authorities)
            rr.write(out);

        for (DnsResourceRecord rr : additional)
            rr.write(out);
    }

    @NotNull
    public DnsOutputStream createEmptyStream() {
        return new DnsOutputStream(labelsManager);
    }

    public static class Builder {
        private final List<DnsQuestion> questions = new ArrayList<>();
        private final List<DnsResourceRecord> answers = new ArrayList<>();
        private final List<DnsResourceRecord> authorities = new ArrayList<>();
        private final List<DnsResourceRecord> additional = new ArrayList<>();
        private DnsHeader header;

        private Builder(DnsMessage copy) {
            this.header = new DnsHeader(copy.header);
            this.questions.addAll(copy.questions);
            this.answers.addAll(copy.answers);
            this.authorities.addAll(copy.authorities);
            this.additional.addAll(copy.additional);
        }

        public Builder setHeader(DnsHeader header) {
            this.header = header;
            return this;
        }

        public Builder addQuestion(DnsQuestion question) {
            questions.add(question);
            return this;
        }

        public Builder addAnswer(DnsResourceRecord rr) {
            answers.add(rr);
            return this;
        }

        public Builder addAuhtority(DnsResourceRecord rr) {
            authorities.add(rr);
            return this;
        }

        public Builder addAdditional(DnsResourceRecord rr) {
            additional.add(rr);
            return this;
        }

        @NotNull
        public DnsMessage build() {
            if (header == null) throw new IllegalStateException("Missing header!");
            if (header.qdcount != questions.size()) throw new IllegalStateException();
            if (header.ancount != answers.size()) throw new IllegalStateException();
            if (header.nscount != authorities.size()) throw new IllegalStateException();
            if (header.arcount != additional.size()) throw new IllegalStateException();
            return new DnsMessage(header, questions, answers, authorities, additional);
        }
    }
}
