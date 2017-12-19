package net.floodlightcontroller.forwarding;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.PathId;
import org.junit.Test;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by @geddings on 12/19/17.
 */
public class MultipathForwardingTest {
    
    @Test
    public void testGetDecisionPointsEmptyWhenNoneExist() throws Exception {
        Path path1 = path(1, 4,
                sp(1, 1),
                sp(1, 2),
                sp(2, 1),
                sp(2, 2),
                sp(4, 1),
                sp(4, 2));

        Path path2 = path(1, 4,
                sp(1, 1),
                sp(1, 4),
                sp(3, 1),
                sp(3, 2),
                sp(4, 3),
                sp(4, 2));

        Set<DatapathId> decisionPoints = MultipathForwarding.getDecisionPoints(ImmutableList.of(path1, path2));
        assertTrue(decisionPoints.isEmpty());
    }

    @Test
    public void testGetDecisionPoints() throws Exception {
        Path path1 = path(1, 4,
                sp(1, 1),
                sp(1, 2),
                sp(2, 1),
                sp(2, 2),
                sp(4, 1),
                sp(4, 2),
                sp(5, 1),
                sp(5, 2),
                sp(6, 1),
                sp(6, 2),
                sp(8, 1),
                sp(8, 2));

        Path path2 = path(1, 4,
                sp(1, 1),
                sp(1, 4),
                sp(3, 1),
                sp(3, 2),
                sp(4, 3),
                sp(4, 2),
                sp(5, 1),
                sp(5, 4),
                sp(7, 1),
                sp(7, 2),
                sp(8, 3),
                sp(8, 2));

        Set<DatapathId> decisionPointsActual = MultipathForwarding.getDecisionPoints(ImmutableList.of(path1, path2));
        Set<DatapathId> decisionPointsExpected = ImmutableSet.of(DatapathId.of(1), DatapathId.of(5));
        
        assertEquals(decisionPointsExpected, decisionPointsActual);
    }
    
    private static SwitchPort sp(int dpid, int port) {
        return new SwitchPort(DatapathId.of(dpid), OFPort.of(port));
    }

    private static Path path(int src, int dst, SwitchPort ... switchPorts) {
        return new Path(new PathId(DatapathId.of(src), DatapathId.of(dst)), Arrays.asList(switchPorts));
    }
}