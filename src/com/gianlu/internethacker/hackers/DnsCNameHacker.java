package com.gianlu.internethacker.hackers;

import com.gianlu.internethacker.Utils;
import com.gianlu.internethacker.models.DnsMessage;
import com.gianlu.internethacker.models.DnsQuestion;
import com.gianlu.internethacker.models.DnsResourceRecord;
import com.gianlu.internethacker.models.rr.CNameRecord;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Change every <i>CNAME</i> record that matches the given domain with the corresponding substitute.
 */
public final class DnsCNameHacker implements DnsHacker {
    private final Map<String, String> map = new HashMap<>();

    public DnsCNameHacker(@NotNull String domain, @NotNull String substitute) {
        addDomainHack(domain, substitute);
    }

    public void addDomainHack(@NotNull String domain, @NotNull String substitute) {
        map.put(domain, substitute);
    }

    @Override
    public boolean interceptAnswerMessage(@NotNull DnsMessage answer) {
        for (String domain : map.keySet())
            for (DnsQuestion question : answer.questions)
                if (Objects.equals(question.getDomainName(), domain))
                    return true;

        return false;
    }

    @Override
    public @NotNull DnsMessage hackDnsAnswerMessage(@NotNull DnsMessage answer) {
        for (int i = 0; i < answer.answers.size(); i++) {
            DnsResourceRecord rr = answer.answers.get(i);
            switch (rr.type) {
                case CNAME:
                    String substitute = map.get(rr.getName());
                    if (substitute != null) {
                        List<String> labels = Arrays.asList(Utils.split(substitute, '.'));

                        rr = rr.buildUpon()
                                .setRData(answer, new CNameRecord(labels))
                                .build();
                    }
                    break;
            }

            answer.answers.set(i, rr);
        }

        return answer;
    }
}
