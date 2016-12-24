package net.floodlightcontroller.qos;

import com.google.common.util.concurrent.ListenableFuture;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
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
    static IOFSwitchService switchService;

    private boolean isQoSEnabled = false;
    private QueueContainer queueContainer;

    public void getQueues() {
        OFQueueGetConfigRequest request;
        OFQueueGetConfigReply reply;
        ListenableFuture<OFQueueGetConfigReply> future;
        for (DatapathId dpid : switchService.getAllSwitchDpids()) {
            IOFSwitch sw = switchService.getActiveSwitch(dpid);
            for (OFPortDesc p : sw.getPorts()) {
                request = sw.getOFFactory()
                        .buildQueueGetConfigRequest()
                        .setPort(p.getPortNo())
                        .build();
                future = sw.writeRequest(request);
                try {
                    reply = future.get(10, TimeUnit.SECONDS);
                    for (OFPacketQueue q : reply.getQueues()) {
                        log.info("Port: {}\tQueue: {}", p.getPortNo(), q.getProperties());
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
        switchService = context.getServiceImpl(IOFSwitchService.class);
        queueContainer = new QueueContainer(context.getConfigParams(this));
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
