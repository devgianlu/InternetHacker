package com.gianlu.internethacker.hackers;

import java.net.URL;

public interface ProxyHttpHacker extends ProxyHacker {

    boolean interceptRequest(String method, URL url);
}
