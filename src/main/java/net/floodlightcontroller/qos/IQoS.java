package net.floodlightcontroller.qos;

import net.floodlightcontroller.core.module.IFloodlightService;

/**
 * Created by geddingsbarrineau on 12/5/16.
 * This is the interface for the Quality of Service service.
 */
public interface IQoS extends IFloodlightService {

    /**
     * Enable the QoS service.
     */
    public void enable();

    /**
     * Disable the QoS service.
     */
    public void disable();

    /**
     * Checks if the QoS service is enabled.
     */
    public boolean isEnabled();
}
