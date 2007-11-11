/**
 * @author gilles
 */
package jcu.sal.Components.Protocols;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
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
import org.w3c.dom.Node;


/**
 * @author gilles
 *
 */
public abstract class Protocol extends AbstractComponent<ProtocolID> {

	private Logger logger = Logger.getLogger(Protocol.class);
	
	public static final String PROTOCOLTYPE_TAG = "type";
	public static final String PROTOCOLNAME_TAG = "name";
	public static final String PROTOCOL_TAG="Protocol";
	public final static Vector<String> SUPPORTED_ENDPOINTS = new Vector<String>();
	
	protected static Hashtable<Integer, String> commands = new Hashtable<Integer, String>();;
	protected Hashtable<SensorID, Sensor> sensors;
	protected EndPoint ep; 
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public Protocol(ProtocolID i, String t, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super();
		Slog.setupLogger(logger);		
		id = i;
		type = t;
		config = c;
		sensors = new Hashtable<SensorID, Sensor>();
		ep = EndPointManager.getEndPointManager().createComponent(d);
		if(ep==null)
			throw new ConfigurationException("Couldnt create the EdnPoint");
		else {
			try {
				parseConfig();
			} catch (ConfigurationException e) {
				logger.error("Error creating the protocol, destroying the endpoint");
				EndPointManager.getEndPointManager().destroyComponent(ep.getID());
				throw e;
			}
		}
	}
	
	/**
	 * Adds a new sensor managed by this logical port
	 * @param s the sensor to be added
	 */
	public final void addSensor(Sensor s) {
		if (!started) {
				this.logger.debug("About to add sensor" + s.toString());
				s.start();
				sensors.put(s.getID(), s);
		} else
			logger.error("NOT IMPLEMENTED: ADD A SENSOR WITH PROTOCOL STARTED");
		
	}
	
	/**
	 * Check if we have a sensor in the table
	 * @param s the sensor to be checked
	 */
	public final boolean hasSensor(SensorID sid) {
		return sensors.containsKey(sid);
	}
	
	/**
	 * Check if we have a sensor in the table
	 * @param s the sensor to be checked
	 */
	public final Sensor getSensor(SensorID sid) {
		return sensors.get(sid);
	}
	
	/**
	 * Returns all managed sensors
	 * @param s the sensor to be checked
	 */
	public final Collection<Sensor> getSensors() {
		return sensors.values();
	}
	
	/**
	 * Removes a sensor managed by this logical port
	 * @param i the sensorID to be removed
	 */
	public final void removeSensor(SensorID i) {
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
	
	
	public final void dumpSensorsTable() {
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
	public final String toString() {
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
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	protected final void parseConfig() throws ConfigurationException {
		logger.debug("Parsing our configuration");
		logger.debug("1st, Check the EndPoint");
		if(!ep.isConfigured() || !SUPPORTED_ENDPOINTS.contains(ep.getType())) {
			logger.error("This Protocol has been setup with the wrong enpoint: got endpoint type: " +ep.getType()+", expected: ");
			Iterator<String> iter = SUPPORTED_ENDPOINTS.iterator();
			while(iter.hasNext())
				logger.error(iter.next());
			throw new ConfigurationException("Wrong Endpoint type");
		}
		logger.debug("EndPoint OK");	
		logger.debug("2nd Check " + type +" software");
		internal_parseConfig();
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
		probeSensors();
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
	 * @param sid the sensorID
	 * @return the result
	 * @throws BadAttributeValueExpException 
	 */
	public String execute(Command c, SensorID sid) throws BadAttributeValueExpException {
		String s = null;
		logger.debug("Received command");
		c.dumpCommand();
		
		//Check if we have the sensor
		if (hasSensor(sid)) {
			if(getSensor(sid).isAvailable()) {
				try {
					Class<?>[] params = {Hashtable.class,Sensor.class};
					Method m = this.getClass().getDeclaredMethod(commands.get(c.getCID()), params);
					logger.debug("running method: "+ m.getName() );
					s = (String) m.invoke(this,c.getParameters(), getSensor(sid));
				} catch (SecurityException e) {
					logger.error("Not allowed to execute the methods matching the command");
					throw new BadAttributeValueExpException("");
				} catch (NoSuchMethodException e) {
					logger.error("Could NOT find the method matching the command");
					throw new BadAttributeValueExpException("");
				} catch (BadAttributeValueExpException e) {
					logger.error("Could NOT parse the command");
					throw e;
				} catch (InvocationTargetException e) {
					logger.error("The command returned an exception:" + e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					logger.error("Could NOT run the command (error with invoke() )");
					e.printStackTrace();
				} 
			} else {
				logger.error("Sensor not available to run the command");
				//TODO throw an exception here
			}
		} else {
			logger.error("Sensor not present.Cannot execute the command");
			//TODO throw an exception here
		}
		return s;
	}
	
	/**
	 * Check whether all the sensors are connected, and change their status accordingly
	 * @throws ConfigurationException 
	 */
	public abstract void probeSensors() throws ConfigurationException;

	/**
	 * Get the subclass to get ready to be removed
	 */
	protected void internal_stop() {}
	
	/**
	 * Starts the subclass 
	 */
	protected void internal_start() {}
	
	/**
	 * Prepare the subclass to be removed 
	 */
	protected void internal_remove() {}
	
	/**
	 * Parse the configuration of the protocol itself
	 */
	protected void internal_parseConfig() throws ConfigurationException {}

}
