package net.floodlightcontroller.qos;

import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

/**
 * Created by geddingsbarrineau on 12/5/16.
 * This is the interface for the Quality of Service service.
 */
public interface IQoS extends IFloodlightService {

    /**
     * Enable the QoS service.
     */
    public void enable();

    /**
     * Disable the QoS service.
     */
    public void disable();

    /**
     * Checks if the QoS service is enabled.
     */
    public boolean isEnabled();

    /**
     * Get the queue ID of the queue that traffic should be assigned to on a given switch and port,
     * depending on the current QoS rules.
     * @param dpid The datapathId of the switch to return a queue for.
     * @param outport The output port to return a queue for.
     * @param pi The packet in message used to determine an output queue.
     * @return A long value that contains the queueId of the chosen queue on the switch and port.
     * If no queue is found then the function will return null.
     */
    public Long getOutputQueue(DatapathId dpid, OFPort outport, OFPacketIn pi);
}
