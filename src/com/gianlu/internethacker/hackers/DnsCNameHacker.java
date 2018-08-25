package com.gianlu.internethacker.hackers;

import com.gianlu.internethacker.Utils;
import com.gianlu.internethacker.models.DnsMessage;
import com.gianlu.internethacker.models.DnsQuestion;
import com.gianlu.internethacker.models.DnsResourceRecord;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DnsCNameHacker implements DnsHacker {
    private final static Logger logger = Logger.getLogger(DnsCNameHacker.class.getName());
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

                        try {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            answer.writeLabels(out, labels);

                            rr = rr.buildUpon()
                                    .setRdata(out.toByteArray())
                                    .build();
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, "Failed writing label.", ex);
                        }
                    }
                    break;
            }

            answer.answers.set(i, rr);
        }

        return answer;
    }
}
