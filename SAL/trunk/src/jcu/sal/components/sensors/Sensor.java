/**
 * @author gilles
 */
package jcu.sal.components.sensors;

import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.components.AbstractComponent;
import jcu.sal.components.componentRemovalListener;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class Sensor extends AbstractComponent<SensorID> {
	
	private Logger logger = Logger.getLogger(Sensor.class);
	private SensorState state;
	public static final String SENSOR_TAG= "Sensor";
	public static final String SENSORSECTION_TAG= "SensorConfiguration";
	public static final String SENSORID_TAG= "sid";
	public static final String SENSORADDRESSATTRIBUTE_TAG= "Address";
	public static final String PROTOCOLATTRIBUTE_TAG = "ProtocolName";
	public static final String SENSOR_TYPE = "Sensor";
	
	/**
	 * Sensor constructor
	 * @param i the sensor ID
	 * @param c the configuration table
	 * @throws ConfigurationException if the configuration is wrong
	 */
	public Sensor(SensorID i, Hashtable<String,String> c) throws ConfigurationException {
		super();
		Slog.setupLogger(this.logger);
		id = i;
		type = SENSOR_TYPE;
		config = c;
		state = new SensorState(i);
		parseConfig();
	}

	public void setPid(ProtocolID pid) {
		assert(pid.getName().equals(config.get(PROTOCOLATTRIBUTE_TAG)));
		id.setPid(pid);
	}
	
	public String getNativeAddress() {
		return config.get(SENSORADDRESSATTRIBUTE_TAG);
	}
		
	/*
	 * Start of state management methods
	 */
	public boolean startRunCmd() {
/*		logger.debug("Running cmd on sensor " + toString());*/
		return state.runCommand();
	}
	
	public boolean disable() {
/*		logger.debug("Disabling sensor " + toString());*/
		return state.disable();
	}
	
	public boolean disconnect(){
/*		logger.debug("Disconnecting sensor " + toString());*/
		return state.disconnect();
	}
	
	public boolean reconnect() {
/*		logger.debug("Reconnecting sensor " + toString());*/
		return state.reconnect();
	}
	
	public boolean enable() {
/*		logger.debug("Enabling sensor " + toString());*/
		return state.enable();
	}
	
	public boolean finishRunCmd() {
/*		logger.debug("Finished running cmd on sensor " + toString());*/
		return state.doneCommand();
	}
	
	public long getDisconnectTimestamp(){
		return state.getDisconnectTimestamp();
	}
	/*
	 * End of state management methods
	 */
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#remove(jcu.sal.components.componentRemovalListener)
	 */
	@Override
	public void remove(componentRemovalListener c) {
		logger.debug("Registering removal of sensor " + toString());
		state.remove(c);
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#start()
	 */
	@Override
	public void start() {
		state.enable();
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		state.disable();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#toString()
	 */
	@Override
	public String toString() {
		return "Sensor " + id.getName() + " (" + getNativeAddress() +") State: "+state.toString()+" AbstractProtocol: "+id.getPIDName().toString();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return state.isStarted();
	}
	
	public boolean isDisconnected() {
		return state.isDisconnectedDisabled();
	}
	
	public String getStateToString() {
		return state.toString();
	}
}

