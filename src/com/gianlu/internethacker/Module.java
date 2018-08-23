package com.gianlu.internethacker;

import java.io.IOException;

public interface Module {

    void close() throws IOException;

    void start() throws IOException;
}
