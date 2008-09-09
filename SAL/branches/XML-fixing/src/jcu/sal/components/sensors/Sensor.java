/**
 * @author gilles
 */
package jcu.sal.components.sensors;

import javax.naming.ConfigurationException;

import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.components.AbstractComponent;
import jcu.sal.components.componentRemovalListener;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class Sensor extends AbstractComponent<SensorID, SMLDescription> {
	
	private static Logger logger = Logger.getLogger(Sensor.class);
	static {Slog.setupLogger(logger);}
	private SensorState state;
	
	/**
	 * Sensor constructor
	 * @param i the sensor ID
	 * @param c the configuration table
	 * @throws ConfigurationException if the configuration is wrong
	 */
	public Sensor(SensorID i, SMLDescription s) throws ConfigurationException {
		super(s,i);
		state = new SensorState(i);
	}

	public void setPid(ProtocolID pid) throws ConfigurationException {
		String p = config.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		if(!pid.getName().equals(p)) {
			logger.error("Trying to associate with  protocol '"+pid.getName()+"' but sensor config expected '"+p+"'");
			throw new ConfigurationException("cant associate with this protocol ("+pid.getName()+")");
		}		
		id.setPid(pid);
	}
	
	public String getNativeAddress() {
		try {
			return config.getParameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE);
		} catch (ConfigurationException e) {
			logger.error("Shouldnt be here, we have a sensor ("+id.toString()+") without a native address");
			return null;
		}
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
		//logger.debug("Registering removal of sensor " + toString());
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

