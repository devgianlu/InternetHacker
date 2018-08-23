package com.gianlu.internethacker.hackers;

import com.gianlu.internethacker.models.DnsMessage;
import org.jetbrains.annotations.NotNull;

public interface DnsHacker {

    /**
     * Whether to intercept this {@link DnsMessage} to modify its content before it is returned to the client.
     *
     * @param answer the answer received from the <i>real</i> server.
     * @return whether this {@link DnsHacker} should intercept this {@link DnsMessage}.
     */
    boolean interceptAnswerMessage(@NotNull DnsMessage answer);

    /**
     * Modify the {@link DnsMessage} and return the modified instance, this instance will be fed into the next {@link DnsHacker}.
     *
     * @param answer the answer received from the <i>real</i> server.
     * @return the modified answer, can be the same instance as {@param answer}.
     */
    @NotNull
    DnsMessage hackDnsAnswerMessage(@NotNull DnsMessage answer);
}
