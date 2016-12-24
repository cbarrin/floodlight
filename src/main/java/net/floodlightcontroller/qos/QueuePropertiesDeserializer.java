package net.floodlightcontroller.qos;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueueProp;
import org.projectfloodlight.openflow.types.OFPort;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by geddingsbarrineau on 12/19/16.
 */
public class QueuePropertiesDeserializer extends StdDeserializer<OFQueueProp> {
    public QueuePropertiesDeserializer() {
        this(null);
    }

    public QueuePropertiesDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public OFQueueProp deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        Iterator<JsonNode> elements = node.elements();

        int rate;
        if (node.has("min-rate")) {
            rate = (int) node.get("min-rate").numberValue();
            return OFFactories.getFactory(OFVersion.OF_13).queueProps().minRate(rate);
        } else if (node.has("max-rate")) {
            rate = (int) node.get("max-rate").numberValue();
            return OFFactories.getFactory(OFVersion.OF_13).queueProps().maxRate(rate);
        }

        return null;
    }
}
