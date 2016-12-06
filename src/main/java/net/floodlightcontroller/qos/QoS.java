package net.floodlightcontroller.qos;

import com.google.common.util.concurrent.ListenableFuture;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.protocol.OFQueueGetConfigReply;
import org.projectfloodlight.openflow.protocol.OFQueueGetConfigRequest;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueueProp;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueuePropMaxRate;
import org.projectfloodlight.openflow.protocol.queueprop.OFQueuePropMinRate;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFQueuePropertiesSerializerVer13;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by geddingsbarrineau on 12/5/16.
 * 
 */
public class QoS implements IQoS, IFloodlightModule {
    private static final Logger log = LoggerFactory.getLogger(QoS.class);
    
    private static IOFSwitchService switchService;

    public void getQueues() {
        OFFactoryVer13 factory = new OFFactoryVer13();
        OFQueueGetConfigRequest cr = factory.buildQueueGetConfigRequest().setPort(OFPort.of(1)).build(); /* Request queues on any port (i.e. don't care) */
        ListenableFuture<OFQueueGetConfigReply> future = switchService.getSwitch(DatapathId.of(1)).writeRequest(cr); /* Send request to switch 1 */
        try {
    /* Wait up to 10s for a reply; return when received; else exception thrown */
            OFQueueGetConfigReply reply = future.get(10, TimeUnit.SECONDS);
            log.info("Got queue config reply: {}", reply);
    /* Iterate over all queues */
            reply.getQueues().stream()
                    .flatMap(q -> q.getProperties().stream())
                    .map(OFQueueProp::getType)
                    .

            for (OFPacketQueue q : reply.getQueues()) {
                OFPort p = q.getPort(); /* The switch port the queue is on */
                long id = q.getQueueId(); /* The ID of the queue */
            /* Determine if the queue rates */
                for (OFQueueProp qp : q.getProperties()) {
                    int rate;
                /* This is a bit clunky now -- need to improve API in Loxi */
                    switch (qp.getType()) {
                        case OFQueuePropertiesSerializerVer13.MIN_RATE_VAL: /* min rate */
                            OFQueuePropMinRate min = (OFQueuePropMinRate) qp;
                            rate = min.getRate();
                            break;
                        case OFQueuePropertiesSerializerVer13.MAX_RATE_VAL: /* max rate */
                            OFQueuePropMaxRate max = (OFQueuePropMaxRate) qp;
                            rate = max.getRate();
                            break;
                    }
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) { /* catch e.g. timeout */
            e.printStackTrace();
        }
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
        return null;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        switchService = context.getServiceImpl(IOFSwitchService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getQueues();
    }
}
