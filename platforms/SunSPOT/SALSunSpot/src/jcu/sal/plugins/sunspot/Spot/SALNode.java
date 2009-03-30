/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import jcu.sal.plugins.sunspot.Spot.sensors.AlreadyStreamingException;
import jcu.sal.plugins.sunspot.Spot.sensors.SensorConstants;
import jcu.sal.plugins.sunspot.Spot.sensors.SensorManager;

/**
 * This class represent a SAL Sunspot node. It uses a {@link SALBSConnection}
 * to communicate with the base station, waits for commands and processes
 * them.
 * @author gilles
 */
public class SALNode {
    private SALBSConnection conn;
    private static Hashtable managers = SensorConstants.getManagers();

    /**
     * This method builds a new SAL node given a {@link SALBSConnection} object
     * to communicate to a SAL base station. The connection will be started by
     * this method.
     * @param c the uninitialised connection to the base station
     * @throws IOException if the connection to the base station cant be opened
     */
    public SALNode(SALBSConnection c) throws IOException{
        c.open(this);
        conn = c;
        Enumeration e = managers.keys();
        while(e.hasMoreElements())
            ((SensorManager) (managers.get(e.nextElement()))).start(c);
    }

    /**
     * this method is called by the {@link SALBSConnection} whenever
     * a {@link SALBSConnection#CMD_DO} arrives and needs processing
     * @param tokens the tokens in DO command
     * @return if an error occurs and the stream can not be started (because of 
     * an incorrect command for example), this method returns whether sending
     * the error reply back to the base station succeeded. If the stream was
     * successfully setup, then this method returns true.
     */
    public boolean processDoCommand(String[] tokens){
        if(managers.containsKey(tokens[1])){
            try {
                ((SensorManager) managers.get(tokens[1])).
                        startStream(tokens[2], Integer.parseInt(tokens[3]));
            } catch (AlreadyStreamingException e){
                System.out.println("Atready streaming");
                return conn.sendAlreadyStreamingError(tokens[1],tokens[2]);
            } catch (IOException ex) {
                System.out.println("Error starting stream: "+ex.getMessage());
                return conn.sendGenericError(tokens[1], tokens[2],
                        ex.getMessage());
            } catch (Throwable t){
                System.out.println("Error starting stream:");
                t.printStackTrace();
                return conn.sendGenericError(tokens[1], tokens[2], 
                        t.getMessage());
            }
        } else {
            System.out.println("unknown sensor type "+tokens[1]);
            return conn.sendGenericError(tokens[1], tokens[2], 
                    "unknown sensor type "+tokens[1]);
        }
        return true;
    }
    
    public void processStopCommand(String[] tokens){
        if(managers.containsKey(tokens[1])){
            try {
                ((SensorManager) managers.get(tokens[1])).stopStream(tokens[2]);
            } catch (Throwable t){
                System.out.println("Error stopping stream:");
                t.printStackTrace();
            }
        } else{
            System.out.println("unknown sensor type "+tokens[1]);
        }
    }

    public void stopManagers(){
        //System.out.println("Terminating all streaming threads");
        Enumeration e = managers.keys();
        while(e.hasMoreElements())
            ((SensorManager) (managers.get(e.nextElement()))).stop();
        //System.out.println("DONE terminating all streaming threads");
    }

    /**
     * This method is called whenever the node must stop
     * its activities and terminate the connection with the
     * base station.
     */
    public void disconnect(){
        System.out.println("Disconnecting from BS");
        conn.terminate();
    }

    /**
     * This method block until the connection with the base station is closed
     */
    public synchronized void waitForDisconnection(){
        try {
            wait();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
