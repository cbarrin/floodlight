package net.floodlightcontroller.core.pipelines;

import net.floodlightcontroller.core.OFPipeline;
import net.floodlightcontroller.core.SwitchDescription;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.TableId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geddingsbarrineau on 4/17/17.
 */
public class HPPipeline extends OFPipeline {
    
    public static TableId DEFAULT_PIPELINE_START = TableId.of(0);
    public static TableId DEFAULT_HW_TABLE = TableId.of(100);
    public static TableId DEFAULT_SW_TABLE = TableId.of(200);
    
    @Override
    public boolean isValidPipeline(SwitchDescription switchDescription) {
        return false;
    }

    @Override
    public List<OFFlowMod> conformMessagesToPipeline(OFFlowMod.Builder fmb) {
        List<OFFlowMod> flowMods = new ArrayList<>();
        
        if (!fmb.getVersion().equals(OFVersion.OF_10)) {
            fmb.setTableId(FLOWMOD_DEFAULT_TABLE_ID);
        }
        return flowMods;
    }

    @Override
    public List<OFFlowMod> getTableMissRules(IOFSwitchBackend sw) {
        return null;
    }
}
