/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot.sensors.light;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ILightSensor;
import java.io.IOException;

/**
 *
 * @author gilles
 */
public class LightSensor{
    private static ILightSensor sensor = EDemoBoard.getInstance().getLightSensor();

    public static String getAverageLux() throws IOException {
        return String.valueOf(sensor.getAverageValue());
    }
}
