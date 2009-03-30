/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcu.sal.plugins.sunspot.Spot;

import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.peripheral.ILed;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.squawk.util.StringTokenizer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 *
 * @author gilles
 */
public class SALBSConnection implements Runnable {
    private static ILed led = Spot.getInstance().getGreenLed();
    public final static String HELLO_FROM_BS = "HelloSALSpot";
    public final static String HELLO_TO_BS = "HelloSALBS";
    public final static int SOCKET_TIMEOUT = 1000;
    //how many missed pings before we disconnect
    //in units of SOCKET_TIMEOUT seconds
    public final static int PING_TIMEOUT = 5;
    private final static String DELIM = "@";
    private final static String CLOSE = "Close";
    private final static String PING = "Ping";
    private final static String CMD_DO = "DO";
    private final static String CMD_STOP = "STOP";
    private final static String REPLY_DATA = "REPLY";
    private final static String REPLY_ERROR = "ERROR";
    private RadiostreamConnection conn;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String address;
    private int port;
    private SALNode client;
    private Thread commandListener;
    private boolean stop;
    private State state;
    private int missedPings;

    public SALBSConnection(String url){
        address = url.substring(url.indexOf("//") + 2, url.lastIndexOf(':'));
        port = Integer.parseInt(url.substring(url.lastIndexOf(':') + 1));
//        System.out.println("Address: " +address);
//        System.out.println("Port: " +port);
        state = new State();

    }

    /**
     * This method opens the connection to the base station
     * @throws java.io.IOException if there is an error opening
     * the connection, or if the client is not set.
     */
    public synchronized void open(SALNode c) throws IOException {
        if (!state.connect()) {
            return;
        }

        client = c;
        String url = "radiostream://" + address + ":" + port;
        conn = (RadiostreamConnection) Connector.open(url);
        conn.setTimeout(SOCKET_TIMEOUT);
        dis = conn.openDataInputStream();
        dos = conn.openDataOutputStream();
        doHanshake();
        stop = false;
        commandListener = new Thread(this, "Command listener");
        commandListener.start();
    }

    private void doHanshake() throws IOException {
        int i=3;
        while(i-->0){
            try {
                if (dis.readUTF().equals(HELLO_FROM_BS)) {
                    dos.writeUTF(HELLO_TO_BS);
                    dos.flush();
                    System.out.println("Connected to BS !");
                }
            } catch(TimeoutException e){
            } catch (IOException ex) {
                System.out.println("Error during handshake");//: " + ex.getMessage());
                close();
                throw ex;
            }
        }
    }

    public int getPort() {
        return port;
    }

    public String getRemoteAddress() {
        return address;
    }

    /**
     * This method sends a reply packet containing data for a stream
     * @param s the sensor
     * @param c the comand
     * @param data the data value
     * @return whether the message was sent
     */
    public boolean sendSensorData(String s, String c, String d) {
        try {
            sendData(REPLY_DATA + " " + s+ " "+c+" "+d);
            return true;
        } catch (IOException ex) {
            System.out.println("Error sendind error reply (" + ex.getMessage() + ") - stopping BS connection");
            stop = true;
        }
        return false;
    }

    /**
     * This method sends a reply error packet, signalling an error for a stream.
     * @param s the sensor
     * @param c the comand
     * @return whether the message was sent
     */
    public boolean sendAlreadyStreamingError(String s, String c) {
        try {
            sendData(REPLY_ERROR + " " + s+" "+c + " Already streaming");
            return true;
        } catch (IOException ex) {
            System.out.println("Error sendind error reply (" + ex.getMessage() + ") - stopping BS connection");
            stop = true;
        }
        return false;
    }

    /**
     * This method sends a reply error packet, signalling an error for a stream.
     * @param s the sensor
     * @param c the comand
     * @param d the error message
     * @return whether the message was sent
     */
    public boolean sendGenericError(String s, String c, String d) {
        try {
            sendData(REPLY_ERROR + " " + s+" "+c+" "+ d);
            return true;
        } catch (IOException ex) {
            System.out.println("Error sendind error reply (" + ex.getMessage() + ") - stopping BS connection");
            stop = true;
        }
        return false;
    }

    public synchronized void terminate() {
        if (!state.terminate()) {
            return;
        }
        //System.out.println("Terminating connection with BS");
        try {
            sendData(CLOSE);
        } catch (IOException e) {
            //System.out.println("Error sending CLOSE: " + e.getMessage());
        }
        stopCommandListener();
        close();
    }

    private void close() {
        if (!state.close()) {
            return;
        }

        client.stopManagers();
        //close outputstream
        synchronized (dos) {
            try {
                dos.close();
            } catch (IOException e) {
            }
        }

        //close input stream
        synchronized (dis) {
            try {
                dis.close();
            } catch (IOException e) {
            }
        }

        //close connection
        try {
            conn.close();
        } catch (IOException e) {
        }
        System.out.println("Connection with BS closed");
        synchronized (client) {
            client.notify();
        }
    }

