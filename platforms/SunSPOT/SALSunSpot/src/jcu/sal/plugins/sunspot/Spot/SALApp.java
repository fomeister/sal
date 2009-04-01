/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcu.sal.plugins.sunspot.Spot;

import com.sun.spot.peripheral.IBattery;
import com.sun.spot.peripheral.IEventHandler;
import com.sun.spot.peripheral.IUSBPowerDaemon;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ISwitch;
import com.sun.spot.sensorboard.peripheral.ISwitchListener;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.LEDColor;
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
    private Thread showBattLevel;
    private ITriColorLED[] leds = EDemoBoard.getInstance().getLEDs();
    private IBattery batt = Spot.getInstance().getPowerController().getBattery();
    private IUSBPowerDaemon usbPower = Spot.getInstance().getUsbPowerDaemon();

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        System.out.println("destroyApp called ?? ...");
    }

    protected void pauseApp() {
        /*
         * never called
         */
    }

    public void run(){
//        ISleepManager m = Spot.getInstance().getSleepManager();
//        m.enableDeepSleep();
//        System.out.println("IS deep sleep enabled: "+m.isDeepSleepEnabled());
//        System.out.println("Minimum sleep time for deep sleep: "+m.getMinimumDeepSleepTime());
//        System.out.println("Maximum sleep time for shallow sleep: "+m.getMaximumShallowSleepTime());
//        while(!stop){
//            System.out.println("Total deep sleep time : "+m.getTotalDeepSleepTime());
//            System.out.println("Total shallow sleep time : "+m.getTotalShallowSleepTime());
//            try {
//                m.ensureDeepSleep(10000);
//            } catch (UnableToDeepSleepException ex) {
//                ex.printStackTrace();
//                Utils.sleep(10000);
//            }
//        }
        boolean prev = !usbPower.isUsbPowered(), curr;
        try {
            while (!stop) {
                curr = usbPower.isUsbPowered();
                if (prev != curr) {
                    showBatteryPower(curr);
                    prev = curr;
                }
                Thread.sleep(3000);
            }
        } catch (InterruptedException ex) {}
        showBatteryPower(false);
    }

    protected void startApp() throws MIDletStateChangeException {
        String url;
        System.out.println("IS deep sleep enabled: "+Spot.getInstance().getSleepManager().isDeepSleepEnabled());

        previousButton = Spot.getInstance().getFiqInterruptDaemon().
                setButtonHandler(new IEventHandler() {

            public void signalEvent() {
                try{disconnect();} catch (Throwable t){}
                previousButton.signalEvent();
            }
        });

        previousPowerOff = Spot.getInstance().getFiqInterruptDaemon().
                setPowerOffHandler(new IEventHandler() {

            public void signalEvent() {
                try{disconnect();} catch (Throwable t){}
                previousPowerOff.signalEvent();
            }
        });
        EDemoBoard.getInstance().getSwitches()[0].addISwitchListener(this);
        EDemoBoard.getInstance().getSwitches()[1].addISwitchListener(this);

        stop = false;
        showBattLevel = new Thread(this);
        showBattLevel.start();

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
        System.out.println("Exiting SAL app");
    }

    private void disconnect() {
        System.out.println("Calling DISCONNECT");
        stop = true;
        showBattLevel.interrupt();
        BSFinder.stopThreads();
        if (n != null) {
            n.stopManagers();
            n.disconnect();
        }
        System.out.println("Done calling DISCONNECT");
    }

    private void showBatteryPower(boolean show){
        if(show){
            //show battery level with leds
            int nbLeds = leds.length, i;
            float ratio = batt.getBatteryLevel()*7f/100f;
            for(i=0;i<ratio-1;i++) {
                leds[i].setOn();
                leds[i].setRGB(0, 20, 0);
            }
            leds[i].setOn();
            leds[i].setRGB( 0, (int) ((ratio - ((int) ratio))*20) , 0);
        } else //turn everything off
            for(int i=0;i<leds.length;i++)
                leds[i].setOff();
    }

    public void switchPressed(ISwitch sw) {
        showBatteryPower(true);
    }

    public void switchReleased(ISwitch sw) {
        if(!usbPower.isUsbPowered())
            showBatteryPower(false);
    }

}
