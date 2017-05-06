package net.floodlightcontroller.core.pipelines;

import net.floodlightcontroller.core.IOFPipeline;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchBackend;
import net.floodlightcontroller.core.SwitchDescription;
import org.projectfloodlight.openflow.protocol.OFFlowDeleteStrict;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by geddingsbarrineau on 4/17/17.
 */
public class HPPipeline implements IOFPipeline {

    public static TableId DEFAULT_PIPELINE_START = TableId.of(0);
    public static TableId DEFAULT_HW_TABLE = TableId.of(100);
    public static TableId DEFAULT_SW_TABLE = TableId.of(200);

    @Override
    public boolean isValidPipeline(SwitchDescription description) {
        return description.getHardwareDescription().contains("HP") || description.getManufacturerDescription().contains("HP");
    }

    @Override
    public List<OFFlowMod> conformFlowsToPipeline(OFFlowMod.Builder fmb) {
        if (!fmb.getVersion().equals(OFVersion.OF_10)) {
            fmb.setTableId(DEFAULT_SW_TABLE);
        }
        return Stream.of(fmb.build()).collect(Collectors.toList());
    }

    @Override
    public OFFlowMod.Builder conformFlowToPipeline(OFFlowMod.Builder fmb) {
        if (!fmb.getVersion().equals(OFVersion.OF_10)) {
            fmb.setTableId(DEFAULT_SW_TABLE);
        }
        return fmb;
    }

    /**
     * In certain HP switches, the OpenFlow pipelines is as follows:
     * [Table 0 (start)] --> [Table 100 (TCAM)] --> [Table 200 (SW)]
     * 
     * Because there are so many restrictions on which matches and actions are supported
     * it is difficult to conform different flow mod messages to fit the OpenFlow pipeline.
     * This becomes more difficult when trying to determine what flows can be supported in 
     * hardware and what may only be supported in software. Fortunately the switch (according
     * to observations) can handle a lot of this work for us. Flows inserted in the software
     * table that can be performed in hardware are automatically identified and run in hardware.
     * 
     * This makes it easy for us, then, as we can simply insert everything into Table 200. However,
     * because there won't be any flows in Table 100, and the default table miss flow in Table 100 
     * specifies that the packet be dropped, we must override the default table miss rule to 
     * send all packets from Table 100 to Table 200 where all of our flows will be inserted.
     *
     * @param sw IOFSwitchBackend
     * @return The list of default table miss OFMessages that should be inserted on the switch
     */
    @Override
    public List<OFMessage> getTableMissRules(IOFSwitchBackend sw) {
        OFFlowMod tableMiss = sw.getOFFactory().buildFlowAdd()
                .setTableId(DEFAULT_HW_TABLE)
                .setPriority(0)
                .setInstructions(Collections.singletonList(sw.getOFFactory()
                        .instructions().gotoTable(DEFAULT_SW_TABLE))).build();
        return Collections.singletonList(tableMiss);
    }

    @Override
    public void removeDefaultFlow(IOFSwitch sw) {
        /* Remove the default flow if it's present. */
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
