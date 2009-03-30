/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot.sensors.temperature;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITemperatureInput;
import java.io.IOException;

/**
 *
 * @author gilles
 */
public class TemperatureSensor {
    private static ITemperatureInput sensor = EDemoBoard.getInstance().getADCTemperature();

    public static String getTempC() throws IOException {
        return String.valueOf(sensor.getCelsius());
    }
}
