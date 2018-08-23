package com.gianlu.internethacker.hackers;

import com.gianlu.internethacker.models.DnsMessage;
import com.gianlu.internethacker.models.DnsQuestion;
import com.gianlu.internethacker.models.DnsResourceRecord;
import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Change every <i>A</i> or <i>AAAA</i> resource record that matches the given domain with the corresponding IP.
 * <p>
 * If both an IPv4 and IPv6 addresses are given, both <i>A</i> and <i>AAAA</i> are changed. If one substitute is missing,
 * the record will be removed.
 */
public final class DnsAddressHacker implements DnsHacker {
    private final List<HackPair> hacks = new ArrayList<>();

    public DnsAddressHacker(String domain, Inet4Address hack) {
        addDomainHack(domain, hack);
    }

    public DnsAddressHacker(String domain, Inet6Address hack) {
        addDomainHack(domain, hack);
    }

    public DnsAddressHacker(String domain, Inet4Address ipv4hack, Inet6Address ipv6hack) {
        addDomainHack(domain, ipv4hack, ipv6hack);
    }

    public void addDomainHack(String domain, Inet4Address hack) {
        hacks.add(new HackPair(domain, hack, null));
    }

    public void addDomainHack(String domain, Inet6Address hack) {
        hacks.add(new HackPair(domain, null, hack));
    }

    public void addDomainHack(String domain, Inet4Address ipv4hack, Inet6Address ipv6hack) {
        hacks.add(new HackPair(domain, ipv4hack, ipv6hack));
    }

    @Override
    public boolean interceptAnswerMessage(@NotNull DnsMessage answer) {
        for (DnsQuestion question : answer.questions)
            for (HackPair hack : hacks)
                if (Objects.equals(question.getDomainName(), hack.domainName))
                    return true;

        return false;
    }

    @Override
    public @NotNull DnsMessage hackDnsAnswerMessage(@NotNull DnsMessage answer) {
        for (HackPair hack : hacks)
            answer = hack.manipulate(answer);

        return answer;
    }

    private static class HackPair {
        private final String domainName;
        private final Inet4Address ipv4hack;
        private final Inet6Address ipv6hack;

        HackPair(String domainName, Inet4Address ipv4hack, Inet6Address ipv6hack) {
            this.domainName = domainName;
            this.ipv4hack = ipv4hack;
            this.ipv6hack = ipv6hack;
        }

        @NotNull
        DnsMessage manipulate(@NotNull DnsMessage answer) {
            boolean changedSize = false;
            for (int i = 0; i < answer.answers.size(); i++) {
                DnsResourceRecord rr = answer.answers.get(i);
                switch (rr.type) {
                    case A:
                        if (ipv4hack != null) {
                            rr = rr.buildUpon()
                                    .setRdata(ipv4hack.getAddress())
                                    .build();
                        } else {
                            rr = null;
                        }
                        break;
                    case AAAA:
                        if (ipv6hack != null) {
                            rr = rr.buildUpon()
                                    .setRdata(ipv6hack.getAddress())
                                    .build();
                        } else {
                            rr = null;
                        }
                        break;
                }

                if (rr == null) {
                    answer.answers.remove(i);
                    changedSize = true;
                } else {
                    answer.answers.set(i, rr);
                }
            }

            if (changedSize) {
                return answer.buildUpon().setHeader(answer.header.buildUpon()
                        .setAncount((short) answer.answers.size())
                        .build()).build();
            } else {
                return answer;
            }
        }
    }
}
