package net.floodlightcontroller.qos;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geddingsbarrineau on 1/5/17.
 */
public class RuleHandler {
    private List<Rule> rules;

    public RuleHandler() {
        rules = new ArrayList<>();
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public void addEthTypeRule(EthType type, int maxRate) {
        Match match = OFFactories.getFactory(OFVersion.OF_13)
                .buildMatch().setExact(MatchField.ETH_TYPE, type).build();
        Rule rule = new Rule(match, maxRate);
        rules.add(rule);
    }
}
