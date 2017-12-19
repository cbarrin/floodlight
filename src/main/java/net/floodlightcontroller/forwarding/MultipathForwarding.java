package net.floodlightcontroller.forwarding;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.routing.ForwardingBase;
import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.util.FlowModUtils;
import net.floodlightcontroller.util.OFMessageUtils;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.*;

/**
 * Created by @geddings on 12/19/17.
 */
public class MultipathForwarding extends ForwardingBase implements IFloodlightModule {

    @Override
    public Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, IRoutingDecision decision, FloodlightContext cntx) {
        OFPort srcPort = OFMessageUtils.getInPort(pi);
        IDevice dstDevice = IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_DST_DEVICE);
        IDevice srcDevice = IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_SRC_DEVICE);

        SwitchPort dstAp = null;
        for (SwitchPort ap : dstDevice.getAttachmentPoints()) {
            if (topologyService.isEdge(ap.getNodeId(), ap.getPortId())) {
                dstAp = ap;
                break;
            }
        }

        if (dstAp == null) {
            log.debug("Could not locate edge attachment point for destination device {}. Flooding packet");
            return Command.CONTINUE;
        }

        /* Validate that the source and destination are not on the same switch port */
        if (sw.getId().equals(dstAp.getNodeId()) && srcPort.equals(dstAp.getPortId())) {
            log.info("Both source and destination are on the same switch/port {}/{}. Dropping packet", sw.toString(), srcPort);
            return Command.CONTINUE;
        }

        /* Get all the paths between the source and destination */
        List<Path> paths = routingEngineService.getPathsSlow(sw.getId(), dstAp.getNodeId(), 4);
        
        /* Get all the decision points between the paths */
        
        /* Insert a flow and a select group at all of the decision points */
        
        /* Split the paths at each decision point */
        
        /* Push a route on every split path */

        return Command.CONTINUE;
    }

    public static Set<DatapathId> getDecisionPoints(List<Path> paths) {
        Set<DatapathId> decisionPoints = new HashSet<>();

        for (Path path1 : paths) {
            for (Path path2 : paths) {
                if (path1 != path2 && path1.getPath().get(0).getNodeId().equals(path2.getPath().get(0).getNodeId())) {
                    decisionPoints.add(path1.getPath().get(0).getNodeId());
                }
            }
        }

        for (Path path : paths) {
            List<NodePortTuple> nodePortList = path.getPath();
            for (int i = 1; i < nodePortList.size() - 2; i += 2) {
                for (Path path1 : paths) {
                    if (path != path1 &&
                            Collections.indexOfSubList(path1.getPath(), ImmutableList.of(nodePortList.get(i), nodePortList.get(i + 1))) != -1) {
                        decisionPoints.add(nodePortList.get(i + 1).getNodeId());
                    }
                }
            }
        }
        return decisionPoints;
    }

    private static OFGroupMod buildSelectGroup(IOFSwitch sw, Set<OFPort> outports) {
        OFFactory factory = sw.getOFFactory();
        List<OFBucket> buckets = new ArrayList<>();

        for (OFPort outport : outports) {
            buckets.add(factory.buildBucket()
                    .setActions(Collections.singletonList((OFAction) factory.actions().buildOutput()
                            .setMaxLen(0xffFFffFF)
                            .setPort(outport)
                            .build()))
                    .build());
        }

        return factory.buildGroupAdd()
                .setGroup(OFGroup.of(1))
                .setGroupType(OFGroupType.SELECT)
                .setBuckets(buckets)
                .build();
    }

    private static OFFlowMod buildFlow(IOFSwitch sw, Match match, OFGroup group) {
        return sw.getOFFactory().buildFlowAdd()
                .setHardTimeout(0)
                .setIdleTimeout(60)
                .setPriority(FlowModUtils.PRIORITY_MAX)
                .setMatch(match)
                .setActions(Collections.singletonList(sw.getOFFactory().actions().buildGroup()
                        .setGroup(group)
                        .build()))
                .build();
    }


    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        return ImmutableSet.of(ITopologyService.class, IRoutingService.class);
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        super.init();
        this.topologyService = context.getServiceImpl(ITopologyService.class);
        this.routingEngineService = context.getServiceImpl(IRoutingService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        super.startUp();
    }
}
