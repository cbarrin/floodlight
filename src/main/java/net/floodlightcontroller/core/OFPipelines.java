package net.floodlightcontroller.core;

import com.google.common.collect.ImmutableList;
import net.floodlightcontroller.core.pipelines.HPPipeline;
import net.floodlightcontroller.core.pipelines.OVSPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by geddingsbarrineau on 4/3/17.
 * <p>
 * OFPipelines
 */
public class OFPipelines {
    private static final Logger log = LoggerFactory.getLogger(net.floodlightcontroller.core.OFPipelines.class);
    
    public enum OFPipelineTypes {
        OVS, HP
    }
    
    private OFPipelines() {}
    
    public static IOFPipeline getPipelineFromDescription(SwitchDescription description) {
        return PrecachedPipeline.pipelines.stream()
                .filter(p -> p.isValidPipeline(description))
                .findFirst()
                .orElse(PrecachedPipeline.ovspipeline);
    }
    
    public static IOFPipeline of(OFPipelineTypes p) {
        switch(p) {
            case OVS:
                return PrecachedPipeline.ovspipeline;
            case HP:
                return PrecachedPipeline.hppipeline;
            default:
                throw new IllegalArgumentException("Unknown OpenFlow pipeline: " + p);
        }
    }

    private static class PrecachedPipeline {
        
        private static final IOFPipeline ovspipeline = new OVSPipeline();
        private static final IOFPipeline hppipeline = new HPPipeline();

        private static final List<IOFPipeline> pipelines = ImmutableList.of(ovspipeline, hppipeline);
        
        private PrecachedPipeline() {}
    }
}
