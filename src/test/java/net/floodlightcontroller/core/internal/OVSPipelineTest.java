package net.floodlightcontroller.core.internal;

import net.floodlightcontroller.core.OFPipeline;
import net.floodlightcontroller.core.SwitchDescription;
import net.floodlightcontroller.core.pipelines.OVSPipeline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by geddingsbarrineau on 4/17/17.
 */
public class OVSPipelineTest {

    OFPipeline pipeline;

    @Before
    public void setUp() throws Exception {
        pipeline = new OVSPipeline();
    }

    @Test
    public void testIsValidPipeline() {
        SwitchDescription description = new SwitchDescription(
                "Nicira, Inc.",
                "Open vSwitch",
                "2.6.0",
                "None",
                "None");
        Assert.assertTrue(pipeline.isValidPipeline(description));
    }

    @Test
    public void testIsInvalidPipeline() {
        SwitchDescription description = new SwitchDescription(
                "HP",
                "Aruba",
                "1.0.0",
                "None",
                "None");
        Assert.assertFalse(pipeline.isValidPipeline(description));
    }
}
