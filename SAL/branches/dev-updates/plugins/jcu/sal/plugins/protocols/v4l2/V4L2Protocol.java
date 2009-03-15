package jcu.sal.plugins.protocols.v4l2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jcu.sal.common.Response;
import jcu.sal.common.Slog;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.InvalidCommandException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.exceptions.SensorIOException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.plugins.endpoints.PCIEndPoint;
import jcu.sal.plugins.endpoints.UsbEndPoint;

import org.apache.log4j.Logger;

import au.edu.jcu.v4l4j.Control;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.JPEGFrameGrabber;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

public class V4L2Protocol extends AbstractProtocol {
	
	private static Logger logger = Logger.getLogger(V4L2Protocol.class);
	static {Slog.setupLogger(logger);}
	public final static String PROTOCOL_TYPE = "v4l2";
	public final static String DEVICE_ATTRIBUTE_TAG= "deviceFile";
	public final static String WIDTH_ATTRIBUTE_TAG= "width";
	public final static String HEIGHT_ATTRIBUTE_TAG= "height";
	public final static String CHANNEL_ATTRIBUTE_TAG= "channel";
	public final static String STANDARD_ATTRIBUTE_TAG= "standard";	
	public final static String CONTROL_VALUE_ATTRIBUTE_TAG = "ControlValue";

	
	private VideoDevice vd = null;
	private Hashtable<String,Control> ctrls = null;
	private boolean streaming;
	private StreamingThread st;
	private StreamingThreadFake stf;
	

	public V4L2Protocol(ProtocolID i, ProtocolConfiguration c) throws ConfigurationException{
		super(i, PROTOCOL_TYPE , c);
		autoDetectionInterval = -1; //run only once
		supportedEndPointTypes.add(PCIEndPoint.ENDPOINT_TYPE);
		supportedEndPointTypes.add(UsbEndPoint.ENDPOINT_TYPE);
		streaming = false;
	}

