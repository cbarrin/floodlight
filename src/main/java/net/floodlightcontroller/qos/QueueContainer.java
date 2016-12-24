package net.floodlightcontroller.qos;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueueProp;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.protocol.ver13.*;

import java.io.IOException;
import java.util.*;

/**
 *
 * Created by geddingsbarrineau on 12/19/16.
 */
public class QueueContainer implements IOFSwitchListener {
    private Map<DatapathId, List<OFPacketQueue>> activeSwitchQueues;
    private Map<DatapathId, List<OFPacketQueue>> inactiveSwitchQueues;

    QueueContainer(Map<String, String> configParams) {
        String switchesInitialQueues = configParams.get("switchesInitialQueues");
        inactiveSwitchQueues = getInitialQueueMapFromJson(switchesInitialQueues);
        moveActiveQueues();
    }

    private void moveActiveQueues() {
        for (DatapathId dpid : QoS.switchService.getAllSwitchDpids()) {
            if (inactiveSwitchQueues.containsKey(dpid)) {
                activeSwitchQueues.put(dpid, inactiveSwitchQueues.remove(dpid));
            }
        }
    }

    Map<DatapathId, List<OFPacketQueue>> getQueues() {
        return activeSwitchQueues;
    }

    List<OFPacketQueue> getQueuesOnSwitch(DatapathId dpid) {
        return activeSwitchQueues.get(dpid);
    }

    Map<DatapathId, List<OFPacketQueue>> getInitialQueueMapFromJson(String json) {
        Map<DatapathId, List<OFPacketQueue>> queues = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OFPacketQueue.class, new QueueDeserializer());
        module.addKeyDeserializer(DatapathId.class, new DatapathIdDeserializer());
        mapper.registerModule(module);

        TypeReference<HashMap<DatapathId,List<OFPacketQueue>>> typeRef
                = new TypeReference<HashMap<DatapathId,List<OFPacketQueue>>>() {};
        try {
            queues = mapper.readValue(json, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return queues;
    }

    @Override
    public void switchAdded(DatapathId switchId) {
        // Move inactive switch queues to active or create empty list in active map.
        if (inactiveSwitchQueues.containsKey(switchId)) {
            activeSwitchQueues.put(switchId, inactiveSwitchQueues.remove(switchId));
        } else if (!activeSwitchQueues.containsKey(switchId)) {
            activeSwitchQueues.put(switchId, new ArrayList<>());
        }
    }

    @Override
    public void switchRemoved(DatapathId switchId) {
        // Move active switch queues to inactive.
        if (activeSwitchQueues.containsKey(switchId)) {
            inactiveSwitchQueues.put(switchId, activeSwitchQueues.remove(switchId));
        }
    }

    @Override
    public void switchActivated(DatapathId switchId) {

    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {

    }

    @Override
    public void switchChanged(DatapathId switchId) {

    }

    @Override
    public void switchDeactivated(DatapathId switchId) {

    }
}
