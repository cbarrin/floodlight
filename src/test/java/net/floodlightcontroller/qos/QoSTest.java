package net.floodlightcontroller.qos;

import net.floodlightcontroller.test.FloodlightTestCase;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by geddingsbarrineau on 12/12/16.
 */
public class QoSTest extends FloodlightTestCase {

    private IQoS qos;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        qos = new QoS();
    }

    @Test
    public void testEnablingAndDisablingQoS() {
        qos.enable();
        assertEquals(qos.isEnabled(), true);
        qos.disable();
        assertEquals(qos.isEnabled(), false);
    }

}
