package net.floodlightcontroller.qos;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueueProp;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueuePropMaxRate;

/**
 * Created by geddingsbarrineau on 1/5/17.
 */
public class Rule {

    private Match match;
    private OFQueuePropMaxRate maxRate;

    public Rule(Match match, OFQueuePropMaxRate maxRate) {
        this.match = match;
        this.maxRate = maxRate;
    }

    public Rule(Match match, int maxRate) {
        this.match = match;
        this.maxRate = OFFactories.getFactory(OFVersion.OF_13)
                .queueProps().buildMaxRate().setRate(maxRate).build();
    }

    public Match getMatch() {
        return match;
    }

    public OFQueueProp getMaxRate() {
        return maxRate;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "match=" + match +
                ", maxRate=" + maxRate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;

        if (match != null ? !match.equals(rule.match) : rule.match != null) return false;
        return maxRate != null ? maxRate.equals(rule.maxRate) : rule.maxRate == null;
    }

    @Override
    public int hashCode() {
        int result = match != null ? match.hashCode() : 0;
        result = 31 * result + (maxRate != null ? maxRate.hashCode() : 0);
        return result;
    }
}
