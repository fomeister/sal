/**
 * @author gilles
 */
package jcu.sal.Components.Sensors;

import java.util.Hashtable;

import jcu.sal.Components.AbstractComponent;
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
	 * 
	 */
	public Sensor(SensorID i, String t, Hashtable<String,String> c) {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor Sensor");
		id = i;
		type = t;
		config = c;
		state = new SensorState();
		parseConfig();
	}
	
	public SensorState getSensorState() {
		synchronized(this) {
			return state;
		}
	}
	
	public String getNativeAddress() {
		return config.get(SENSORADDRESSATTRIBUTE_TAG);
	}
	
	public boolean isAvailable() {
		synchronized(this) {
			return state.isAvailable();
		}
	}
	
	public void setAvailable() {
		synchronized(this) {
			state.setStateAvailable();
		}
	}
	
	public void setUseState(int s) {
		synchronized(this) {
			state.setState(s,SensorState.STATE_UNCHANGED,SensorState.STATE_UNCHANGED);
		}
	}
	
	public void setConfigState(int s) {
		synchronized(this) {
			state.setState(SensorState.STATE_UNCHANGED,s,SensorState.STATE_UNCHANGED);
		}
	}
	
	public void setErrorState(int s) {
		synchronized(this) {
			state.setState(SensorState.STATE_UNCHANGED,SensorState.STATE_UNCHANGED,s);
		}
	}
	
	public int getUseState() {
		synchronized(this) {
			return state.getUseState();
		}
	}
		
	public int getConfigState() {
		synchronized(this) {
			return state.getConfigState();
		}
	}
	
	public int getErrorState() {
		synchronized(this) {
			return state.getErrorState();
		}
	}
	
	@Override
	protected void parseConfig() {
		setAvailable();
	}
	
	@Override
	public void remove() {
		this.logger.debug("Removing sensor " + toString());
	}
	@Override
	public void start() {
		this.logger.debug("Starting sensor " + toString());
	}
	@Override
	public void stop() {
		this.logger.debug("Stopping sensor " + toString());
	}

	@Override
	public String toString() {
		return "Sensor " + id.getName() + " (" + getNativeAddress() +")";
	}
}

