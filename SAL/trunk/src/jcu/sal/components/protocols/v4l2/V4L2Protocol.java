package jcu.sal.components.protocols.v4l2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.Response;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.components.EndPoints.PCIEndPoint;
import jcu.sal.components.EndPoints.UsbEndPoint;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import au.edu.jcu.v4l4j.Control;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

public class V4L2Protocol extends AbstractProtocol {
	
	private static Logger logger = Logger.getLogger(V4L2Protocol.class);
	public final static String PROTOCOL_TYPE = "v4l2";
	public final static String DEVICE_ATTRIBUTE_TAG= "deviceFile";
	public final static String WIDTH_ATTRIBUTE_TAG= "width";
	public final static String HEIGHT_ATTRIBUTE_TAG= "height";
	public final static String CHANNEL_ATTRIBUTE_TAG= "channel";
	public final static String STANDARD_ATTRIBUTE_TAG= "standard";	
	public final static String CONTROL_VALUE_ATTRIBUTE_TAG = "ControlValue";
	
	private int intialQuality = 60;

	
	private FrameGrabber fg = null;
	private Hashtable<String,Control> ctrls = null;
	private boolean streaming;
	private StreamingThread st;
	private StreamingThreadFake stf;
	

	public V4L2Protocol(ProtocolID i, Hashtable<String, String> c,
			Node d){
		super(i, PROTOCOL_TYPE , c, d);
		Slog.setupLogger(logger);
		autoDetectionInterval = -1; //run only once
		supportedEndPointTypes.add(PCIEndPoint.PCIENDPOINT_TYPE);
		supportedEndPointTypes.add(UsbEndPoint.USBENDPOINT_TYPE);
		streaming = false;
	}

