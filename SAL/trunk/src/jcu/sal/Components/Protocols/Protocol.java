/**
 * @author gilles
 */
package jcu.sal.Components.Protocols;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import jcu.sal.Managers.ProtocolManager;
import jcu.sal.Managers.SensorManager;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * @author gilles
 *
 */
public abstract class Protocol extends AbstractComponent<ProtocolID> implements Runnable {

	private Logger logger = Logger.getLogger(Protocol.class);
	
	public static final String PROTOCOLTYPE_TAG = "type";
	public static final String PROTOCOLNAME_TAG = "name";
	public static final String PROTOCOL_TAG="Protocol";
	
	/**
	 * how often the autodetection process should kick in (in seconds)
	 */
	protected static int AUTODETECT_INTERVAL = 10;
	
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
	 * Can the protocol automatically detect sensor addition/removal ?
	 */
	protected boolean autodetect;
	private Thread autodetectThread;
	
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
		removed=false;
		autodetect = false;
		
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
		autodetectThread = new Thread(this);
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
	 * Associate a new sensor managed by this Protocol
	 * Also checks whether this sensor is supported, requires the access to be
	 * synchronized with respect to this protocol (synchronized(p))
	 * @param s the sensor to be added
	 * @return 
	 */
	public synchronized final void associateSensor(Sensor s) throws ConfigurationException{
		if (!removed) {
				if(isSensorSupported(s)) {
					logger.debug("About to associate sensor" + s.toString());
					synchronized(sensors) {
						sensors.put(s.getID(), s);
					}
					s.getID().setPid(this.id);
					logger.debug("Sensor associated (" + s.toString()+")");
				} else {
					logger.error("Sensor "+s.toString()+" not supported by this protocol");
					throw new ConfigurationException();
				}
		} else
			logger.error("cant associate a new sensor, the protocol is about to be removed");
	}
	
	/**
	 * Removes all sensors associated to this logical port
	 * @param i the sensorID to be removed
	 */
	public final ArrayList<Sensor> unassociateSensors() {
		ArrayList<Sensor> c;
		synchronized(sensors){
			Enumeration<SensorID> e = sensors.keys();
			c = new ArrayList<Sensor>(sensors.values());
			while(e.hasMoreElements()) {
				unassociateSensor(e.nextElement());
			}
		}
		return c;
	}

	
	/**
	 * Removes a sensor managed by this logical port
	 * @param i the sensorID to be removed
	 */
	public final boolean unassociateSensor(SensorID i) {
		this.logger.debug("About to unassociate sensor " + i.toString());
		if(sensors.containsKey(i)) {
			synchronized(sensors) {
				if(sensors.remove(i) == null) {
					logger.error("Cant unassociate sensor with key " + i.toString() +  ": No such element");
					return false;
				} else logger.debug("unassociated sensor with key " + i.toString() +  " from protocol "+toString());
			}
		} else {
			logger.error("Sensor " + i.toString()+ " doesnt exist and can NOT be unassociated");
			return false;
		}
		return true;
	}
	
	public final void dumpSensorsTable() {
		this.logger.debug("current sensors table contents:" );
		synchronized (this) {
			Enumeration<SensorID> keys = sensors.keys();
			Collection<Sensor> cvalues = sensors.values();
			Iterator<Sensor> iter = cvalues.iterator();
			while ( keys.hasMoreElements() &&  iter.hasNext())
			   logger.debug("key: " + keys.nextElement().toString() + " - "+iter.next().toString());			
		}
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
			removed=true;
			if(started)
				stop();
			ProtocolManager.getProcotolManager().removeSensors(this);
			internal_remove();
			EndPointManager.getEndPointManager().destroyComponent(ep.getID());
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
			this.logger.debug("Probing sensors for Protocol " + toString());
			synchronized(sensors) {
				Iterator<Sensor> i = sensors.values().iterator();
				while(i.hasNext())
					probeSensor(i.next());
			}
			if(!started)
				started=true;
		}
		this.logger.debug("protocol "+type+" started");
		//Start the sensor monitoring thread
	
