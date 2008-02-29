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
import jcu.sal.Components.componentRemovalListener;
import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.Managers.EndPointManager;
import jcu.sal.Managers.SensorManager;
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
	
	/**
	 * A list of endpoints types supported by this protocol
	 */
	public static final Vector<String> SUPPORTED_ENDPOINT_TYPES = new Vector<String>();
	
	/**
	 * A table mapping command Ids to the name of a method to be exectuted when this command arrives
	 */
	protected static Hashtable<Integer, String> commands = new Hashtable<Integer, String>();
	
	/**
	 * A table of sensors managed by this protocol
	 */
	protected Hashtable<SensorID, Sensor> sensors;
	
	/**
	 * The endpoint associated with this protocol
	 */
	protected EndPoint ep;
	
	/**
	 * Is this protocol started ?
	 */
	private boolean started;
	
	/**
	 * Construct a new protocol gien its ID, type, configuration directives and an XML node
	 * containing the associated endpoint configuration
	 * @param i the protocol identifier
	 * @paran t the type of the protocol
	 * @param c the configuration directives
	 * @param d the XML node containing the associated endpoint configuration  
	 * @throws ConfigurationException 
	 */
	public Protocol(ProtocolID i, String t, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super();
		Slog.setupLogger(logger);
		started=false;
		
		/* construct the endpoint first */
		ep = EndPointManager.getEndPointManager().createComponent(d);
		if(ep==null)
			throw new ConfigurationException("Couldnt create the EdnPoint");

		/* Sets the PID field of the EndPointID */
		ep.getID().setPid(i);
		
		/* init the rest of the fields */
		id = i;
		type = t;
		config = c;
		sensors = new Hashtable<SensorID, Sensor>();
		
		/* parse the configuration */
		try {
			parseConfig();
		} catch (ConfigurationException e) {
			logger.error("Error creating the protocol, destroying the endpoint");
			EndPointManager.getEndPointManager().destroyComponent(ep.getID());
			throw e;
		}
	}
	
	/**
	 * Adds a new sensor managed by this Protocol
	 * Also checks whether this sensor is connected
	 * @param s the sensor to be added
	 */
	public final void addSensor(Sensor s) throws ConfigurationException{
		if (!started) {
				if(isSensorSupported(s)) {
					this.logger.debug("About to add sensor" + s.toString());
					//s.start();
					sensors.put(s.getID(), s);
					s.getID().setPid(this.id);
				} else {
					logger.error("Sensor "+s.toString()+" not supported by this protocol");
					throw new ConfigurationException();
				}
		} else
			logger.error("NOT IMPLEMENTED: ADD A SENSOR WITH PROTOCOL STARTED");
	}
	
	/**
	 * Removes a sensor managed by this logical port
	 * @param i the sensorID to be removed
	 */
	public final void removeSensor(SensorID i) {
		this.logger.debug("About to remove sensor " + i.toString());
		if(sensors.containsKey(i)) {
			if(sensors.remove(i) == null)
				this.logger.error("Cant remove sensor with key " + i.toString() +  ": No such element");
			else {
				SensorManager.getSensorManager().destroyComponent(i);
				this.logger.debug("Sensor " + i.toString()+ " Removed");
			}
		} else
			this.logger.error("Sensor " + i.toString()+ " doesnt exist and can NOT be removed");
	}
	
	/**
	 * Removes all sensors belonging to this protocol
	 *
	 */
	public final void removeAllSensors(){
		Enumeration<SensorID> i = sensors.keys();
		while (i.hasMoreElements())
			removeSensor(i.nextElement());
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
	 * returns a textual representation of a Protocol's instance
	 * @return the textual representation of the Protocol's instance
	 */
	public final String toString() {
		return "Protocol "+id.getName()+"("+type+"), EndPoint: " + ep.toString();

	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#remove()
	 */
	public final void remove(componentRemovalListener c) {
		synchronized (this) {
			removeAllSensors();
			if(started)
				stop();
			internal_remove();
			EndPointManager.getEndPointManager().destroyComponent(ep);
			this.logger.debug("protocol " + type +" removed");
		}
		c.componentRemovable(id);
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	protected final void parseConfig() throws ConfigurationException {
		logger.debug("Parsing our configuration");
		logger.debug("1st, Check the EndPoint");
		if(!isEPTypeSupported(ep.getType())) {
			logger.error("This Protocol has been setup with the wrong enpoint: got endpoint type: " +ep.getType()+", expected: ");
			Iterator<String> iter = SUPPORTED_ENDPOINT_TYPES.iterator();
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
		synchronized (this) {
			this.logger.debug("starting "+type+" Protocol");
			if(!ep.isStarted())
				ep.start();
			internal_start();
			Iterator<Sensor> i = sensors.values().iterator();
			while(i.hasNext())
				probeSensor(i.next());
			if(!started)
				started=true;
			this.logger.debug("protocol "+type+" started");
		}
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#stop()
	 */
	public final void stop() {
		synchronized (this) {
			this.logger.debug("stopping "+type+" Protocol");
			if(started) {
				internal_stop();
				if(ep.isStarted())
					ep.stop();
			}
			this.logger.debug("protocol "+type+" stopped");
		}
	}

	/**
	 * Sends a command to a sensor
	 * @param c the command
	 * @param sid the sensorID
	 * @return the result
	 * @throws BadAttributeValueExpException 
	 */
	public String execute(Command c, SensorID sid) throws BadAttributeValueExpException {
		String ret_val = null;
		logger.debug("Received command :");
		Sensor s = sensors.get(sid);
		if(started) {
			//Check if we have the sensor
			if (s!=null) {
				//Check if it accepts command
				if(s.startRunCmd()) {
					try {
						Class<?>[] params = {Hashtable.class,Sensor.class};
						Method m = this.getClass().getDeclaredMethod(commands.get(c.getCID()), params);
						logger.debug("running method: "+ m.getName() );
						ret_val = (String) m.invoke(this,c.getParameters(), s);
					} catch (SecurityException e) {
						logger.error("Not allowed to execute the methods matching the command");
						s.finishRunCmd();
						throw new BadAttributeValueExpException("");
					} catch (NoSuchMethodException e) {
						logger.error("Could NOT find the method matching the command");
						s.finishRunCmd();
						throw new BadAttributeValueExpException("");
					} catch (BadAttributeValueExpException e) {
						logger.error("Could NOT parse the command");
						s.finishRunCmd();
						throw e;
					} catch (InvocationTargetException e) {
						logger.error("The command returned an exception:" + e.getMessage());
						e.printStackTrace();
					} catch (Exception e) {
						logger.error("Could NOT run the command (error with invoke() )");
						e.printStackTrace();
					}
					s.finishRunCmd();
				} else {
					logger.error("Sensor not available to run the command");
					//TODO throw an exception here
				}
			} else {
				logger.error("Sensor not present.Cannot execute the command");
				//TODO throw an exception here
			}
		} else
			logger.error("protocol not started.Cannot execute the command");
			//TODO throw an exception here
		return ret_val;
	}
	
	/**
	 * Check whether a endPoint type is supported by this protocol
	 * @param String the EndPoint type
	 */
	public final boolean isEPTypeSupported(String type) {
		return SUPPORTED_ENDPOINT_TYPES.contains(type);
	}
	
	/**
	 * Check whether a sensor is supported by this protocol
	 * @param sensor the sensor to be probed
	 */
	public abstract boolean isSensorSupported(Sensor sensor);
	
	/**
	 * Check whether a sensor is currently plugged-in/reachable/readable, and set its state accordingly.
	 * @param sensor the sensor to be probed
	 */
	public abstract boolean probeSensor(Sensor sensor);

	/**
	 * Stops the protocol
	 */
	protected abstract void internal_stop();
	
	/**
	 * Starts the protocol. When this method returns, the protocol must be ready to handle
	 * requests to access (commands()), probe (probeSensor()) sensors without any further configuration
	 * internal_start should not call probeSensor
	 */
	protected abstract void internal_start();
	
	/**
	 * Prepare the subclass to be removed 
	 */
	protected abstract void internal_remove();
	
	/**
	 * Parse the configuration of the protocol itself
	 */
	protected abstract void internal_parseConfig() throws ConfigurationException;
	
	/**
	 * Is the protocol started ? 
	 */
	public final boolean isStarted() {
		return started;
	}

}
