package jcu.sal.plugins.sunspot.Spot;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.ILed;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.util.Utils;
import com.sun.squawk.util.StringTokenizer;
import java.io.IOException;
import java.lang.Thread;
import java.util.Random;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

/**
 *
 * @author gilles
 */
public class BSFinder {
    private static ILed led = Spot.getInstance().getRedLed();
    public final static int BROADCAST_PORT = 34;

    private final static String I_M_A_SAL_SPOT="SALSpot";
    private final static String I_M_A_SAL_BS="SALBS";
    private final static String INTERRUPTED_URL="NOTAURL";
    private final static int LISTENER_TIMEOUT=1000;

    //for broadcast
    private static RadiogramConnection bcConnection;
    
    //for establishing the connection
    private static RadiogramConnection bsHandshake;

    private static long ourAddress = RadioFactory.
            getRadioPolicyManager().getIEEEAddress();

    private static String url;
    private static BSFinder c = new BSFinder();
    private static Thread listener = null, broadcaster = null;
    private static boolean stopBCThread;

    public static String getUrl() throws InterruptedException{
        synchronized (c) {
            url = "";
            startListenforBS();
            startBroadcastThread();
            try {
                c.wait();
            } catch (InterruptedException ex) {}
            joinThreads();
            if(url.equals(INTERRUPTED_URL))
               throw new InterruptedException();
            else {
                //System.out.println("Returning "+url);
                return url;
            }
        }
    }

    public static void stopThreads(){
        synchronized(c){
            url=INTERRUPTED_URL;
            stopBCThread = true;
            joinThreads();
            c.notify();
        }
    }

    private static void joinThreads(){
        if (listener != null) {
            try {listener.join();}
            catch (InterruptedException ex) {}
            listener = null;
        }
        if(broadcaster!=null){
            broadcaster.interrupt();
            try {broadcaster.join();}
            catch (InterruptedException ex) {}
            broadcaster = null;
        }
    }

    private static void startListenforBS(){
        listener = new Thread(new Runnable(){
           public void run(){
               System.out.println("Starting listener thread");
                try {
                    listenForBS();
                } catch (Throwable e) {
                    System.out.println("Error listening for BS");
                    e.printStackTrace();
                }
               System.out.println("listener thread exited");
           }
        });
        listener.start();
    }

    private static String[] split(String msg){
        StringTokenizer stk = new StringTokenizer(msg, " ");
        String [] result = new String[stk.countTokens()];
        for (int i = 0; stk.hasMoreTokens(); i++) {
            result[i] = stk.nextToken();
        }
        return result;
    }


    private static void listenForBS() throws IOException{
        String[] msg;
        int port;
        bsHandshake = (RadiogramConnection) Connector.open("radiogram://:"+BROADCAST_PORT);
        bsHandshake.setTimeout(LISTENER_TIMEOUT);
        Datagram dg = bsHandshake.newDatagram(bsHandshake.getMaximumLength());
        while (url.equals("")) {
            try {
                bsHandshake.receive(dg);
                msg = split(dg.readUTF());
                if (msg[0].equals(I_M_A_SAL_BS) && msg.length == 2) {
                    //stop broadcasting thread
                    stopBCThread = true;
                    broadcaster.interrupt();

                    port = Integer.parseInt(msg[1]);
                    synchronized (c) {
                        if (url.equals("")) {
                            url = "radiostream://" + dg.getAddress() + ":" + port;
                            c.notify();
                        }
                    //else url!="" means someone called stopThreads(), so do nothing
                    }
                } //else
                    //System.out.println("Unknown message");
            } catch (TimeoutException ex) {}

        }
        bsHandshake.close();
    }

    private static void startBroadcastThread(){
        stopBCThread = false;
        broadcaster = new Thread(new Runnable(){
           public void run(){
               Random rand = new Random(ourAddress);
               System.out.println("Starting broadcasting thread");
               try {
                   bcConnection = (RadiogramConnection) Connector.open("radiogram://broadcast:" + BROADCAST_PORT);
               } catch (IOException ex) {
                   System.out.println("Could not open radiogram broadcast connection");
                   return;
               }

                try{
                    while (!stopBCThread) {
                        led.setOn();
                        sendBroadcastMessage(I_M_A_SAL_SPOT);
                        led.setOff();
                        Thread.sleep(3000);// + rand.nextInt(1000));
                    }
                }catch (InterruptedException e){}

                try {
                    bcConnection.close();
                } catch (IOException ex) {
                    System.out.println("Could not close radiogram broadcast connection");
                }
               System.out.println("broadcasting thread exited");
           }
        });
        broadcaster.start();
    }


    private static void sendBroadcastMessage(String m){
        try {
            Datagram dg = bcConnection.newDatagram(bcConnection.getMaximumLength());
            dg.writeUTF(m);
            bcConnection.send(dg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
