package com.gianlu.internethacker.hackers;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Changes an URL with another which can be completely different or partly modified.
 * If used, it must be the only hacker in the list.
 */
public interface ProxyHttpUrlHacker extends ProxyHttpHacker {

    @NotNull
    URL getUrl(@NotNull URL match);
}
