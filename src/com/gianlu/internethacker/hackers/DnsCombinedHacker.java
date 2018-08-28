package com.gianlu.internethacker.hackers;

import com.gianlu.internethacker.Utils;
import com.gianlu.internethacker.models.DnsMessage;
import com.gianlu.internethacker.models.DnsResourceRecord;
import com.gianlu.internethacker.models.rr.AAAARecord;
import com.gianlu.internethacker.models.rr.ARecord;
import com.gianlu.internethacker.models.rr.CNameRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Change every <i>A</i>, <i>AAAA</i>, <i>CNAME</i> resource record to guarantee that the substitute domain is contacted instead.
 * <p>
 * This is useful when the domain is defined through a <i>CNAME</i> record.
 * This {@link DnsHacker} will change the <i>CNAME</i> record, but also <i>A</i> and <i>AAAA</i> records accordingly.
 *
 * @author Gianlu
 */
public final class DnsCombinedHacker implements DnsHacker {
    private static final Logger logger = Logger.getLogger(DnsCombinedHacker.class.getName());
    private final List<Hack> hacks = new ArrayList<>();

    public DnsCombinedHacker(@NotNull String domain, @NotNull String hack) throws UnknownHostException {
        addDomainHack(domain, hack);
    }

    public DnsCombinedHacker() {
    }

    public void addDomainHack(@NotNull String domain, @NotNull String hack) throws UnknownHostException {
        hacks.add(new Hack(domain, hack));
    }

    @Override
    public boolean interceptAnswerMessage(@NotNull DnsMessage answer) {
        boolean intercept = false;
        for (DnsResourceRecord rr : answer.answers) {
            Hack hack = findForTarget(rr.getName());
            if (hack != null) {
                intercept = true;

                if (rr.getType() == DnsResourceRecord.Type.CNAME)
                    hack.registerCName(((CNameRecord) rr.getRData(answer)).getDomain());
            }
        }

        return intercept;
    }

    @Override
    public @NotNull DnsMessage hackDnsAnswerMessage(@NotNull DnsMessage answer) {
        boolean changedSize = false;
        for (int i = answer.answers.size() - 1; i >= 0; i--) {
            DnsResourceRecord rr = answer.answers.get(i);
            Hack hack;
            switch (rr.getType()) {
                case A:
                    hack = findForAddressTarget(rr.getName());
                    if (hack == null) continue;

                    if (hack.availableIpv4Addresses.isEmpty()) {
                        answer.answers.remove(i);
                        changedSize = true;
                    } else {
                        Inet4Address addr = hack.availableIpv4Addresses.remove();
                        answer.answers.set(i, rr.buildUpon(answer)
                                .setRData(new ARecord(addr))
                                .build());

                        logger.info("Hacked A " + rr.getName() + " to " + addr);
                    }
                    break;
                case AAAA:
                    hack = findForAddressTarget(rr.getName());
                    if (hack == null) continue;

                    if (hack.availableIpv6Addresses.isEmpty()) {
                        answer.answers.remove(i);
                        changedSize = true;
                    } else {
                        Inet6Address addr = hack.availableIpv6Addresses.remove();
                        answer.answers.set(i, rr.buildUpon(answer)
                                .setRData(new AAAARecord(addr))
                                .build());

                        logger.info("Hacked AAAA " + rr.getName() + " to " + addr);
                    }
                    break;
                case CNAME:
                    hack = findForTarget(rr.getName());
                    if (hack != null) {
                        List<String> labels = Arrays.asList(Utils.split(hack.substitute, '.'));

                        answer.answers.set(i, rr.buildUpon(answer)
                                .setRData(new CNameRecord(labels))
                                .build());

                        logger.info("Hacked CNAME " + rr.getName() + " to " + hack.substitute);
                    }
                    break;
            }
        }

        reset();

        if (changedSize) {
            return answer.buildUpon().setHeader(answer.header.buildUpon()
                    .setANCount((short) answer.answers.size())
                    .build()).build();
        } else {
            return answer;
        }
    }

    @Nullable
    private Hack findForAddressTarget(@NotNull String target) {
        Hack hack = findForCNameTarget(target);
        if (hack == null) hack = findForTarget(target);
        return hack;
    }

    @Nullable
    private Hack findForCNameTarget(@NotNull String target) {
        for (Hack hack : hacks)
            if (hack.cnames.contains(target))
                return hack;

        return null;
    }

    @Nullable
    private Hack findForTarget(@NotNull String target) {
        for (Hack hack : hacks)
            if (hack.target.equals(target))
                return hack;

        return null;
    }

    private void reset() {
        for (Hack hack : hacks)
            hack.resetAvailableAddresses();
    }

    private class Hack {
        private final InetAddress[] addresses;
        private final String target;
        private final String substitute;
        private final List<String> cnames = new ArrayList<>();
        private final Queue<Inet4Address> availableIpv4Addresses = new ArrayDeque<>();
        private final Queue<Inet6Address> availableIpv6Addresses = new ArrayDeque<>();

        Hack(@NotNull String domain, @NotNull String hack) throws UnknownHostException {
            this.target = domain;
            this.substitute = hack;
            this.addresses = InetAddress.getAllByName(hack);

            resetAvailableAddresses();
        }

        void registerCName(String domain) {
            cnames.add(domain);
        }

        void resetAvailableAddresses() {
            availableIpv4Addresses.clear();
            availableIpv6Addresses.clear();

            for (InetAddress addr : addresses) {
                if (addr instanceof Inet4Address)
                    availableIpv4Addresses.add((Inet4Address) addr);
                else if (addr instanceof Inet6Address)
                    availableIpv6Addresses.add((Inet6Address) addr);
            }
        }
    }
}
