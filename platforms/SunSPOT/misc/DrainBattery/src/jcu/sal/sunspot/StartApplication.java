/*
 * StartApplication.java
 *
 * Created on 1/04/2009 11:24:31;
 */

package jcu.sal.sunspot;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.io.j2me.radiogram.*;

import com.sun.spot.peripheral.IEventHandler;
import com.sun.spot.peripheral.Spot;
import java.io.IOException;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 * 
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class StartApplication extends MIDlet implements Runnable{

    private ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();
    private static RadiogramConnection bc;
    private Thread calc;
    private boolean stop;
    private IEventHandler previousButton;

    protected void startApp() throws MIDletStateChangeException {
        stop = false;
        previousButton = Spot.getInstance().getFiqInterruptDaemon().
                setButtonHandler(new IEventHandler() {

            public void signalEvent() {
                stop = true;
                previousButton.signalEvent();
            }
        });

        calc = new Thread(this);
        calc.start();
        Datagram dg;
        for (int i = 0; i < 8; i++) {
            leds[i].setOn();
            leds[i].setRGB(255, 255, 255);
        }
        try {
            bc = (RadiogramConnection) Connector.open("radiogram://broadcast:254");
            dg = bc.newDatagram(bc.getMaximumLength());
            dg.writeUTF("LET ME KNOW IF THIS DISRUPTS YOUR COMMUNICATIONS");
            while(!stop){
                bc.send(dg);
                Thread.sleep(20);
            }
        } catch (Exception ex) {
        }

        System.out.println("Exiting main");
        

    }

    public void run(){
        while(!stop){
        for(int i=0; i<360 * 100;i++)
            Math.sin(i%360 * Math.PI/180);
        }
        System.out.println("Exiting sin thread");
    }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    /**
     * Called if the MIDlet is terminated by the system.
     * I.e. if startApp throws any exception other than MIDletStateChangeException,
     * if the isolate running the MIDlet is killed with Isolate.exit(), or
     * if VM.stopVM() is called.
     * 
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true when this method is called, the MIDlet must
     *    cleanup and release all resources. If false the MIDlet may throw
     *    MIDletStateChangeException  to indicate it does not want to be destroyed
     *    at this time.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        for (int i = 0; i < 8; i++) {
            leds[i].setOff();
        }
    }
}
