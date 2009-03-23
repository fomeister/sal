package jcu.sal.components.protocols;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jcu.sal.common.Response;
import jcu.sal.common.Slog;
import jcu.sal.common.StreamID;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.exceptions.SensorDisconnectedException;
import jcu.sal.components.sensors.Sensor;

import org.apache.log4j.Logger;

public class DefaultStreamingThread implements StreamingThread, Runnable {
	private static Logger logger = Logger.getLogger(DefaultStreamingThread.class);
	static {Slog.setupLogger(logger);}	
	
	private Thread t;
	private int interval;
	private AbstractProtocol protocol;
	private Command cmd;
	private Method method;
	private Sensor sensor;
	private StreamingThreadListener listener;
	private LocalStreamID lid;
	private StreamID sid;
	private boolean stop;
	
	public DefaultStreamingThread(Method m, AbstractProtocol o, Sensor s, Command c, StreamingThreadListener l, LocalStreamID id){
		stop = false;
		sensor = s;
		method = m;
		protocol = o;
		lid = id;
		interval = c.getInterval();
		cmd = c;
		listener = l;
		sid = new StreamID(id.getSID(),id.getCID(),id.getPID());
	}

	@Override
	public synchronized void start(){
		if(t==null){
			t = new Thread(this, "Streaming thread");
			t.start();
		} else
			throw new SALRunTimeException("This thread has already been started");
	}

	@Override
	public synchronized void stop() {
		if(t!=null && t.isAlive()){
			stop = true;
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void update(int i) {
		interval = i;
	}
	
	@Override
	public void run(){
		Response r;
		boolean error = false;
		try {
			while(!stop && !error){
				try {
					r = new Response((byte[]) method.invoke(protocol, cmd, sensor),sid);
				}catch (InvocationTargetException e) {
					//method threw an exception
					e.printStackTrace();
					error = true;
					r = new Response(sid, new SensorControlException("Sensor control error:\n"+e.getCause().getMessage()));

					if(e.getCause() instanceof SensorDisconnectedException){
						logger.debug("Disconnecting sensor '"+sensor.getID().getName()+"("+sensor.getNativeAddress()+")'");
						sensor.disconnect();
					} else {
						logger.debug("Stream stopped on sensor '"+sensor.getID().getName()+"("+sensor.getNativeAddress()+")'");
						sensor.stopStream();
					}
					
				} catch (Throwable t) {
					error = true;
					r = new Response(sid, new SensorControlException("Programming error in the protocol subclass:\n"+t.getMessage()));

					logger.error("Could NOT run the command (error with invoke() )");
					t.printStackTrace();
					sensor.stopStream();
				}
				
				try {
					cmd.getStreamCallBack().collect(r);
				} catch (IOException e) {
					error = true;
					logger.debug("Error calling stream callback - terminating stream");
					e.printStackTrace();
					sensor.stopStream();
				}
				if(!error && !stop)
					Thread.sleep(interval);
			}
		} catch (InterruptedException e) {}
		catch (Throwable t) {
			t.printStackTrace();
		}
		if(!error){
			sensor.stopStream();
			try {
				cmd.getStreamCallBack().collect(new Response(sid, new ClosedStreamException()));
			} catch (IOException e) {}
		}
		listener.threadExited(lid);
	}

}
