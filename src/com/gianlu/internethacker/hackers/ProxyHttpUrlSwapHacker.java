package com.gianlu.internethacker.hackers;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Swaps out URLs. If you want to modify the URL dynamically, implement {@link ProxyHttpUrlHacker}.
 */
public class ProxyHttpUrlSwapHacker implements ProxyHttpUrlHacker {
    private final Map<URL, URL> swaps = new HashMap<>();

    public ProxyHttpUrlSwapHacker(URL match, URL substitute) {
        addSwap(match, substitute);
    }

    public void addSwap(URL match, URL substitute) {
        if (match.getProtocol().equals("https"))
            throw new IllegalArgumentException("Cannot intercept HTTPS request with this hacker.");

        swaps.put(match, substitute);
    }

    @Override
    public boolean interceptRequest(String method, URL url) {
        return swaps.containsKey(url);
    }

    @Override
    public @NotNull URL getUrl(@NotNull URL match) {
        return swaps.get(match);
    }
}