	@Override
	protected String internal_getCMLStoreKey(Sensor s)
			throws ConfigurationException {
		return s.getNativeAddress();
	}

	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		if(s.getNativeAddress().equals(CMLDescriptionStore.CCD_KEY)) return true;
		return false;
	}

	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		//Check config directives:
		String dev;
		int w=-1,h=-1,std=-1,ch=-1, cid;
		try {
			dev = getConfig(DEVICE_ATTRIBUTE_TAG);
		} catch (BadAttributeValueExpException e) {
			logger.error("The device file parameter is missing, cant instanciate framegrabber");
			throw new ConfigurationException();
		}
		
		try {
			ch = Integer.parseInt(getConfig(CHANNEL_ATTRIBUTE_TAG));
			std = Integer.parseInt(getConfig(STANDARD_ATTRIBUTE_TAG));
			w = Integer.parseInt(getConfig(WIDTH_ATTRIBUTE_TAG));
			h = Integer.parseInt(getConfig(HEIGHT_ATTRIBUTE_TAG));
		} catch (Exception e1) {}
		
		
		//create the frame grabber object
		try {
			if(w!=-1 && h!=-1 && ch !=-1 && ch!=-1)
				fg = new FrameGrabber(dev, w, h, ch, std, intialQuality);
			else if(std!=-1 && ch!=-1)
				fg = new FrameGrabber(dev, ch, std, intialQuality);
			else 
				fg = new FrameGrabber(dev, intialQuality);
			
			fg.init();

		} catch (V4L4JException e) {
			logger.error("Couldnt create/initialise FrameGrabber object");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch(UnsatisfiedLinkError e) {
			logger.error("Cant load JNI library. Couldnt create/initialise FrameGrabber object");
			throw new ConfigurationException();
		} catch (NoClassDefFoundError e){
			//thrown the second time a FrameGrabber is instanciated and the JNI lib is not found.
			//I suspect that at the first instanciation attempt, since the JVM cant find the JNI lib,
			//it unloads the FrameGrabber class, which is why the second attempt throws NoClassDefFound
			logger.error("Cant load JNI library. Couldnt create FrameGrabber object");
			throw new ConfigurationException();
		}
		
		cmls = CMLDescriptionStore.getStore();
		//get the V4L2 controls
		Control[] v4l2c = fg.getControls();
		ctrls = new Hashtable<String, Control>();
		String key = CMLDescriptionStore.CCD_KEY, name, ctrlName, desc;
		List<String> argNamesEmpty = new Vector<String>();
		List<String> argNamesValue = new Vector<String>();
		argNamesValue.add(CMLDescriptionStore.CONTROL_VALUE_NAME);
		
		List<ArgumentType> tEmpty = new Vector<ArgumentType>();
		List<ArgumentType> tValue = new Vector<ArgumentType>();
		tValue.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));
		
		ReturnType retInt = new ReturnType(CMLConstants.RET_TYPE_INT);
		ReturnType retVoid = new ReturnType(CMLConstants.RET_TYPE_VOID);
		for (int id = 0; id < v4l2c.length; id++) {
			ctrlName = v4l2c[id].getName();
			//add two commands to the CCD sensor for this control
			//(one to set its value, the other to get its value)
			//getValue command
			name = "get"+ctrlName.replace(" ", "");
			desc = "Fetches the value of "+ctrlName;
			cid = cmls.addPrivateCMLDesc(key, GET_CONTROL_METHOD, name, desc, tEmpty, argNamesEmpty , retInt);
			ctrls.put(String.valueOf(cid), v4l2c[id]);
			
			//setValue command
			name = "set"+ctrlName.replace(" ", "");
			desc = "Sets the value of "+ctrlName;
			cid = cmls.addPrivateCMLDesc(key, SET_CONTROL_METHOD, name, desc, tValue, argNamesValue , retVoid);
			ctrls.put(String.valueOf(cid), v4l2c[id]);
		}
	}

	@Override
	protected boolean internal_probeSensor(Sensor s) {
		if(internal_isSensorSupported(s)) {s.enable(); return true;}
		else { s.disconnect(); return false; }
	}

	@Override
	protected void internal_start() throws ConfigurationException {}

	@Override
	protected void internal_stop() {
		if(streaming)
			st.stop();

		//make sure the frame grabber capture is stopped
		try {fg.stopCapture();} catch (V4L4JException e) {}
	}
	
	@Override
	protected void internal_remove() {
		ctrls.clear();
		try {
			fg.remove();
		} catch (V4L4JException e) {
			logger.error("Error while deleting Frame grabber object");
			e.printStackTrace();
		}
	}
	
	@Override
	protected List<String> detectConnectedSensors() {
		List <String> v = new Vector<String>();
		v.add(CMLDescriptionStore.CCD_KEY);
		return v; 
	}

	
	/*
	 * COMMAND HANDLING METHODS
	 */
	
	public static String GET_CONTROL_METHOD = "getControl";
	public byte[] getControl(Command c, Sensor s) throws IOException{
		Control ctrl = ctrls.get(String.valueOf(c.getCID()));
		if(ctrl!=null) {
			try {
				return String.valueOf(ctrl.getValue()).getBytes();
			} catch (V4L4JException e) {
				logger.error("Could NOT read the value for control "+ctrl.getName());
				throw new IOException();
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.getCID());
			throw new IOException();
		}
	}

	public static String SET_CONTROL_METHOD = "setControl";
	public byte[] setControl(Command c, Sensor s) throws IOException{
		Control ctrl = ctrls.get(String.valueOf(c.getCID()));
		if(ctrl!=null) {
			try {
				ctrl.setValue(Integer.parseInt(c.getValue(CMLDescriptionStore.CONTROL_VALUE_NAME)));
				return null;
			} catch (V4L4JException e) {
				logger.error("Could NOT set the value for control "+ctrl.getName());
				throw new IOException();
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.getCID());
			throw new IOException();
		}
	}
	
	public static String GET_FRAME_METHOD = "getFrame";
	public byte[] getFrame(Command c, Sensor s) throws IOException{
		if(streaming) {
			logger.error("Already streaming");
			throw new IOException();
		}
		
		byte[] b;
		ByteBuffer bb;
		try {
			fg.startCapture();
		} catch (V4L4JException e) {
			logger.error("Cant start capture");
			throw new IOException();
		}
		try {
			bb = fg.getFrame();
			b = new byte[bb.limit()];
			bb.get(b);
		} catch (V4L4JException e1) {
			logger.error("Cant capture single frame");
			throw new IOException();
		} finally {
			try {
				fg.stopCapture();
			} catch (V4L4JException e) {
				logger.error("Cant stop capture");
				throw new IOException();
			}
		}
		return b;
	}
	
	public static String START_STREAM_METHOD = "startStream";
	public byte[] startStream(Command c, Sensor s) throws IOException{
		if(streaming){
			logger.error("Already streaming");
			throw new IOException();
		}
		try {
			fg.startCapture();
		} catch (V4L4JException e) {
			logger.error("Cant start capture");
			throw new IOException();
		}
		try {
			st = new StreamingThread(c.getStreamCallBack(CMLDescriptionStore.CALLBACK_ARG_NAME), s);
		} catch (BadAttributeValueExpException e) {
			// TODO Auto-generated catch block
			//FIXME
			e.printStackTrace();
		}
		streaming = true;
		return new byte[0];
	}
	
	public static String STOP_STREAM_METHOD = "stopStream";
	public byte[] stopStream(Command c, Sensor s) throws IOException{
		if(!streaming){
			logger.error("Not streaming");
			throw new IOException();
		}		
		
		st.stop();
		streaming = false;
		return new byte[0];
	}
	
	public static String START_STREAM_FAKE_METHOD = "startStreamFake";
	public byte[] startStreamFake(Command c, Sensor s) throws IOException{
		if(streaming){
			logger.error("Already streaming");
			throw new IOException();
		}
		
		try {
			stf = new StreamingThreadFake(c.getStreamCallBack(CMLDescriptionStore.CALLBACK_ARG_NAME), s);
		} catch (BadAttributeValueExpException e) {
			// TODO Auto-generated catch block
			//FIXME
			e.printStackTrace();
		}
		streaming = true;
		return new byte[0];
	}
	
	public static String STOP_STREAM_FAKE_METHOD = "stopStreamFake";
	public byte[] stopStreamFake(Command c, Sensor s) throws IOException{
		if(!streaming){
			logger.error("Not streaming");
			throw new IOException();
		}		
		
		stf.stop();
		streaming = false;
		return new byte[0];
	}
	
	private class StreamingThread implements Runnable{
		private StreamCallback cb;
		private Thread t;
		private Sensor s;
		private boolean error=false;
		
		public StreamingThread(StreamCallback c, Sensor s){
			cb = c;
			this.s = s;
			t = new Thread(this, "V4L streaming thread");
			t.start();
		}
		
		public void stop(){
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for Streaming thread to finish");
			}
		}

		public void run() {
			byte[] b;
			ByteBuffer bb;
			String sid = s.getID().getName();
//			long ts, ts2, ts3;
			while(!error && !Thread.interrupted()){
				try {
					//ts = System.currentTimeMillis();
					bb = fg.getFrame();
					//ts2 = System.currentTimeMillis();;
					//logger.debug("getFrame: "+(ts2 - ts)+" ms");
					b = new byte[bb.limit()];
					bb.get(b);
					bb.position(0);
					//ts3 = System.currentTimeMillis();
					//logger.debug("prepFrame: "+(ts3 - ts2)+" ms");
					cb.collect(new Response(b, sid));
					//logger.debug("collect: "+(System.currentTimeMillis()- ts3)+" ms");
					//logger.debug("total: "+(System.currentTimeMillis()- ts)+" ms");
				} catch (IOException e1) {
					logger.error("Callback error");
					error=true;
				} catch (Exception e) {
					logger.error("Cant capture frame");
					try {cb.collect(new Response(sid, true));} catch (IOException e1) {}				
					e.printStackTrace();
					error=true;
				}
			}
			try {
				fg.stopCapture();
			} catch (V4L4JException e) {
				logger.error("Cant stop capture");
			}
			
			if(!error)
				try {cb.collect(new Response(sid, false));} catch (IOException e) {}
				
			logger.debug("Capture thread stopped");
			streaming = false;
		}		
	}
	
	private class StreamingThreadFake implements Runnable{
		private StreamCallback cb;
		private Thread t;
		private Sensor s;
		private boolean error=false;
		private byte[] b;
		private int FAKE_ARRAY_SIZE=100000;
		
		
		public StreamingThreadFake(StreamCallback c, Sensor s){
			cb = c;
			this.s = s;
			t = new Thread(this, "V4L streaming thread fake");
			t.start();
		}
		
		public void stop(){
			t.interrupt();
		}
		
		public void join(){
			try {
				t.join();
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for Streaming thread fake to finish");
			}
		}

		public void run() {
			String sid = s.getID().getName();
			//long before;
			while(error==false && !Thread.interrupted()){
				try {
					//before = System.currentTimeMillis();
					b = new byte[FAKE_ARRAY_SIZE];
					for(int i=0; i<FAKE_ARRAY_SIZE; i++)
						b[i]=(byte) (i%100);
					cb.collect(new Response(b, sid));
					//logger.debug("collect: "+(System.currentTimeMillis()- before2)+" ms");
					//logger.debug("total: "+(System.currentTimeMillis()- before)+" ms");
				} catch (IOException e1) {
					logger.error("Callback error");
					error=true;
				}  catch (Exception e) {
					try {cb.collect(new Response(sid, false));} catch (IOException e1) {}
					e.printStackTrace();
					error=true;
				}
			}
			if(!error)
				try {cb.collect(new Response(sid, false));} catch (IOException e) {}
			logger.debug("Capture thread fake stopped");
		}		
	}
	
}
