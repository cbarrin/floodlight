package net.floodlightcontroller.qos;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchBackend;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.test.FloodlightTestCase;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.*;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

/**
 * Created by geddingsbarrineau on 12/12/16.
 */
public class QoSTest extends FloodlightTestCase {

    private FloodlightModuleContext fmc;
    private QoS qos;
    private IPacket testPacket;
    private OFPacketIn pi;
    // TODO: Test with more than just OpenFlow 1.3
    private final OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);


    /**
     * There are two switches in this test topology that have queues on them.
     * The first switch will be initialized as ACTIVE for test purposes.
     * Following are the initial switch queues:
     * DPID - 00:00:00:00:00:00:00:01
     *      QueueId - 1
     *      Port - 1
     *      -----------
     *      QueueId - 2
     *      Port - 2
     * DPID - 00:00:00:00:00:00:00:02
     *      QueueId - 1
     *      Port - 1
     *      -----------
     *      QueueId - 2
     *      Port - 2
     */
    //language=JSON
    private String json = "{\"00:00:00:00:00:00:00:01\":[{\"queueId\":1,\"port\":1," +
            "\"properties\":[{\"min-rate\":3000000},{\"max-rate\":5000000}]},{\"queueId\":2,\"port\":2," +
            "\"properties\":[{\"min-rate\":5000000},{\"max-rate\":7000000}]}]," +
            "\"00:00:00:00:00:00:00:02\":[{\"queueId\":1,\"port\":1,\"properties\":[{\"min-rate\":3000000}," +
            "{\"max-rate\":5000000}]},{\"queueId\":2,\"port\":2,\"properties\":[{\"min-rate\":5000000},{\"max-rate\":" +
            " 7000000}]}]}";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        fmc = new FloodlightModuleContext();
        qos = new QoS();

        Map<DatapathId, IOFSwitch> switches = new HashMap<>();
        switches.put(DatapathId.of(1), createMock(IOFSwitchBackend.class));
        mockSwitchManager.setSwitches(switches);
        fmc.addService(IOFSwitchService.class, mockSwitchManager);
        fmc.addConfigParam(qos, "switchesInitialQueues", json);

        qos.init(fmc);
        qos.startUp(fmc);

        testPacket = new Ethernet()
                .setSourceMACAddress("00:44:33:22:11:00")
                .setDestinationMACAddress("00:11:22:33:44:55")
                .setEtherType(EthType.ARP)
                .setPayload(
                        new ARP()
                                .setHardwareType(ARP.HW_TYPE_ETHERNET)
                                .setProtocolType(ARP.PROTO_TYPE_IP)
                                .setHardwareAddressLength((byte) 6)
                                .setProtocolAddressLength((byte) 4)
                                .setOpCode(ARP.OP_REPLY)
                                .setSenderHardwareAddress(MacAddress.of("00:44:33:22:11:00"))
                                .setSenderProtocolAddress(IPv4Address.of("192.168.1.1"))
                                .setTargetHardwareAddress(MacAddress.of("00:11:22:33:44:55"))
                                .setTargetProtocolAddress(IPv4Address.of("192.168.1.2")));
        byte[] testPacketSerialized = testPacket.serialize();

        // The specific factory can be obtained from the switch, but we don't have one
//        pi = (OFPacketIn) factory.buildPacketIn()
//                .setBufferId(OFBufferId.NO_BUFFER)
//                .setInPort(OFPort.of(1))
//                .setData(testPacketSerialized)
//                .setReason(OFPacketInReason.NO_MATCH)
//                .setTotalLen(testPacketSerialized.length).build();
        pi = factory.buildPacketIn()
                .setReason(OFPacketInReason.NO_MATCH)
                .build();
    }

    @Test
    public void testEnablingAndDisablingQoS() {
        qos.enable();
        assertEquals(qos.isEnabled(), true);
        qos.disable();
        assertEquals(qos.isEnabled(), false);
    }

    /**
     *
     */
    @Test
    public void testGetOutputQueue() {
        Long queueId = qos.getOutputQueue(DatapathId.of(1), OFPort.of(1), pi);
        assertEquals(queueId, Long.valueOf(1));
    }

}
