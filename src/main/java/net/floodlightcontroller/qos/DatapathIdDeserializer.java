package net.floodlightcontroller.qos;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.projectfloodlight.openflow.types.DatapathId;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by geddingsbarrineau on 12/22/16.
 */
public class DatapathIdDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return DatapathId.of(key);
    }
}
