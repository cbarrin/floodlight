package net.floodlightcontroller.core.pipelines;

import net.floodlightcontroller.core.IOFPipeline;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchBackend;
import net.floodlightcontroller.core.SwitchDescription;
import net.floodlightcontroller.core.internal.TableFeatures;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.actionid.OFActionId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by geddingsbarrineau on 3/27/17.
 * <p>
 * OVSPipeline
 */
public class OVSPipeline implements IOFPipeline {

    protected static TableId FLOWMOD_DEFAULT_TABLE_ID = TableId.ZERO;
    
    @Override
    public boolean isValidPipeline(SwitchDescription description) {
        return description.getHardwareDescription().contains("Open vSwitch");
    }

    @Override
    public List<OFFlowMod> conformFlowsToPipeline(OFFlowMod.Builder fmb) {
        if (!fmb.getVersion().equals(OFVersion.OF_10)) {
            fmb.setTableId(FLOWMOD_DEFAULT_TABLE_ID);
        }
        return Stream.of(fmb.build()).collect(Collectors.toList());
    }

    @Override
    public OFFlowMod.Builder conformFlowToPipeline(OFFlowMod.Builder fmb) {
        if (!fmb.getVersion().equals(OFVersion.OF_10)) {
            fmb.setTableId(FLOWMOD_DEFAULT_TABLE_ID);
        }
        return fmb;
    }

    @Override
    public List<OFMessage> getTableMissRules(IOFSwitchBackend sw) {
        OFFactory factory = sw.getOFFactory();

        /* Default flow miss behavior is to send packet to controller */
        List<OFMessage> flows = new ArrayList<>();

        /* If we received a table features reply, iterate over the tables */
        if (!sw.getTables().isEmpty()) {
            short missCount = 0;
            for (TableId tid : sw.getTables()) {
					/* Only add the flow if the table exists and if it supports sending to the controller */
                TableFeatures tf = sw.getTableFeatures(tid);
                if (tf != null && (missCount < sw.getMaxTableForTableMissFlow().getValue())) {
                    if (tf.getPropApplyActionsMiss() != null) {
                        for (OFActionId aid : tf.getPropApplyActionsMiss().getActionIds()) {
                            if (aid.getType() == OFActionType.OUTPUT) { /* The assumption here is that OUTPUT includes the special port CONTROLLER... */
                                OFFlowMod defaultFlow = getDefaultTableMissFlow(tid, factory);
                                flows.add(defaultFlow);
                                break; /* Stop searching for actions and go to the next table in the list */
                            }
                        }
                    }
                }
                missCount++;
            }
        } else { /* Otherwise, use the number of tables starting at TableId=0 as indicated in the features reply */
            short missCount = 0;
            for (short tid = 0; tid < sw.getNumTables(); tid++, missCount++) {
                if (missCount < sw.getMaxTableForTableMissFlow().getValue()) { /* Only insert if we want it */
                    OFFlowMod defaultFlow = getDefaultTableMissFlow(TableId.of(tid), factory);
                    flows.add(defaultFlow);
                }
            }
        }

        return flows;
    }
    
    private OFFlowMod getDefaultTableMissFlow(TableId tid, OFFactory factory) {
        ArrayList<OFAction> actions = new ArrayList<OFAction>(1);
        actions.add(factory.actions().output(OFPort.CONTROLLER, 0xffFFffFF));

        return factory.buildFlowAdd()
                .setTableId(tid)
                .setPriority(0)
                .setActions(actions)
                .build();
    }

    @Override
    public void removeDefaultFlow(IOFSwitch sw) {
        /*
         * Remove the default flow if it's present.
	     */
        OFFlowDeleteStrict deleteFlow = sw.getOFFactory().buildFlowDeleteStrict()
                .setTableId(TableId.ALL)
                .setOutPort(OFPort.CONTROLLER)
                .build();
        
        sw.write(deleteFlow);
    }

    @Override
    public void addDefaultFlows(IOFSwitchBackend sw) {
        removeDefaultFlow(sw);
        sw.write(getTableMissRules(sw));
    }
}

