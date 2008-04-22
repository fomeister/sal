package jcu.sal.Components.Protocols.v4l2;

import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.Components.EndPoints.PCIEndPoint;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Components.Protocols.ProtocolID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

public class V4L2Protocol extends Protocol {
	
	private static Logger logger = Logger.getLogger(V4L2Protocol.class);
	
	public final static String V4L2PROTOCOL_TYPE = "v4l2";
		
	static { 
		Slog.setupLogger(logger);
		SUPPORTED_ENDPOINT_TYPES.add(PCIEndPoint.PCIENDPOINT_TYPE);
		
		commands.put(new Integer(100), "getFrame");
		
		commands.put(new Integer(101), "startStream");
		commands.put(new Integer(102), "stopStream");
	}

	public V4L2Protocol(ProtocolID i, String t, Hashtable<String, String> c,
			Node d) throws ConfigurationException {
		super(i, t, c, d);
		autodetect = false;
		AUTODETECT_INTERVAL = 0;
		cmls = V4L2CML.getStore();
	}

	@Override
	protected String internal_getCMLStoreKey(Sensor s)
			throws ConfigurationException {
		return V4L2CML.V4L2CMLKey;
	}

	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean internal_probeSensor(Sensor s) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void internal_remove() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void internal_start() throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void internal_stop() {
		// TODO Auto-generated method stub

	}

}
