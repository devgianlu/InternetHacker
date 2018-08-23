package com.gianlu.internethacker;

import com.gianlu.internethacker.hackers.DnsHacker;
import com.gianlu.internethacker.hackers.ProxyHacker;

import java.io.IOException;

public final class InternetHacker {
    private DnsModule dns = null;
    private ProxyModule proxy = null;

    private InternetHacker() {
    }

    public static InternetHacker create() {
        return new InternetHacker();
    }

    /**
     * Setup the {@link ProxyModule} on port {@param port}
     *
     * @param hackers if none is given, the proxy won't manipulate the data, otherwise they are applied in the given order
     */
    public InternetHacker useProxy(int port, ProxyHacker... hackers) {
        proxy = new ProxyModule(port, hackers);
        return this;
    }

    /**
     * Setup the {@link DnsModule} on port 53
     *
     * @param hackers if none is given, the DNS will behave like a proxy, otherwise they are applied in the given order
     */
    public InternetHacker useDns(DnsHacker... hackers) {
        dns = new DnsModule(53, hackers);
        return this;
    }

    /**
     * Start the servers
     */
    public void start() throws IOException {
        if (dns != null) dns.start();
        if (proxy != null) proxy.start();
    }
}
