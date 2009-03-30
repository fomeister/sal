/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcu.sal.plugins.sunspot.Spot.sensors;

import com.sun.spot.util.Utils;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import jcu.sal.plugins.sunspot.Spot.sensors.accel.Accelerometer;
import jcu.sal.plugins.sunspot.Spot.sensors.light.LightSensor;
import jcu.sal.plugins.sunspot.Spot.sensors.temperature.TemperatureSensor;

/**
 *
 * @author gilles
 */
public class SensorConstants {
    /*
     * TO ADD A NEW SENSOR:
     * - create a new sensor type (TYPE_blabla)
     *
     * - add a new entry in the "managers" tables using the sensor type
     * as key and the constructor "new SensorManager(sensor_type)" as element
     *
     * - create a new Hashtable "sensorCommands"
     *
     * - add the new type (as key) and "sensorCommands" hastable (as element)
     * to the "knownSensors" table.
     *
     * - create static strings for each command (TYPE_DO_this or TYPE_GET_that)
     *
     * - populate "sensorCommands" with each command as the key and a data
     * producer as element
     *
     */


    /**
     * Hashtable<String, Hashtable<String, DataProducer>>
     * hashtable<SensorType, Hashtable<CMD, DataProducer>>
     * The following map associate sensor types (TYPE_*),
     * with a map of supported commands and their data producers
     */
    private final static Hashtable knownSensors = new Hashtable();
    /**
     * a map of commands for the light sensor and associated data producer
     */
    private final static Hashtable lightCommands = new Hashtable();
    /**
     * a map of commands for the temperature sensor and associated data producer
     */
    private final static Hashtable tempCommands = new Hashtable();
    /**
     * a map of commands for the accelerometer and associated data producer
     */
    private final static Hashtable accelCommands = new Hashtable();
    /*
     * Known sensor types
     */
    public final static String TYPE_ACCEL = "1";
    public final static String TYPE_TEMP = "2";
    public final static String TYPE_LIGHT = "3";
    /*
     * NEW SENSORS MUST ALSO BE ADDED TO
     * THE knownSensors HASHTABLE BELOW
     */
    static {
        knownSensors.put(TYPE_ACCEL, accelCommands);
        knownSensors.put(TYPE_TEMP, tempCommands);
        knownSensors.put(TYPE_LIGHT, lightCommands);
    }





    /*
     * Light sensor commands
     */
    public final static String LIGHT_GET_LUX = "1";
    /*
     * NEW COMMANDS MUST BE ADDED TO THE
     * lightCommands HASHTABLE BELOW
     */
    static {
        lightCommands.put(LIGHT_GET_LUX, new DataProducer() {

            public String getData() throws IOException {
                return LightSensor.getAverageLux();
            }
            public String getDataCont() throws IOException {
                Utils.sleep(100);
                return getData();
            }
        });
    }




    /*
     * Temp sensor commands
     */
    public final static String TEMP_GET_C = "1";
    /*
     * NEW COMMANDS MUST BE ADDED TO THE
     * tempCommands HASHTABLE BELOW
     */
    static {
        tempCommands.put(TEMP_GET_C, new DataProducer() {

            public String getData() throws IOException {
                return TemperatureSensor.getTempC();
            }
            public String getDataCont() throws IOException {
                Utils.sleep(100);
                return getData();
            }
        });
    }




    /*
     * Accelerometersensor commands
     */
    public final static String ACCEL_GET_X = "1";
    public final static String ACCEL_GET_Y = "2";
    public final static String ACCEL_GET_Z = "3";
    public final static String ACCEL_GET_TOTAL = "4";
    /*
     * NEW COMMANDS MUST BE ADDED TO THE
     * accelCommands VECTOR BELOW
     */
    static {
        accelCommands.put(ACCEL_GET_X, new DataProducer() {

            public String getData() throws IOException {
                return Accelerometer.getX();
            }
            public String getDataCont() throws IOException {
                Utils.sleep(100);
                return getData();
            }
        });
        accelCommands.put(ACCEL_GET_Y, new DataProducer() {

            public String getData() throws IOException {
                return Accelerometer.getY();
            }
            public String getDataCont() throws IOException {
                Utils.sleep(100);
                return getData();
            }
        });
        accelCommands.put(ACCEL_GET_Z, new DataProducer() {

            public String getData() throws IOException {
                return Accelerometer.getZ();
            }
            public String getDataCont() throws IOException {
                Utils.sleep(100);
                return getData();
            }
        });
        accelCommands.put(ACCEL_GET_TOTAL, new DataProducer() {

            public String getData() throws IOException {
                return Accelerometer.getTotal();
            }
            public String getDataCont() throws IOException {
                Utils.sleep(100);
                return getData();
            }
        });
    }


    /*
     *
     * MANAGERS
     *
     */

    /**
     * Hashtable<String, SensorManager>
     * the following map associated a sensor type
     * with a sensor manager which can start and stop streams
     */
    private static Hashtable managers = new Hashtable();
    static{
        managers.put(SensorConstants.TYPE_LIGHT,
                new SensorManager(SensorConstants.TYPE_LIGHT));
        managers.put(SensorConstants.TYPE_TEMP,
                new SensorManager(SensorConstants.TYPE_TEMP));
        managers.put(SensorConstants.TYPE_ACCEL,
                new SensorManager(SensorConstants.TYPE_ACCEL));
    }
    
    
    
    
    
    
    
    /*
     *
     *  U T I L I T Y   M E T H O D S
     *
     * 
     */

    /**
     * This method returns a hashtable of all managers.
     * The key in the hashtable is a sensor type.
     * The element is a {@link SensorManager}.
     * @return a hashtable of all known managers
     */
    public static Hashtable getManagers() {
        Hashtable r = new Hashtable();
        Enumeration e = managers.keys();
        Object o;
        while (e.hasMoreElements()) {
            o = e.nextElement();
            r.put(o, managers.get(o));
        }
        return r;
    }

    /**
     * This method returns a vector of all known sensor types
     * @return a vector of all known sensor types
     */
    public static Vector getSensorTypeList() {
        Vector v = new Vector();
        Enumeration e = knownSensors.keys();
        while (e.hasMoreElements()) {
            v.addElement(e.nextElement());
        }
        return v;
    }

    /**
     * This method checks if the given sensor type <code>t</code> exists.
     * @param t the sensor type to be tested
     * @return <code>true</code> if the type exisits. <code>false</code>
     * otherwise
     */
    public static boolean hasType(String t){
        return knownSensors.containsKey(t);
    }

    /**
     * This method returns (a copy of) the map
     * containing the commands and their data producer for a given sensor type.
     * @param t the sensor type
     * @return (a copy of) the map
     * containing the known command and their data producer, or null if the
     * given sensor type does not exits.
     */
    public static Hashtable getSensorCommands(String t) {
        if(!hasType(t))
            return null;

        Hashtable r = new Hashtable();
        Hashtable cmds = (Hashtable) knownSensors.get(t);
        Enumeration e = cmds.keys();
        Object o;
        while (e.hasMoreElements()) {
            o = e.nextElement();
            r.put(o, cmds.get(o));
        }
        return r;
    }
}
