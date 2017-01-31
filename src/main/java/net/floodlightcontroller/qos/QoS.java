package net.floodlightcontroller.qos;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by geddingsbarrineau on 12/5/16.
 */
public class QoS implements IFloodlightModule, IQoS {
    private static final Logger log = LoggerFactory.getLogger(QoS.class);
    private boolean isQoSEnabled = false;
    private QueueContainer queueContainer;

    /*
     * IQoS implementation
	 */

    @Override
    public void enable() {
        isQoSEnabled = true;
    }

    @Override
    public void disable() {
        isQoSEnabled = false;
    }

    @Override
    public boolean isEnabled() {
        return isQoSEnabled;
    }

    /*
     * Currently only returns the first queueId found, given a switch and port.
     *
     * 1. Are there any queues on this switch and port?
     * 2. If so, which queue is the best to put it on?
     */
    @Override
    public Long getOutputQueue(DatapathId dpid, OFPort outport, OFPacketIn pi) {
        Long queueId = null;
        List<OFPacketQueue> queues = queueContainer.getActiveQueuesOnSwitch(dpid);
        if (queues != null) {
            queueId = queues.stream()
                    .filter(queue -> outport.equals(queue.getPort()))
                    .map(OFPacketQueue::getQueueId)
                    .findFirst()
                    .orElse(null);
        }
        return queueId;
    }

    /*
     * IFloodlightModule implementation
	 */

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IQoS.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IQoS.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IOFSwitchService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        IOFSwitchService switchService = context.getServiceImpl(IOFSwitchService.class);
        queueContainer = new QueueContainer(context.getConfigParams(this), switchService);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
