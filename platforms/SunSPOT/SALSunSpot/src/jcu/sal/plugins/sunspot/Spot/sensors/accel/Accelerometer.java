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

    private static String trim(String t){
        if(t.length()>=7)
                return t.substring(0, 6);

        return t;
    }

    public static String getX() throws IOException {
        return trim(String.valueOf(sensor.getAccelX()));
    }
    
    public static String getY() throws IOException {
        return trim(String.valueOf(sensor.getAccelY()));
    }
    
    public static String getZ() throws IOException {
        return trim(String.valueOf(sensor.getAccelZ()));
    }

    public static String getTotal() throws IOException {
        return trim(String.valueOf(sensor.getAccel()));
    }
}
