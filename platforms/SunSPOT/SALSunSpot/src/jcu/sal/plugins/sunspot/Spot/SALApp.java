/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcu.sal.plugins.sunspot.Spot;

import com.sun.spot.peripheral.IBattery;
import com.sun.spot.peripheral.IEventHandler;
import com.sun.spot.peripheral.ISleepManager;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.peripheral.UnableToDeepSleepException;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ISwitch;
import com.sun.spot.sensorboard.peripheral.ISwitchListener;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.util.Utils;
import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 *
 * @author gilles
 */
public class SALApp extends MIDlet implements Runnable, ISwitchListener{

    private SALNode n = null;
    private boolean stop;
    private IEventHandler previousButton,  previousPowerOff;
    private Thread t;
    private ITriColorLED[] leds = EDemoBoard.getInstance().getLEDs();
    private IBattery batt = Spot.getInstance().getPowerController().getBattery();

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        System.out.println("destroyApp called ?? ...");
    }

    protected void pauseApp() {
        /*
         * never called
         */
    }

    public void run(){
        ISleepManager m = Spot.getInstance().getSleepManager();
        m.enableDeepSleep();
        System.out.println("IS deep sleep enabled: "+m.isDeepSleepEnabled());
        System.out.println("Minimum sleep time for deep sleep: "+m.getMinimumDeepSleepTime());
        System.out.println("Maximum sleep time for shallow sleep: "+m.getMaximumShallowSleepTime());
        while(!stop){
            System.out.println("Total deep sleep time : "+m.getTotalDeepSleepTime());
            System.out.println("Total shallow sleep time : "+m.getTotalShallowSleepTime());
            try {
                m.ensureDeepSleep(10000);
            } catch (UnableToDeepSleepException ex) {
                ex.printStackTrace();
                Utils.sleep(10000);
            }
        }

    }

    protected void startApp() throws MIDletStateChangeException {
        String url;

        previousButton = Spot.getInstance().getFiqInterruptDaemon().
                setButtonHandler(new IEventHandler() {

            public void signalEvent() {
                try{disconnect();} catch (Throwable t){t.printStackTrace();}
                previousButton.signalEvent();
            }
        });

        previousPowerOff = Spot.getInstance().getFiqInterruptDaemon().
                setPowerOffHandler(new IEventHandler() {

            public void signalEvent() {
                disconnect();
                previousPowerOff.signalEvent();
            }
        });
        EDemoBoard.getInstance().getSwitches()[0].addISwitchListener(this);
        EDemoBoard.getInstance().getSwitches()[1].addISwitchListener(this);

        stop = false;
//        t = new Thread(this);
//        t.start();

        while (!stop) {
            try {
                n = new SALNode(new SALBSConnection(BSFinder.getUrl()));
                n.waitForDisconnection();
            } catch (InterruptedException ex) {
                //interrupted while waiting for BS connection
                stop = true;
            } catch (IOException ex) {
                System.out.println("Error creating SALNode");
            }
        }
    }

    public void disconnect() {
        System.out.println("Calling DISCONNECT");
        stop = true;
//        t.interrupt();
        BSFinder.stopThreads();
        if (n != null) {
            n.stopManagers();
            n.disconnect();
        }
        System.out.println("Done calling DISCONNECT");
    }

    private void showBatteryPower(){
        int nbLeds = leds.length, i;
        float ratio = batt.getBatteryLevel()*8f/100f;
//        System.out.println("Battery level: "+batt.getBatteryLevel());
//        System.out.println("Scaled battery level: "+ratio);
        for(i=0;i<ratio-1;i++) {
            leds[i].setOn();
            leds[i].setRGB(0, 50, 0);
        }
//        System.out.println("Setting led "+i+" to "+((int) ((ratio - ((int) ratio))*100)));
        leds[i].setOn();
        leds[i].setRGB( 0, (int) ((ratio - ((int) ratio))*50) , 0);

    }

    public void switchPressed(ISwitch sw) {
        showBatteryPower();
    }

    public void switchReleased(ISwitch sw) {
        for(int i=0;i<leds.length;i++)
            leds[i].setOff();
    }

}
