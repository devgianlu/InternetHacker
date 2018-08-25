package com.gianlu.internethacker.hackers;

import com.gianlu.internethacker.Utils;
import com.gianlu.internethacker.io.DnsOutputStream;
import com.gianlu.internethacker.models.DnsMessage;
import com.gianlu.internethacker.models.DnsQuestion;
import com.gianlu.internethacker.models.DnsResourceRecord;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DnsCNameHacker implements DnsHacker {
    private final Map<String, String> map = new HashMap<>();

    public DnsCNameHacker(String domain, String substitute) {
        addDomainHack(domain, substitute);
    }

    public void addDomainHack(String domain, String substitute) {
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
                        DnsOutputStream out = answer.createEmptyStream();
                        out.writeLabels(labels);

                        rr = rr.buildUpon()
                                .setRdata(out.toByteArray())
                                .build();
                    }
                    break;
            }

            answer.answers.set(i, rr);
        }

        return answer;
    }
}
