package net.floodlightcontroller.core.internal;

import net.floodlightcontroller.core.IOFPipeline;
import net.floodlightcontroller.core.SwitchDescription;
import net.floodlightcontroller.core.pipelines.HPPipeline;
import net.floodlightcontroller.core.pipelines.OVSPipeline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.TableId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geddingsbarrineau on 4/17/17.
 */
public class OFPipelinesTest {

    private IOFPipeline ovspipeline;
    private IOFPipeline hppipeline;
    
    private SwitchDescription ovsDescription, hpDescription;
    
    private OFFlowMod.Builder flowAddBuilder;

    @Before
    public void setUp() throws Exception {
        ovspipeline = new OVSPipeline();
        hppipeline = new HPPipeline();
        
        ovsDescription = new SwitchDescription(
                "Nicira, Inc.",
                "Open vSwitch",
                "2.6.0",
                "None",
                "None");
        
        hpDescription = new SwitchDescription(
                "HP",
                "Aruba",
                "1.0.0",
                "None",
                "None");
    }

    /* The following tests are for the OVS OpenFlow pipeline */
    @Test
    public void testIsValidOVSPipeline() {
        Assert.assertTrue(ovspipeline.isValidPipeline(ovsDescription));
    }

    @Test
    public void testIsInvalidOVSPipeline() {
        Assert.assertFalse(ovspipeline.isValidPipeline(hpDescription));
    }

    /**
     * OVS – being a software switch – supports almost all matches and actions and has a simple OpenFlow pipeline.
     * All we really need to test is that, given a flowmod, the table in which it is inserted will be table 0.
     */
    @Test
    public void testIsFlowConformedToOVSPipeline() {
        OFFlowMod.Builder flowAddBuilder = OFFactories.getFactory(OFVersion.OF_13).buildFlowAdd();
        
        List<OFFlowMod> flowAddExpected = new ArrayList<>();
        flowAddExpected.add(flowAddBuilder.setTableId(TableId.ZERO).build());
        List<OFFlowMod> flowAddActual = ovspipeline.conformFlowsToPipeline(flowAddBuilder);
        
        Assert.assertEquals(flowAddExpected, flowAddActual);

        flowAddBuilder = OFFactories.getFactory(OFVersion.OF_10).buildFlowAdd();
        
        flowAddExpected.clear();
        flowAddExpected.add(flowAddBuilder.build());
        flowAddActual = ovspipeline.conformFlowsToPipeline(flowAddBuilder);
        
        Assert.assertEquals(flowAddExpected, flowAddActual);
    }

    /* The following tests are for the HP OpenFlow pipeline */
    @Test
    public void testIsValidHPPipeline() {
        Assert.assertTrue(hppipeline.isValidPipeline(hpDescription));
    }

    @Test
    public void testIsInvalidHPPipeline() {
        Assert.assertFalse(hppipeline.isValidPipeline(ovsDescription));
    }

    @Test
    public void testIsFlowConformedToHPPipeline() {
        OFFlowMod.Builder flowAddBuilder = OFFactories.getFactory(OFVersion.OF_13).buildFlowAdd();

        List<OFFlowMod> flowAddExpected = new ArrayList<>();
        flowAddExpected.add(flowAddBuilder.setTableId(TableId.of(200)).build());
        List<OFFlowMod> flowAddActual = hppipeline.conformFlowsToPipeline(flowAddBuilder);

        Assert.assertEquals(flowAddExpected, flowAddActual);

        flowAddBuilder = OFFactories.getFactory(OFVersion.OF_10).buildFlowAdd();

        flowAddExpected.clear();
        flowAddExpected.add(flowAddBuilder.build());
        flowAddActual = hppipeline.conformFlowsToPipeline(flowAddBuilder);

        Assert.assertEquals(flowAddExpected, flowAddActual);
    }
}
