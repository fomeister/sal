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
	public static final String SENSORADDRESSNODE_TAG= "Address";
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
		return id.getNativeAddress();
	}
	
	public boolean isAvailable() {
		synchronized(this) {
			return state.isAvailable();
		}
	}
	
	@Override
	protected void parseConfig() {
		state.setState(SensorState.USESTATE_ENABLED_IDLE,SensorState.CONFIGSTATE_CONFIGURED, SensorState.ERRORSTATE_PRESENT);
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

