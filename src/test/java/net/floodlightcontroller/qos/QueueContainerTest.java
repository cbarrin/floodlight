package net.floodlightcontroller.qos;

import net.floodlightcontroller.test.FloodlightTestCase;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueueProp;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by geddingsbarrineau on 12/19/16.
 */
public class QueueContainerTest extends FloodlightTestCase {


    private QueueContainer queueContainer;

    //language=JSON
    String json = "{\"00:00:00:00:00:00:00:01\":[{\"queueId\":1,\"port\":1," +
            "\"properties\":[{\"min-rate\":3000000},{\"max-rate\":5000000}]},{\"queueId\":2,\"port\":2," +
            "\"properties\":[{\"min-rate\":5000000},{\"max-rate\":7000000}]}]," +
            "\"00:00:00:00:00:00:00:02\":[{\"queueId\":1,\"port\":1,\"properties\":[{\"min-rate\":3000000}," +
            "{\"max-rate\":5000000}]},{\"queueId\":2,\"port\":2,\"properties\":[{\"min-rate\":5000000},{\"max-rate\":" +
            " 7000000}]}]}";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Map<String, String> configParams = new HashMap<>();
        configParams.put("switchesInitialQueues", json);
        queueContainer = new QueueContainer(configParams, mockSwitchManager);
    }

    @Test
    public void testGetInactiveQueues() {
        List<OFPacketQueue> expectedQueues = new ArrayList<>();
        expectedQueues.add(createQueue(1, 1, 3000000, 5000000));
        expectedQueues.add(createQueue(2, 2, 5000000, 7000000));
        List<OFPacketQueue> queues = queueContainer.getInactiveQueuesOnSwitch(DatapathId.of("00:00:00:00:00:00:00:01"));
        assertEquals(expectedQueues, queues);
    }

    @Test
    public void testJsonParsing() {
        Map<DatapathId, List<OFPacketQueue>> expectedQueues = new HashMap<>();
        List<OFPacketQueue> queueList = new ArrayList<>();
        queueList.add(createQueue(1, 1, 3000000, 5000000));
        queueList.add(createQueue(2, 2, 5000000, 7000000));
        expectedQueues.put(DatapathId.of(1), queueList);
        expectedQueues.put(DatapathId.of(2), queueList);
        Map<DatapathId, List<OFPacketQueue>> queues = queueContainer.getInitialQueueMapFromJson(json);
        assertEquals(expectedQueues, queues);
    }

    @Test
    public void testIntroductionOfQueuesToActiveListWhenSwitchIsAdded() {
        Map<DatapathId, List<OFPacketQueue>> expectedQueues = new HashMap<>();
        assertEquals(expectedQueues, queueContainer.getActiveQueues());

        List<OFPacketQueue> queueList = new ArrayList<>();
        queueList.add(createQueue(1, 1, 3000000, 5000000));
        queueList.add(createQueue(2, 2, 5000000, 7000000));
        expectedQueues.put(DatapathId.of(1), queueList);

        queueContainer.switchAdded(DatapathId.of(1));

        assertEquals(expectedQueues, queueContainer.getActiveQueues());
    }

    @Test
    public void testRemovalOfQueuesFromInactiveListWhenSwitchIsAdded() {
        Map<DatapathId, List<OFPacketQueue>> expectedQueues = new HashMap<>();
        List<OFPacketQueue> queueList = new ArrayList<>();
        queueList.add(createQueue(1, 1, 3000000, 5000000));
        queueList.add(createQueue(2, 2, 5000000, 7000000));
        expectedQueues.put(DatapathId.of(1), queueList);
        expectedQueues.put(DatapathId.of(2), queueList);

        assertEquals(expectedQueues, queueContainer.getInactiveQueues());

        queueContainer.switchAdded(DatapathId.of(1));

        expectedQueues.remove(DatapathId.of(1));
        assertEquals(expectedQueues, queueContainer.getInactiveQueues());
    }

    private OFPacketQueue createQueue(long queueId, int port, int minRate, int maxRate) {
        OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);
        List<OFQueueProp> properties = new ArrayList<>();
        properties.add(factory.queueProps().minRate(minRate));
        properties.add(factory.queueProps().maxRate(maxRate));
        return factory.buildPacketQueue()
                .setQueueId(queueId)
                .setPort(OFPort.of(port))
                .setProperties(properties)
                .build();

    }

}
