/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcu.sal.plugins.sunspot.Spot.sensors;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Stack;
import jcu.sal.plugins.sunspot.Spot.SALBSConnection;

/**
 * This class manages streams from a given type of sensor
 * @author gilles
 */
public class SensorManager implements ISensorManager {

    /**
     * the pool of streaming threads
     */
    private ThreadPool threads;
    /**
     * A map of command names (SensorConstants.LIGHT_*) and
     * associated data producers
     */
    private Hashtable producers;
    private String type;
    private boolean started;
    private SALBSConnection conn;

    /**
     * This method builds a new sensor manager for a sensor of the given
     * type t.
     * @param t the type of the sensor
     * @throws java.lang.RuntimeException if the given type does not exist
     */
    public SensorManager(String t) {
        if (!SensorConstants.hasType(t)) {
            throw new RuntimeException("unknown sensor type " + t);
        }

        threads = new ThreadPool();
        type = t;
        producers = SensorConstants.getSensorCommands(t);
        started = false;
        conn = null;
    }

    public synchronized void start(SALBSConnection c) {
        if (!started) {
            conn = c;
            started = true;
        } else {
            System.out.println("Starting an already-started manager");
            //print a stack trace
            //I know this is lame...
            try {
                throw new IOException("lame");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void startStream(String cmd, int freq)
            throws AlreadyStreamingException, IOException {
        if (started) {
            if (!producers.containsKey(cmd)) {
                throw new IOException("unknown command " + cmd);
            }
            System.out.println("Starting stream: cmd: "+cmd+" - freq: "+freq);
            if (!threads.addThread(cmd,
                    new DataThread(this, type, cmd, (DataProducer) producers.get(cmd),
                    freq, conn))) {
                throw new AlreadyStreamingException("Already streaming");
            }
        }

    }

    public synchronized void stopStream(String cmd) {
        if (started) {
            threads.removeThread(cmd);
        }
    }

    public synchronized void stop() {
        if (started) {
            started = false;
            threads.removeAll();
        }
    }
}
