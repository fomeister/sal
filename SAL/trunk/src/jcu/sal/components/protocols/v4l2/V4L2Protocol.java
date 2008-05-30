package jcu.sal.components.protocols.v4l2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.CMLConstants;
import jcu.sal.common.Command;
import jcu.sal.common.Response;
import jcu.sal.common.StreamCallback;
import jcu.sal.components.EndPoints.PCIEndPoint;
import jcu.sal.components.EndPoints.UsbEndPoint;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.protocols.CMLDescription.ArgTypes;
import jcu.sal.components.protocols.CMLDescription.ReturnType;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.V4L2Control;
import au.edu.jcu.v4l4j.V4L4JException;

public class V4L2Protocol extends AbstractProtocol {
	
	private static Logger logger = Logger.getLogger(V4L2Protocol.class);
	public final static String PROTOCOL_TYPE = "v4l2";
	public final static String DEVICE_ATTRIBUTE_TAG= "deviceFile";
	public final static String WIDTH_ATTRIBUTE_TAG= "width";
	public final static String HEIGHT_ATTRIBUTE_TAG= "height";
	public final static String CHANNEL_ATTRIBUTE_TAG= "channel";
	public final static String STANDARD_ATTRIBUTE_TAG= "standard";	
	public final static String CONTROL_VALUE_ATTRIBUTE_TAG = "ControlValue";
	
	private int intialQuality = 80;

	
	private FrameGrabber fg = null;
	private Hashtable<String,V4L2Control> ctrls = null;
	private boolean streaming;
	private StreamingThread st;
	

	public V4L2Protocol(ProtocolID i, Hashtable<String, String> c,
			Node d) throws ConfigurationException {
		super(i, PROTOCOL_TYPE , c, d);
		Slog.setupLogger(logger);
		autodetect = true;
		AUTODETECT_INTERVAL = -1; //run only once
		cmls = CMLDescriptionStore.getStore();
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
		}
		
		//get the V4L2 controls
		V4L2Control[] v4l2c = fg.getControls();
		ctrls = new Hashtable<String, V4L2Control>();
		String key = CMLDescriptionStore.CCD_KEY, name, ctrlName, desc;
		String[] argNamesEmpty = new String[0];
		String[] argNamesValue = new String[]{CMLDescriptionStore.CONTROL_VALUE_NAME};
		ArgTypes[] tEmpty = new ArgTypes[0];
		ArgTypes[] tValue = new ArgTypes[] {new ArgTypes(CMLConstants.ARG_TYPE_INT)};
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
			name = "set"+name.replace(" ", "");
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
		if(streaming) {
			st.stop();
			st.join();
		}

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
	protected Vector<String> detectConnectedSensors() {
		Vector <String> v = new Vector<String>();
		v.add(CMLDescriptionStore.CCD_KEY);
		return v; 
	}

	
	/*
	 * COMMAND HANDLING METHODS
	 */
	
	public static String GET_CONTROL_METHOD = "getControl";
	public byte[] getControl(Command c, Sensor s) throws IOException{
		V4L2Control ctrl = ctrls.get(String.valueOf(c.getCID()));
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
		V4L2Control ctrl = ctrls.get(String.valueOf(c.getCID()));
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
		st = new StreamingThread(c.getStreamCallBack());
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
	
	private class StreamingThread implements Runnable{
		StreamCallback cb;
		Thread t;
		int stop=0;
		
		public StreamingThread(StreamCallback c){
			cb = c;
			t = new Thread(this);
			t.start();
		}
		
		public void stop(){
			stop=1;
		}
		
		public void join(){
			try {
				t.join();
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for Streaming thread to finish");
			}
		}

		public void run() {
			byte[] b;
			ByteBuffer bb;
			while(stop==0){
				try {
					bb = fg.getFrame();
					b = new byte[bb.limit()];
					bb.get(b);
					bb.position(0);
					cb.collect(new Response(b));
				} catch (V4L4JException e1) {
					logger.error("Cant capture frame");
					stop=1;
				}
			}
			try {
				fg.stopCapture();
			} catch (V4L4JException e) {
				logger.error("Cant stop capture");
			}
		}
		
	}
}
