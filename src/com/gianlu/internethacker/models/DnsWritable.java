package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface DnsWritable {

    void write(@NotNull DnsOutputStream out) throws IOException;
}
