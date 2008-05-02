package jcu.sal.components.protocols.v4l2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.Command;
import jcu.sal.components.EndPoints.PCIEndPoint;
import jcu.sal.components.protocols.Protocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import v4l4j.FrameGrabber;
import v4l4j.V4L2Control;
import v4l4j.V4L4JException;

public class V4L2Protocol extends Protocol {
	
	private static Logger logger = Logger.getLogger(V4L2Protocol.class);
	public final static String V4L2PROTOCOL_TYPE = "v4l2";
	public final static String V4L2D_DEVICE_ATTRIBUTE_TAG= "deviceFile";
	public final static String WIDTH_ATTRIBUTE_TAG= "width";
	public final static String HEIGHT_ATTRIBUTE_TAG= "height";
	public final static String CHANNEL_ATTRIBUTE_TAG= "channel";
	public final static String STANDARD_ATTRIBUTE_TAG= "standard";
	
	public final static String CONTROL_VALUE_ATTRIBUTE_TAG = "ControlValue";

	
	private FrameGrabber fg = null;
	private Hashtable<String,V4L2Control> ctrls = null;
	

	public V4L2Protocol(ProtocolID i, Hashtable<String, String> c,
			Node d) throws ConfigurationException {
		super(i, V4L2PROTOCOL_TYPE , c, d);
		Slog.setupLogger(logger);
		autodetect = true;
		AUTODETECT_INTERVAL = -1; //run only once
		cmls = V4L2CML.getStore();
		supportedEndPointTypes.add(PCIEndPoint.PCIENDPOINT_TYPE);
	}

	@Override
	protected String internal_getCMLStoreKey(Sensor s)
			throws ConfigurationException {
		return s.getNativeAddress();
	}

	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		if(s.getNativeAddress().equals(V4L2CML.CCD_KEY)) return true;
		return false;
	}

	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		//we must have several config directives:
		String dev;
		int w,h,std,ch;
		try {
			dev = getConfig(V4L2D_DEVICE_ATTRIBUTE_TAG);
			w = Integer.parseInt(getConfig(WIDTH_ATTRIBUTE_TAG));
			h = Integer.parseInt(getConfig(HEIGHT_ATTRIBUTE_TAG));
			ch = Integer.parseInt(getConfig(CHANNEL_ATTRIBUTE_TAG));
			std = Integer.parseInt(getConfig(STANDARD_ATTRIBUTE_TAG));
		} catch (BadAttributeValueExpException e) {
			logger.error("Each of these parameters must be present, and some are missing: ");
			logger.error(V4L2D_DEVICE_ATTRIBUTE_TAG+"-"+WIDTH_ATTRIBUTE_TAG+"-"+HEIGHT_ATTRIBUTE_TAG+"-"+CHANNEL_ATTRIBUTE_TAG+"-"+STANDARD_ATTRIBUTE_TAG);
			throw new ConfigurationException();
		}
		
		//got all of them, create the frame grabber object
		try {
			fg = new FrameGrabber(dev, w, h, ch, std, 80);
			fg.init();
		} catch (V4L4JException e) {
			logger.error("Couldnt create/initialise FrameGrabber object");
			e.printStackTrace();
			throw new ConfigurationException();
		}
		
		//get the V4L2 controls
		V4L2Control[] v4l2c = fg.getControls();
		ctrls = new Hashtable<String, V4L2Control>();
		String name;
		for (int id = 0; id < v4l2c.length; id++) {
			name = v4l2c[id].getName();
			//add two commands to the CCD sensor for this control
			//(one to set its value, the other to get its value)
			//getValue command
			cmls.addPrivateCMLDesc(V4L2CML.CCD_KEY, GET_CONTROL_METHOD, "get"+name.replace(" ", ""), "Fetches the value of "+name, new String[0], new String[0]);
			
			//setValue command
			cmls.addPrivateCMLDesc(V4L2CML.CCD_KEY, SET_CONTROL_METHOD, "set"+name.replace(" ", ""), "Sets the value of "+name, new String[0], new String[0]);
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
		v.add(V4L2CML.CCD_KEY);
		return v; 
	}

	
	/*
	 * COMMAND HANDLING METHODS
	 */
	
	public static String GET_CONTROL_METHOD = "getControl";
	public byte[] getControl(Hashtable<String,String> c, Sensor s) throws IOException{
		V4L2Control ctrl = ctrls.get(c.get(Command.CIDATTRIBUTE_TAG));
		if(ctrl!=null) {
			try {
				return String.valueOf(ctrl.getValue()).getBytes();
			} catch (V4L4JException e) {
				logger.error("Could NOT read the value for control "+ctrl.getName());
				e.printStackTrace();
				throw new IOException();
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.get(Command.CIDATTRIBUTE_TAG));
			throw new IOException();
		}
	}

	public static String SET_CONTROL_METHOD = "setControl";
	public byte[] setControl(Hashtable<String,String> c, Sensor s) throws IOException{
		V4L2Control ctrl = ctrls.get(c.get(Command.CIDATTRIBUTE_TAG));
		if(ctrl!=null) {
			try {
				ctrl.setValue(Integer.parseInt(c.get(CONTROL_VALUE_ATTRIBUTE_TAG)));
				return null;
			} catch (V4L4JException e) {
				logger.error("Could NOT set the value for control "+ctrl.getName());
				e.printStackTrace();
				throw new IOException();
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.get(Command.CIDATTRIBUTE_TAG));
			throw new IOException();
		}
	}
	
	public static String GET_FRAME_METHOD = "getFrame";
	public byte[] getFrame(Hashtable<String,String> c, Sensor s) throws IOException{
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
}
