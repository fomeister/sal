/**
 * @author gilles
 */
package jcu.sal.Components.Protocols;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.Command;
import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.Managers.EndPointManager;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;


/**
 * @author gilles
 *
 */
public abstract class Protocol extends AbstractComponent<ProtocolID> {

	private Logger logger = Logger.getLogger(Protocol.class);
	
	public static final String PROTOCOLPARAMNAME_TAG = "name";
	public static final String PROTOCOLPARAM_TAG = "Param";
	public static final String PROTOCOLTYPE_TAG = "type";
	public static final String PROTOCOLNAME_TAG = "name";
	public static final String PROTOCOL_TAG="Protocol";
	public final static Vector<String> OWFS_SUPPORTED_ENDPOINTS;
	static {
		OWFS_SUPPORTED_ENDPOINTS = new Vector<String>();
	}
	
	protected Hashtable<SensorID, Sensor> sensors;
	protected EndPoint ep; 
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public Protocol(ProtocolID i, String t, Hashtable<String,String> c, Document d) throws ConfigurationException {
		super();
		Slog.setupLogger(logger);		
		id = i;
		type = t;
		config = c;
		sensors = new Hashtable<SensorID, Sensor>();
		ep = EndPointManager.getEndPointManager().createComponent(d);
		if(ep==null)
			throw new ConfigurationException("Couldnt create the EdnPoint");
		else
			parseConfig();
	}
	
	/**
	 * Adds a new sensor managed by this logical port
	 * @param s the sensor to be added
	 */
	public void addSensor(Sensor s) {
		this.logger.debug("About to add sensor" + s.toString());
		s.start();
		sensors.put(s.getID(), s);
		
	}
	
	/**
	 * Removes a sensor managed by this logical port
	 * @param i the sensorID to be removed
	 */
	public void removeSensor(SensorID i) {
		this.logger.debug("About to remove sensor " + i.toString());
		if(sensors.containsKey(i)) {
			dumpSensorsTable();
			sensors.get(i).remove();
			if(sensors.remove(i) == null)
				this.logger.error("Cant remove sensor with key " + i.toString() +  ": No such element");
			else
				this.logger.debug("Sensor " + i.toString()+ " Removed");
		} else
			this.logger.error("Sensor " + i.toString()+ " doesnt exist and can NOT be removed");
	}
	
	
	public void dumpSensorsTable() {
		this.logger.debug("current sensors table contents:" );
		Enumeration<SensorID> keys = sensors.keys();
		Collection<Sensor> cvalues = sensors.values();
		Iterator<Sensor> iter = cvalues.iterator();
		while ( keys.hasMoreElements() &&  iter.hasNext())
		   this.logger.debug("key: " + keys.nextElement().toString() + " - "+iter.next().toString());
	}
	
	/**
	 * returns a textual representation of a End Point's instance
	 * @return the textual representation of the Logical Port's instance
	 */
	public String toString() {
		return "Protocol "+id.getName()+"("+type+"), EndPoint: " + ep.toString();

	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#remove()
	 */
	public final void remove() {
		if(started)
			stop();
		internal_remove();
		ep.remove();
		this.logger.debug("protocol removed");
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#start()
	 */
	public final void start() throws ConfigurationException{
		this.logger.debug("starting Procol");
		if(!configured)
			parseConfig();
		if(!ep.isStarted())
			ep.start();
		internal_start();
		started = true;
		this.logger.debug("protocol started");
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#stop()
	 */
	public final void stop() {
		this.logger.debug("stopping Procol");
		if(started) {
			internal_stop();
			if(ep.isStarted())
				ep.stop();
			started = false;
		}
		this.logger.debug("protocol stopped");
	}

	/**
	 * Sends a command to a sensor
	 * @param c the command
	 * @param s the sensor
	 * @return the result
	 */
	public abstract String execute(Command c, Sensor s);
	
	/**
	 * Check whether all the sensors are connected, and change their status accordingly
	 */
	public abstract void probeSensors();

	/**
	 * Get the subclass to get ready to be removed
	 */
	protected abstract void internal_stop();
	
	/**
	 * Starts the subclass 
	 */
	protected abstract void internal_start();
	
	/**
	 * Prepare the subclass to be removed 
	 */
	protected abstract void internal_remove();

}
