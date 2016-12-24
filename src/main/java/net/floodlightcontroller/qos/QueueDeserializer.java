package net.floodlightcontroller.qos;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueueProp;
import org.projectfloodlight.openflow.types.OFPort;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by geddingsbarrineau on 12/19/16.
 */
public class QueueDeserializer extends StdDeserializer<OFPacketQueue> {

    public QueueDeserializer() {
        this(null);
    }

    public QueueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public OFPacketQueue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OFQueueProp.class, new QueuePropertiesDeserializer());
        mapper.registerModule(module);
        JsonNode node = jp.getCodec().readTree(jp);

        long queueId = node.get("queueId").numberValue().longValue();
        OFPort port = OFPort.of((int) node.get("port").numberValue());

        TypeFactory typeFactory = mapper.getTypeFactory();
        List<OFQueueProp> properties =
                mapper.readValue(node.get("properties").toString(), typeFactory.constructCollectionType(List.class,
                        OFQueueProp.class));
        return OFFactories.getFactory(OFVersion.OF_13)
                .buildPacketQueue()
                .setQueueId(queueId)
                .setPort(port)
                .setProperties(properties)
                .build();
    }
}
