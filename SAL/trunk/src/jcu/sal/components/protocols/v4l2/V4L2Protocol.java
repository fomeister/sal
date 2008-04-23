package jcu.sal.components.protocols.v4l2;

import java.util.Hashtable;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

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
		
	static { 
		Slog.setupLogger(logger);
		SUPPORTED_ENDPOINT_TYPES.add(PCIEndPoint.PCIENDPOINT_TYPE);
		
		commands.put(new Integer(100), "getFrame");
		
		commands.put(new Integer(101), "startStream");
		commands.put(new Integer(102), "stopStream");
	}
	
	private FrameGrabber fg = null;
	private Hashtable<String,V4L2Control> ctrls = null;
	private String CCD_ADDRESS="CCD";
	

	public V4L2Protocol(ProtocolID i, Hashtable<String, String> c,
			Node d) throws ConfigurationException {
		super(i, V4L2PROTOCOL_TYPE , c, d);
		autodetect = true;
		AUTODETECT_INTERVAL = -1; //run only once
		cmls = V4L2CML.getStore();
		
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
		StringBuffer b = new StringBuffer();
		String name;
		for (int id = 0; id < v4l2c.length; id++) {
			//put each in the vector ctrls
			name = v4l2c[id].getName();
			ctrls.put(name, v4l2c[id]);
			//and create a CML doc for it

			cmls.addSensor(name);
//			generic 103 getValue command
			b.append("<Command name=\"getValue\">\n");
			b.append("\t<CID>103</CID>\n");
			b.append("\t<ShortDescription>Fetches the value of this control</ShortDescription>\n");
			b.append("\t<arguments count=\"0\" />\n");
			b.append("\t<returnValues count=\"1\">\n");
			b.append("\t\t<ReturnValue type=\"string\" quantity=\"none\" />\n");
			b.append("\t</returnValues>\n");
			b.append("</Command>\n");
			cmls.addCML(name, b.toString(), 103);
			b.delete(0, b.length());
			
//			generic 104 setValue command
			b.append("<Command name=\"setValue\">\n");
			b.append("\t<CID>104</CID>\n");
			b.append("\t<ShortDescription>Set the value of this control</ShortDescription>\n");
			b.append("\t<arguments count=\"0\" />\n");
			b.append("\t<returnValues count=\"1\">\n");
			b.append("\t\t<ReturnValue type=\"string\" quantity=\"none\" />\n");
			b.append("\t</returnValues>\n");
			b.append("</Command>\n");
			cmls.addCML(name, b.toString(), 103);
			b.delete(0, b.length());
		}
	}

	@Override
	protected String internal_getCMLStoreKey(Sensor s)
			throws ConfigurationException {
		return s.getNativeAddress();
	}

	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		if(s.getNativeAddress().equals(CCD_ADDRESS)) return true;
		if(ctrls.containsKey(s.getNativeAddress())) return true;
		return false;
	}

	@Override
	protected void internal_parseConfig() throws ConfigurationException {}

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
		Vector<String> s = new Vector<String>(ctrls.keySet());
		s.add(CCD_ADDRESS);
		return s;
	}

}
