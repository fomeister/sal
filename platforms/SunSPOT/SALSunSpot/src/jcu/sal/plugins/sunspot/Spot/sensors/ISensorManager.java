/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot.sensors;

import java.io.IOException;
import jcu.sal.plugins.sunspot.Spot.SALBSConnection;

/**
 * Objects implementing this interface manage streams from
 * sensors. they are receive commands, create threads if appropriate
 * and stop them when required.
 * @author gilles
 */
public interface ISensorManager {

    /**
     * This method resets the manager and starts it.
     * @param c
     */
    public void start(SALBSConnection c);
    /**
     * This method is used to start a stream
     * @param what a sensor-specific command
     * @param freq how often to run the command (in ms)
     * @throws AlreadyStreamingException if there is already a stream for
     * this command
     * @throws java.io.IOException if the given
     * command is not recognised
     */
    public void startStream(String what, int freq) throws AlreadyStreamingException, IOException;

    /**
     * This method stops a stream
     * @param what the sensor specific command for which the stream
     * must be stopped
     */
    public void stopStream(String what);

    /**
     * This method is invoked when the manager must stop and terminate
     * all stremaing threads
     */
    public void stop();
}
