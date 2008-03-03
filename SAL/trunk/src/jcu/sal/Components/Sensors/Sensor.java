/**
 * @author gilles
 */
package jcu.sal.Components.Sensors;

import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.componentRemovalListener;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class Sensor extends AbstractComponent<SensorID> {
	
	private SensorState state;
	public static final String SENSOR_TAG= "Sensor";
	public static final String SENSORID_TAG= "sid";
	public static final String SENSORADDRESSATTRIBUTE_TAG= "Address";
	public static final String PROTOCOLATTRIBUTE_TAG = "ProtocolName";
	public static final String SENSOR_TYPE = "Sensor";
	
	private Logger logger = Logger.getLogger(Sensor.class);
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public Sensor(SensorID i, Hashtable<String,String> c) throws ConfigurationException {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor Sensor");
		id = i;
		type = SENSOR_TYPE;
		config = c;
		state = new SensorState(i);
		parseConfig();
	}

	public String getNativeAddress() {
		return config.get(SENSORADDRESSATTRIBUTE_TAG);
	}
	
	public String getProtocolName() {
		return config.get(PROTOCOLATTRIBUTE_TAG);
	}
	
	public boolean startRunCmd() {
		logger.debug("Running cmd on sensor " + toString());
		return state.runCommand();
	}
	
	public boolean disable() {
		logger.debug("Disabling sensor " + toString());
		return state.disable();
	}
	
	public boolean disconnect(){
		logger.debug("Disconnecting sensor " + toString());
		return state.disconnect();
	}
	
	public boolean reconnect() {
		logger.debug("Reconnecting sensor " + toString());
		return state.reconnect();
	}
	
	public boolean enable() {
		logger.debug("Enabling sensor " + toString());
		return state.enable();
	}
	
	public boolean finishRunCmd() {
		logger.debug("Finished running cmd on sensor " + toString());
		return state.doneCommand();
	}
	
	public long getDisconnectTimestamp(){
		return state.getDisconnectTimestamp();
	}

	@Override
	protected void parseConfig() throws ConfigurationException {
	}
	
	@Override
	public void remove(componentRemovalListener c) {
		logger.debug("Registering removal of sensor " + toString());
		state.stop(c);
	}
	@Override
	public void start() {
		if(state.enable()) { this.logger.debug("Starting sensor " + toString());}
		else this.logger.debug("Cant start sensor " + toString());
	}
	@Override
	public void stop() {
		state.disable();
		this.logger.debug("Sensor " + toString()+" stopped");
	}

	@Override
	public String toString() {
		return "Sensor " + id.getName() + " (" + getNativeAddress() +") State: "+state.toString()+" Protocol: "+id.getPid().toString();
	}

	@Override
	public boolean isStarted() {
		return state.isStarted();
	}
	
	public boolean isDisconnected() {
		return state.isDisconnected();
	}
	
	public String getState() {
		return state.toString();
	}
}

