/**
 * 
 */
package jcu.sal.components.protocols.dummy;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.EndPoints.FSEndPoint;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;

/**
 * @author gilles
 *
 */
public class DummyProtocol extends AbstractProtocol {
	public static final String PROTOCOL_TYPE = "DUMMY";
	public static final int NB_DUMMY_SENSORS=100;
	private byte[] reading= new String("439041101").getBytes();

	/**
	 * @param i 
	 * @param t 
	 * @param c
	 * @param d
	 * @throws ConfigurationException 
	 */
	public DummyProtocol(ProtocolID i, ProtocolConfiguration c) throws ConfigurationException {
		super(i, PROTOCOL_TYPE, c);		

		//Add to the list of supported EndPoint IDs
		supportedEndPointTypes.add(FSEndPoint.FSENDPOINT_TYPE);
		//runs auto detect thread only once if it is going to run
		autoDetectionInterval = -1;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_getCMLStoreKey(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected String internal_getCMLStoreKey(Sensor s)
			throws ConfigurationException {
		return CMLDescriptionStore.DUMMY_KEY;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_isSensorSupported(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		return true;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_parseConfig()
	 */
	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		try { autoDetectionInterval = (getParameter("AutoDetect").equals("1") || getParameter("AutoDetect").equalsIgnoreCase("true")) ? -1 : 0;}
		catch (BadAttributeValueExpException e) {autoDetectionInterval=0;}
		cmls = CMLDescriptionStore.getStore();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_probeSensor(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_probeSensor(Sensor s) {
		s.enable();
		return true;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_remove()
	 */
	@Override
	protected void internal_remove() {}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_start()
	 */
	@Override
	protected void internal_start() throws ConfigurationException {}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_stop()
	 */
	@Override
	protected void internal_stop() {}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#detectConnectedSensors()
	 */
	@Override
	protected List<String> detectConnectedSensors() {
		List<String> detected = new Vector<String>();
		for(int i=0;i<NB_DUMMY_SENSORS;i++)
			detected.add("DUMMY_"+i);		
		return detected;
	}
	
	/*
	 * Command handling methods
	 */
	// TODO create an exception class for this instead of Exception
	public static String GET_READING_METHOD = "getReading";
	public  byte[] getReading(Command c, Sensor s) throws IOException{
		return reading;
	}

}
