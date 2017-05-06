package net.floodlightcontroller.core;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;

import java.util.List;

/**
 * Created by geddingsbarrineau on 4/25/17.
 */
public interface IOFPipeline {
    
    public boolean isValidPipeline(SwitchDescription switchDescription);
    
    public List<OFFlowMod> conformFlowsToPipeline(OFFlowMod.Builder fmb);
    
    public OFFlowMod.Builder conformFlowToPipeline(OFFlowMod.Builder fmb);
    
    public List<OFMessage> getTableMissRules(IOFSwitchBackend sw);

    public void removeDefaultFlow(IOFSwitch sw);
    
    public void addDefaultFlows(IOFSwitchBackend sw);
    
}
