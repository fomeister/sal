/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcu.sal.plugins.sunspot.Spot.sensors;

import com.sun.spot.util.Utils;
import java.io.IOException;
import jcu.sal.plugins.sunspot.Spot.SALBSConnection;

/**
 *
 * @author gilles
 */
public class DataThread implements IDataThread {

    private Thread thread;
    private boolean stop, error;
    private int freq;
    private DataProducer prod;
    private final String command, sensor;
    private SALBSConnection conn;
    private final ISensorManager manager;

    /**
     * This method builds a new thread to stream sensor data.
     * @param m the sensor manager to notify when this thread exits because of
     * a connection error
     * @param t the sensor type for which we are streaming data
     * @param cmd the command for which we are streaming data
     * @param p the data producer which hands out the data for this command
     * @param f how often to send the command (in ms), if 0 data is sent only
     * when there is a change
     * @param c the base station connection where to send the data to
     */
    public DataThread(ISensorManager m, String t, String cmd, DataProducer p,
            int f, SALBSConnection c) {
        manager = m;
        command = cmd;
        sensor = t;
        prod = p;
        conn = c;
        thread = null;
        stop = false;
        error = false;
        freq = f;
    }

    public synchronized void start() {
        if (thread == null) {
            thread = new Thread(this, sensor + " "+command);
            thread.start();
        }
    }

    public synchronized void stop() {
        if (thread != null) {
            if (thread.isAlive()) {
                stop = true;
                thread.interrupt();
//                try {
//                    thread.join();
//                } catch (InterruptedException ex) {
//                    System.out.println("Interrupted while waiting for '" +
//                            sensor + " "+command + "' to finish");
//                }
            }
            thread = null;
        }
    }

    private void sendResponse(String r){
        //System.out.println("Sending "+r);
        if (!conn.sendSensorData(sensor, command, r)) {
            System.out.println("Error sending data from " +
                    "sensor -  stopping thread '" +
                    sensor + " "+command + "'");
            error = true;
        }
    }

    public void run() {
        String prev = "", curr;
        System.out.println("thread '" + sensor + " "+command + " " +
                freq+"' started");
        //check freq
        if(freq==0){
            //continuous
            //System.out.println("data thread continuous");
            while (!stop && !error) {
                try {
                    curr = prod.getDataCont();
                    //compare with previous
                    if (!curr.equals(prev)) {
                        //send if different
                        sendResponse(curr);
                        prev = curr;
                    }
                } catch (IOException ex) {
                    //error reading from sensor
                    System.out.println("Error reading from sensor - " +
                            "sending error & stopping thread '" +
                            sensor + " "+command + "'");
                    conn.sendGenericError(sensor, command,
                            "error reading from sensor");
                    error = true;
                }
            }
        } else if (freq==-1) {
            //single shot
            //System.out.println("single shot");
            try {
                sendResponse(prod.getData());
            } catch (Throwable t) {
                //error reading from sensor
                System.out.println("Error reading from sensor -" +
                        "sending error");
                conn.sendGenericError(sensor, command,
                        "error reading from sensor - " + t.getMessage());
                error = true;
            }

        } else {
            //interval
            //System.out.println("data thread interval");
            while (!stop && !error) {
                try {
                    sendResponse(prod.getData());
                    Utils.sleep(freq);
                } catch (Throwable t) {
                    //error reading from sensor
                    System.out.println("Error reading from sensor -" +
                            "sending error");
                    conn.sendGenericError(sensor, command,
                            "error reading from sensor - "+t.getMessage());
                    error = true;
                }
            }
        }

        System.out.println("thread '" + sensor + " "+command + " "+
                freq+"' exited");

        if (error || freq==-1) {
            //notify manager in separate thread to avoid join() deadlock
            new Thread(new Runnable() {

                public void run() {
                    manager.stopStream(command);
                }
            }).start();
        }
    }
}