	@Override
	protected String internal_getCMLStoreKey(Sensor s) {
		return s.getNativeAddress();
	}

	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		if(s.getNativeAddress().equals(CMLDescriptionStore.CCD_KEY)) return true;
		return false;
	}

	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		String dev;
		int cid;
		//Check config directives:
		try {
			dev = getParameter(DEVICE_ATTRIBUTE_TAG);
		} catch (NotFoundException e) {
			logger.error("The device file parameter is missing, cant instanciate framegrabber");
			throw new ConfigurationException();
		}
		
		//create the frame grabber object
		try {
			vd = new VideoDevice(dev);
		} catch (V4L4JException e) {
			logger.error("Couldnt create/initialise VideoDevice object");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch(UnsatisfiedLinkError e) {
			logger.error("Cant load JNI library. Couldnt create/initialise VideoDevice object");
			throw new ConfigurationException();
		} catch (NoClassDefFoundError e){
			//thrown the second time a VideoDevice is instantiated and the JNI lib is not found.
			//I suspect that at the first instantiation attempt, since the JVM cant find the JNI lib,
			//it unloads the VideoDevice class, which is why the second attempt throws NoClassDefFound
			logger.error("Cant load JNI library. Couldnt create VideoDevice object");
			throw new ConfigurationException();
		}
		
		cmls = CMLDescriptionStore.getStore();
		//get the V4L2 controls
		List<Control> v4l2c = vd.getControlList().getList();
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
		for (Control c: v4l2c) {
			ctrlName = c.getName();
			//add two commands to the CCD sensor for this control
			//(one to set its value, the other to get its value)
			//getValue command
			name = "Get"+ctrlName.replace(" ", "");
			desc = "Fetches the value of "+ctrlName;
			try {
				cid = cmls.addPrivateCMLDesc(key, GET_CONTROL_METHOD, name, desc, tEmpty, argNamesEmpty , retInt);
			} catch (AlreadyPresentException e) {
				logger.error("we shouldnt be here - trying to insert a duplicate element in CML table");
				e.printStackTrace();
				throw new SALRunTimeException("trying to insert a duplicate element in CML table",e);
			}
			ctrls.put(String.valueOf(cid), c);
			
			//setValue command
			name = "Set"+ctrlName.replace(" ", "");
			desc = "Sets the value of "+ctrlName;
			try {
				cid = cmls.addPrivateCMLDesc(key, SET_CONTROL_METHOD, name, desc, tValue, argNamesValue , retVoid);
			} catch (AlreadyPresentException e) {
				logger.error("we shouldnt be here - trying to insert a duplicate element in CML table");
				e.printStackTrace();
				throw new SALRunTimeException("trying to insert a duplicate element in CML table",e);
			}
			ctrls.put(String.valueOf(cid), c);
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
		vd.releaseControlList();
		vd.release();
	}
	
	@Override
	protected void internal_remove() {
		ctrls.clear();
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
	public byte[] getControl(Command c, Sensor s) throws SensorControlException{
		Control ctrl = ctrls.get(String.valueOf(c.getCID()));
		if(ctrl!=null) {
			try {
				return String.valueOf(ctrl.getValue()).getBytes();
			} catch (V4L4JException e) {
				logger.error("Could NOT read the value for control "+ctrl.getName());
				throw new SensorIOException("Error reading the contol value (CID:'"+c.getCID()+"')",e);
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.getCID());
			throw new SensorIOException("Error finding the control to read from (CID:'"+c.getCID()+"')");
		}
	}

	public static String SET_CONTROL_METHOD = "setControl";
	public byte[] setControl(Command c, Sensor s) throws SensorControlException{
		Control ctrl = ctrls.get(String.valueOf(c.getCID()));
		if(ctrl!=null) {
			try {
				ctrl.setValue(Integer.parseInt(c.getValue(CMLDescriptionStore.CONTROL_VALUE_NAME)));
				return null;
			} catch (V4L4JException e) {
				logger.error("Could NOT set the value for control "+ctrl.getName());
				throw new SensorIOException("Error setting the value on the control (CID:'"+c.getCID()+"')", e);
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.getCID());
			throw new SensorIOException("Error finding the control to set value on (CID:'"+c.getCID()+"')");
		}
	}
	
	public static String GET_FRAME_METHOD = "getFrame";
	public byte[] getFrame(Command c, Sensor s) throws SensorControlException{
		if(streaming) {
			logger.error("Already streaming");
			throw new InvalidCommandException("The sensor is streaming data");
		}
		
		byte[] b;
		ByteBuffer bb;
		JPEGFrameGrabber fg;
		try {
			fg = vd.getJPEGFrameGrabber(c.getIntValue(CMLDescriptionStore.WIDTH_VALUE_NAME),
					c.getIntValue(CMLDescriptionStore.HEIGHT_VALUE_NAME), c.getIntValue(CMLDescriptionStore.CHANNEL_VALUE_NAME),
					c.getIntValue(CMLDescriptionStore.STANDARD_VALUE_NAME), c.getIntValue(CMLDescriptionStore.QUALITY_VALUE_NAME));
		} catch (V4L4JException e) {
			logger.error("Error getting JPEG frame grabber");
			throw new SensorIOException("Error getting JPEG frame grabber",e);
		}
		
		try {
			fg.startCapture();
		} catch (V4L4JException e) {
			logger.error("Error starting capture");
			vd.releaseFrameGrabber();
			throw new SensorIOException("Error starting capture",e);
		}
		
		try {
			bb = fg.getFrame();
			b = new byte[bb.limit()];
			bb.get(b);
		} catch (V4L4JException e1) {
			logger.error("Error while capturing frame");
			throw new SensorIOException("Error while capturing frame",e1);
		} finally {
			fg.stopCapture();
			vd.releaseFrameGrabber();
		}
		return b;
	}
	
	public static String START_STREAM_METHOD = "startStream";
	public byte[] startStream(Command c, Sensor s) throws SensorControlException{
		JPEGFrameGrabber fg;
		if(streaming)
			throw new InvalidCommandException("The sensor is streaming data");

		try {
			fg = vd.getJPEGFrameGrabber(c.getIntValue(CMLDescriptionStore.WIDTH_VALUE_NAME),
					c.getIntValue(CMLDescriptionStore.HEIGHT_VALUE_NAME), c.getIntValue(CMLDescriptionStore.CHANNEL_VALUE_NAME),
					c.getIntValue(CMLDescriptionStore.STANDARD_VALUE_NAME), c.getIntValue(CMLDescriptionStore.QUALITY_VALUE_NAME));
			fg.startCapture();
		} catch (V4L4JException e) {
			logger.error("Error while starting capture");
			vd.releaseFrameGrabber();
			throw new SensorIOException("Error while starting capture", e);
		}
		st = new StreamingThread(c.getStreamCallBack(CMLDescriptionStore.CALLBACK_ARG_NAME), s, fg, c.getCID());

		streaming = true;
		return new byte[0];
	}
	
	public static String STOP_STREAM_METHOD = "stopStream";
	public byte[] stopStream(Command c, Sensor s) throws SensorControlException{
		if(!streaming)
			throw new InvalidCommandException("The sensor is not streaming");
		
		
		st.stop();
		vd.releaseFrameGrabber();
		streaming = false;
		return new byte[0];
	}
	
	public static String START_STREAM_FAKE_METHOD = "startStreamFake";
	public byte[] startStreamFake(Command c, Sensor s) throws SensorControlException{
		if(streaming)
			throw new InvalidCommandException("The sensor is already streaming");
		
		stf = new StreamingThreadFake(c.getStreamCallBack(CMLDescriptionStore.CALLBACK_ARG_NAME), s,c.getCID());

		streaming = true;
		return new byte[0];
	}
	
	public static String STOP_STREAM_FAKE_METHOD = "stopStreamFake";
	public byte[] stopStreamFake(Command c, Sensor s) throws SensorControlException{
		if(!streaming)
			throw new InvalidCommandException("The sensor is not streaming");
		
		stf.stop();
		streaming = false;
		return new byte[0];
	}
	
	private class StreamingThread implements Runnable{
		private StreamCallback cb;
		private FrameGrabber fg;
		private Thread t;
		private Sensor s;
		private int cid;
		private boolean error=false;
		private boolean stop = false;
		
		public StreamingThread(StreamCallback c, Sensor s, FrameGrabber f, int cid){
			cb = c;
			fg = f;
			this.s = s;
			this.cid = cid;
			t = new Thread(this, "V4L streaming thread");
			stop = false;
			t.start();
		}
		
		public void stop(){
			//t.interrupt();
			stop = true;
			try { t.join();} catch (InterruptedException e) {}
		}

		public void run() {
			byte[] b;
			ByteBuffer bb;
			String sid = s.getID().getName();
//			long ts, ts2, ts3;
			//while(!error && !Thread.interrupted()){
			while(!error && !stop){
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
					cb.collect(new Response(b, cid, sid));
					//logger.debug("collect: "+(System.currentTimeMillis()- ts3)+" ms");
					//logger.debug("total: "+(System.currentTimeMillis()- ts)+" ms");
				} catch (IOException e1) {
					logger.error("Callback error");
					error=true;
				} catch (Throwable e) {
					logger.error("Error while capturing frame");
					e.printStackTrace();
					
					try {
						//PrintWriter pw = new PrintWriter(new StringWriter());
						//e.printStackTrace(pw);
						cb.collect(new Response(sid, cid, new SensorIOException("Error while capturing frame."+e.getMessage())));
					} catch (IOException e1) {
						logger.error("Error notifying the client");
						e1.printStackTrace();
					}				
					error=true;
				}
			}
			fg.stopCapture();
			vd.releaseFrameGrabber();
			
			if(!error)
				try {cb.collect(new Response(sid, cid, new ClosedStreamException()));} catch (IOException e) {}
				
			//logger.debug("Capture thread stopped");
			streaming = false;
		}		
	}
	
	private class StreamingThreadFake implements Runnable{
		private StreamCallback cb;
		private Thread t;
		private Sensor s;
		private int cid;
		private boolean error=false;
		private byte[] b;
		private int FAKE_ARRAY_SIZE=100000;
		
		
		public StreamingThreadFake(StreamCallback c, Sensor s, int cid){
			cb = c;
			this.s = s;
			this.cid = cid;
			t = new Thread(this, "V4L streaming thread fake");
			t.start();
		}
		
		public void stop(){
			t.interrupt();
		}
		
		public void join(){
			try { t.join();	} catch (InterruptedException e) {}
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
					cb.collect(new Response(b, cid, sid));
					//logger.debug("collect: "+(System.currentTimeMillis()- before2)+" ms");
					//logger.debug("total: "+(System.currentTimeMillis()- before)+" ms");
				} catch (IOException e1) {
					logger.error("Callback error");
					error=true;
				}  catch (Exception e) {
					try {cb.collect(new Response(sid, cid, new SensorIOException("Error while capturing frame", e)));} catch (IOException e1) {}
					e.printStackTrace();
					error=true;
				}
			}
			if(!error)
				try {cb.collect(new Response(sid, cid, new ClosedStreamException()));} catch (IOException e) {}
			logger.debug("Capture thread fake stopped");
		}		
	}
	
}