    /**
     * Sends a ping
     * @return whether or not the ping was sent
     */
	private boolean sendPing(){
		try {
			sendData(PING);
            return true;
		} catch (IOException e){
			System.out.println("Error sending PING command - closing");
            stop = true;
		}
        return false;
	}

    private String readData() throws IOException {
        synchronized (dis) {
            return dis.readUTF();
        }
    }

    private void sendData(String s) throws IOException {
        synchronized (dos) {
            dos.writeUTF(DELIM + s + DELIM);
            dos.flush();
        }
    }

    private void stopCommandListener() {
        synchronized (commandListener) {
            //stop thread
            if (commandListener.isAlive()) {
                stop = true;
                try {
                    commandListener.join();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * This method process a string received form the base station.
     * @param d the string
     * @return if there is an IO error, this method returns false, otherwise
     * true
     */
    private boolean processCmdData(String d) {
        String[] commands = split(d, DELIM);
        String[] tokens;
        for (int i = 0; i < commands.length; i++) {
            tokens = split(commands[i], " ");
            if (tokens[0].equals(CLOSE)) {
                System.out.println("Received Close command from BS - closing");
                stop = true;
                return true;
            } else if (tokens[0].equals(PING)) {
                led.setOn();
                sendPing();
                missedPings = 0;
                led.setOff();
                return true;
            } else if (tokens[0].equals(CMD_DO)) {
                System.out.println("Processing command: " + commands[i]);
                return client.processDoCommand(tokens);
            } else if (tokens[0].equals(CMD_STOP)) {
                System.out.println("Processing command: " + commands[i]);
                client.processStopCommand(tokens);
            } else {
                System.out.println("unknown command: " + tokens[0]);
            }
        }
        return true;

    }

    public void run() {
        missedPings = 0;
        String data, leftOver = "";
        while (!stop) {
            try {
                data = leftOver + readData();
                //System.out.println("Received " + data);
                if (data.endsWith(DELIM)) {
                } else {
                    System.out.println("DATA DOES NOT END WITH " + DELIM);
                    leftOver =data.substring(data.lastIndexOf(DELIM.charAt(0)));
                    data = data.substring(0,
                            data.lastIndexOf(DELIM.charAt(0) - 1));
                    System.out.println("LEFTOVER: " + leftOver);
                    System.out.println("Processing first bit: " + data);
                }

                if (!processCmdData(data)) {
                    System.out.println("Error processing the command" +
                            " - closing connection");
                    stop = true;
                }

            } catch (TimeoutException e) {
                if ((++missedPings) >= PING_TIMEOUT) {
                    System.out.println("Ping timeout from SAL BS " +
                            "- closing connection");
                    stop = true;
                }
            } catch (IOException e) {
                System.out.println("IO Error reading from SAL BS (" +
                        e.getMessage() + ")- closing connection");
                stop = true;
            }
        }
        close();
        //System.out.println("Command listener thread exited");
    }
    
    private static String[] split(String msg, String delim) {
        StringTokenizer stk = new StringTokenizer(msg, delim);
        String[] result = new String[stk.countTokens()];
        for (int i = 0; stk.hasMoreTokens(); i++) {
            result[i] = stk.nextToken();
        }
        return result;
    }

    private static class State {

        private static final int DISCONNECTED = 0;
        private static final int IDLE = 1;
        private static final int STREAMING = 2;
        private static final int TERMINATING = 3;
        private static final int CLOSED = 4;
        private int streamCount;
        private int state;

        public State() {
            state = DISCONNECTED;
            streamCount = 0;
        }

        public synchronized boolean connect() {
            switch (state) {
                case DISCONNECTED:
                    state = IDLE;
                    return true;
                default:
                    return false;
            }
        }

        public synchronized void startStream() {
            switch (state) {
                case IDLE:
                case STREAMING:
                    state = STREAMING;
                    streamCount++;
                    return;
                default:
                    throw new StateException("Not idle or streaming");
            }
        }

        public synchronized void stopStream() {
            switch (state) {
                case STREAMING:
                    if (--streamCount == 0) {
                        state = IDLE;
                    }
                    return;
                default:
                    throw new StateException("Not streaming");
            }
        }

        public synchronized boolean terminate() {
            switch (state) {
                case IDLE:
                case STREAMING:
                    state = TERMINATING;
                    return true;
                default:
                    return false;
            }
        }

        public synchronized boolean close() {
            switch(state){
                case IDLE:
                case STREAMING:
                    state = CLOSED;
                    return true;
                default:
                    return false;
            }
        }
    }
}
