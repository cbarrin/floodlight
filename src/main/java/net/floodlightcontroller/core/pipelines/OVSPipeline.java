package net.floodlightcontroller.core.pipelines;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchBackend;
import net.floodlightcontroller.core.OFPipeline;
import net.floodlightcontroller.core.SwitchDescription;
import net.floodlightcontroller.core.internal.TableFeatures;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.actionid.OFActionId;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by geddingsbarrineau on 3/27/17.
 * <p>
 * OVSPipeline
 */
public class OVSPipeline extends OFPipeline {

    final String hardwareDescription = "Open vSwitch";

    @Override
    public boolean isValidPipeline(SwitchDescription description) {
        return description.getHardwareDescription().contains("Open vSwitch");
    }

    @Override
    public List<OFFlowMod> conformMessagesToPipeline(OFFlowMod.Builder fmb) {
        if (!fmb.getVersion().equals(OFVersion.OF_10)) {
            fmb.setTableId(FLOWMOD_DEFAULT_TABLE_ID);
        }
        return Stream.of(fmb.build()).collect(Collectors.toList());
    }

    @Override
    public List<OFFlowMod> getTableMissRules(IOFSwitchBackend sw) {
        OFFactory factory = sw.getOFFactory();
        
        /* Default flow miss behavior is to send packet to controller */
        ArrayList<OFAction> actions = new ArrayList<OFAction>(1);
        actions.add(sw.getOFFactory().actions().output(OFPort.CONTROLLER, 0xffFFffFF));
        List<OFFlowMod> flowMods = new ArrayList<>();

        short missCount = 0;
        for (TableId tableId : sw.getTables()) {
            /* Only add the flow if the table exists and if it supports sending to the controller */
            TableFeatures tf = sw.getTableFeatures(tableId);
            if (tf != null && (missCount < sw.getMaxTableForTableMissFlow().getValue())) {
                if (tf.getPropApplyActionsMiss() != null) {
                    for (OFActionId aid : tf.getPropApplyActionsMiss().getActionIds()) {
                        if (aid.getType() == OFActionType.OUTPUT) { /* The assumption here is that OUTPUT includes the special port CONTROLLER... */
                            OFFlowAdd defaultFlow = factory.buildFlowAdd()
                                    .setTableId(tid)
                                    .setPriority(0)
                                    .setInstructions(Collections.singletonList((OFInstruction) factory.instructions().buildApplyActions().setActions(actions).build()))
                                    .build();
                            flows.add(defaultFlow);
                            break; /* Stop searching for actions and go to the next table in the list */
                        }
                    }
                }
            }
            missCount++;
        }

        return flowMods;
    }

    private OFFlowDeleteStrict getDefaultRuleDeleteFlow(IOFSwitch sw) {
        /*
         * Remove the default flow if it's present.
	     */
        return sw.getOFFactory().buildFlowDeleteStrict()
                .setTableId(TableId.ALL)
                .setOutPort(OFPort.CONTROLLER)
                .build();
    }
}