		if(AUTODETECT_INTERVAL!=0) {
			this.logger.debug("Starting autodetect thread");
			autodetectThread.start();
		} else {
			logger.error("Autodetect interval set to 0 in the protocol config.");
			logger.error("Disabling sensor autodetection");
		}
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#stop()
	 */
	public final void stop() {
		synchronized (this) {
			logger.debug("stopping "+type+" Protocol");
			if(autodetectThread.isAlive()) {
				logger.debug("stopping autodetect thread ...");
				autodetectThread.interrupt();
				try { autodetectThread.join();}
				catch (InterruptedException e) {
					logger.error("interrupted while waiting for autodetect thread to finish");
				}
				logger.debug("autodetect thread stopped");
			}
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
	 * Check whether a sensor is currently plugged-in/reachable/readable 
	 * and set its state accordingly (protocol specific)
	 * @param sensor the sensor to be probed
	 */
	public final boolean probeSensor(Sensor sensor) {
		return internal_probeSensor(sensor);
	}
	
	/**
	 * Check whether a sensor is supported by this protocol
	 * @param sensor the sensor to be probed
	 */
	public final boolean isSensorSupported(Sensor s) {
		if(s.getProtocolName().equals(id.getName())) {
			return internal_isSensorSupported(s);
		}
		else {
			logger.debug("The protocol associated with this sensor doesnt match this protocol's name. Sensor not supported");
			return false;
		}
	}
	
	/**
	 * Implements the autodetection thread
	 */
	public void run() {
		Vector<String> detected;
		Sensor stmp;
		ArrayList<Sensor> current;
		Iterator<Sensor> iter;
		try {
			logger.debug("Autodetect thread started");
			while(!Thread.interrupted() && ((detected = detectConnectedSensors())!=null)) {
				//for each sensor in current
				synchronized(sensors) {
					current = new ArrayList<Sensor>(sensors.values());
					iter = current.iterator();
					while(iter.hasNext()) {
						//if that sensor is in detected also, we remove both
						stmp = iter.next();
						if(detected.contains(stmp.getNativeAddress())) {
	//						logger.debug("sensor "+stmp.toString()+" found in both current & detected, nothing to do");
							if(stmp.isDisconnected())
								stmp.reconnect();
							detected.remove(stmp.getNativeAddress());
							iter.remove();
						} else if (stmp.isDisconnected()) {
	//						logger.debug("sensor "+stmp.toString()+" FOUND in current and NOT FOUND in detected and already DISCONNECTED");
							detected.remove(stmp.getNativeAddress());
							iter.remove();
						} //else {
/*							logger.debug("detected contains " + stmp.getNativeAddress() + " ? " +detected.contains(stmp.getNativeAddress()));
							logger.debug("current sensor state: "+stmp.getState());
							logger.debug("Sensor " + stmp.getNativeAddress()+" only in current");
						}
*/					}
				}
				
				//now we re left with newly-connected sensors in detected and
				//removed sensors in current
				Iterator<String> it = detected.iterator();
				String t;
				while(it.hasNext()) {
						try {
							t = generateSensorConfig(it.next());
							Sensor s = ProtocolManager.getProcotolManager().createSensorFromPartialSML(t);
							ProtocolManager.getProcotolManager().associateSensor(s);
							probeSensor(s);
							logger.debug("autodetected sensor added");
						} catch (ConfigurationException e1) {
							logger.error("couldnt create the autodetected sensor from its autogenerated XML config:");
						}

				}					
				
				iter = current.iterator();
				while(iter.hasNext()) {
						stmp = iter.next();
						logger.debug("Newly removed sensor: " + stmp.toString());
						stmp.disconnect();
				}
				dumpSensorsTable();

				Thread.sleep(Long.valueOf(AUTODETECT_INTERVAL*1000));
			}
		} catch (InterruptedException e) {}
		logger.debug("Autodetect thread exiting");
	}
	
	/**
	 * Check whether a sensor is supported by this protocol (protocol specific)
	 * @param s the sensor to be probed
	 */
	protected abstract boolean internal_isSensorSupported(Sensor s);
	
	/**
	 * Check whether a sensor is currently plugged-in/reachable/readable 
	 * and set its state accordingly (protocol specific). The sensor must NOT
	 * be removed !
	 * @param s the sensor to be probed
	 */
	protected abstract boolean internal_probeSensor(Sensor s);

	/**
	 * Stops the protocol
	 */
	protected abstract void internal_stop();
	
	/**
	 * Starts the protocol. When this method returns, the protocol must be ready to handle
	 * requests to access (commands()), probe (probeSensor()) sensors without any further configuration
	 * internal_start should not call probeSensor
	 */
	protected abstract void internal_start() throws ConfigurationException;
	
	/**
	 * Prepare the subclass to be removed 
	 */
	protected abstract void internal_remove();
	
	/**
	 * Parse the configuration of the protocol itself
	 */
	protected abstract void internal_parseConfig() throws ConfigurationException;
	
	/**
	 * this method should be overriden by protocols which provide sensor autodetection
	 * and return a Vector of native address (strings) of currently connected/visible sensors 
	 */
	protected Vector<String> detectConnectedSensors() {
		logger.error("calling the wrong method");
		return null;
	}
	
	/**
	 * this method should be overriden by protocols which provide sensor autodetection
	 * and return an XML document for a newly detected sensor given its native address 
	 */
	protected final String generateSensorConfig(String nativeAddress){
		StringBuffer xml = new StringBuffer();

		xml.append("<Sensor sid=\""+ SensorManager.SENSORID_MARKER + "\">\n");
		xml.append("\t<parameters>\n");
		xml.append("\t\t<Param name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\" value=\""+this.id.getName()+"\" />\n");
		xml.append("\t\t<Param name=\""+Sensor.SENSORADDRESSATTRIBUTE_TAG+"\" value=\""+nativeAddress+"\" />\n");
		xml.append("\t</parameters>\n");
		xml.append("</Sensor>\n");
			
		return xml.toString();
	}
	
	/**
	 * Is the protocol started ? 
	 */
	public final boolean isStarted() {
		return started;
	}

}
