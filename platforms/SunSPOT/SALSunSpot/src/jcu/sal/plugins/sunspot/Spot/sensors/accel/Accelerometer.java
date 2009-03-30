/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot.sensors.accel;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import java.io.IOException;

/**
 *
 * @author gilles
 */
public class Accelerometer{
    private static IAccelerometer3D sensor =
        EDemoBoard.getInstance().getAccelerometer();

    public static String getX() throws IOException {
        return String.valueOf(sensor.getAccelX());
    }
    
    public static String getY() throws IOException {
        return String.valueOf(sensor.getAccelY());
    }
    
    public static String getZ() throws IOException {
        return String.valueOf(sensor.getAccelZ());
    }

    public static String getTotal() throws IOException {
        return String.valueOf(sensor.getAccel());
    }
}
