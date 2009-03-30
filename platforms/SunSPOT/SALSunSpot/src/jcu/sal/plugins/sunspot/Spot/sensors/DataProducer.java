/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot.sensors;

import java.io.IOException;

/**
 * This interface is implemented by objects producing sensor
 * data
 * @author gilles
 */
public interface DataProducer {
    /**
     * This method is called to retrieve the latest sensor data.
     * It must not be called when streaming continuously.
     * Used {@link #getDataCont()} instead.
     * @return the latest data
     * @throws IOException if there is an error getting the data
     */
    public String getData() throws IOException;

    /**
     * This method is called to retrieve the sensor data for a continuous stream.
     * Because this method is called repeatedly, tt sleeps a reasonnable amount
     * of time (10-50ms) so as to yield the cpu.
     * @return the latest data
     * @throws IOException if there is an error getting the data
     */
    public String getDataCont() throws IOException;
}
