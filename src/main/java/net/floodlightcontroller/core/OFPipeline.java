package net.floodlightcontroller.core;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.types.TableId;

import java.util.List;

/**
 * Created by geddingsbarrineau on 4/3/17.
 * <p>
 * OFPipeline
 */
public abstract class OFPipeline {
    protected static TableId FLOWMOD_DEFAULT_TABLE_ID = TableId.ZERO;

    public abstract boolean isValidPipeline(SwitchDescription switchDescription);

    public abstract List<OFFlowMod> conformMessagesToPipeline(OFFlowMod.Builder fmb);
    
    public abstract List<OFFlowMod> getTableMissRules(IOFSwitchBackend sw);
}
