/**
 * @author gilles
 */
package jcu.sal.components.protocols;

import java.io.NotActiveException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.components.AbstractComponent;
import jcu.sal.components.componentRemovalListener;
import jcu.sal.components.EndPoints.DeviceListener;
import jcu.sal.components.EndPoints.EndPoint;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.components.sensors.SensorID;
import jcu.sal.events.EventDispatcher;
import jcu.sal.managers.EndPointManager;
import jcu.sal.managers.SensorManager;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * @author gilles
 *
 */
public abstract class AbstractProtocol extends AbstractComponent<ProtocolID>  implements DeviceListener {

	private static Logger logger = Logger.getLogger(AbstractProtocol.class);
	
	public static final String PROTOCOLTYPE_TAG = "type";
	public static final String PROTOCOLNAME_TAG = "name";
	public static final String PROTOCOL_TAG="Protocol";
	public static final String PROTOCOLSECTION_TAG="protocols";

	
	/**
	 * A list of endpoints types supported by this protocol
	 */
	public final Vector<String> supportedEndPointTypes;
	

	/**
	 * A table of sensors managed by this protocol
	 */
	protected Hashtable<SensorID, Sensor> sensors;
	
	/**
	 * The endpoint associated with this protocol
	 */
	protected EndPoint ep;
	private Node epConfig;
	
	/**
	 * Can there be multiple instances of this protocol at once ? Must be set by the subclass
	 */
	protected boolean multipleInstances;
	
	/**
	 * The CML store associated with this protocol. This field must be instanciated by the subclass
	 */
	protected AbstractStore cmls=null;
	
	/**
	 * Is this protocol started ?
	 */
	private AtomicBoolean started;
	
	
	/**
	 * Can the protocol automatically detect sensor addition/removal ?
	 * how often the autodetection process should kick in (in milliseconds)
	 * if this value is negative, then the autodetection thread is executed only once
	 * if it is set to 0, the autodetection thread never starts
	 * A positive value specifies how often (in milliseconds) the autodetect thread will try to detect
	 * what s cuccrently connected
	 */
	protected int autoDetectionInterval = 2 * 1000;
	private Autodetection autodetectThread = null;
	
	/**
	 * The subclass stores here the IDs of devices the EndPoint should watch for
	 * Example: OWFS stores the USB IDs of DS2490 so UsbEndPoint can notify OWFS when new ds2490
	 * are connected  
	 */
	protected String[] epIds = null;
	
	protected AtomicBoolean removed;
	
	/**
	 * Construct a new protocol gien its ID, type, configuration directives and an XML node
	 * containing the associated endpoint configuration
	 * @param i the protocol identifier
	 * @paran t the type of the protocol
	 * @param c the configuration directives
	 * @param d the XML node containing the associated endpoint configuration  
	 */
	public AbstractProtocol(ProtocolID i, String t, Hashtable<String,String> c, Node d){
		super();
		Slog.setupLogger(logger);
		started= new AtomicBoolean(false);
		removed=new AtomicBoolean(false);

		
		/* init the rest of the fields */
		id = i;
		type = t;
		config = c;
		epConfig = d;
		supportedEndPointTypes = new Vector<String>();
		sensors = new Hashtable<SensorID, Sensor>();
		multipleInstances = true;
		
	
		/* Registers with the EventHandler */
		EventDispatcher.getInstance().addProducer(id.getName());
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	public final void parseConfig() throws ConfigurationException {
		logger.debug("Parsing our configuration");
		logger.debug("Build the EndPoint");
		/* check if we have already instancicated CMl store (we shouldnt, CML store instanciation msut be done in
		 * (internal_)parse_config() 
		 */
		if(cmls!=null)
			throw new ConfigurationException();
		
		/* construct the endpoint  */
		ep = EndPointManager.getEndPointManager().createComponent(epConfig);
		if(ep==null)
			throw new ConfigurationException("Couldnt create the EndPoint");
		
		if(!isEPTypeSupported(ep.getType())) {
			logger.error("This AbstractProtocol has been setup with the wrong enpoint: got endpoint type: " +ep.getType()+", expected: ");
			Iterator<String> iter = supportedEndPointTypes.iterator();
			while(iter.hasNext())
				logger.error(iter.next());
			EndPointManager.getEndPointManager().destroyComponent(ep.getID());
			throw new ConfigurationException("Wrong Endpoint type");
		}		

		/* Sets the PID field of the EndPointID */
		ep.setPid(id);
		
		logger.debug("EndPoint OK");
		logger.debug("Check " + type +" software");
		try { internal_parseConfig(); }
		catch (ConfigurationException e) {
			logger.error("Error configuring the protocol, destroying the endpoint");
			EndPointManager.getEndPointManager().destroyComponent(ep.getID());
			throw e;
		} 
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#start()
	 */
	public final void start() throws ConfigurationException{
		if(started.compareAndSet(false, true)) {
			/* Start the EndPoint */
			ep.start();
			/* Start the ourselves */
			internal_start();
			/* Probe associted sensors */
			logger.debug("Probing associated sensors for AbstractProtocol " + toString());
			synchronized(sensors) {
				Iterator<Sensor> i = sensors.values().iterator();
				while(i.hasNext())
					probeSensor(i.next());
			}
			
			logger.debug("protocol "+type+" started");
			
			/* if we need the endpoint to report device plugging / unplugging events
			 * do it now
			 */
			if(epIds!=null) {
				int nb;
				//check if the devices we re interested in are connected
				//Initial run
				for (String ids : epIds) {
					nb=ep.getConnectedDeviceNum(ids);
					logger.debug("devices with "+ids+": "+nb);
					if(nb!=0)
						adapterChange(nb, ids);
				}
				/* Register ourselves with EndPoint for subsequent notifications */ 
				try { ep.registerDeviceListener(this, epIds); }
				catch (UnsupportedOperationException e) { logger.debug("Autodetect not supported by the EndPoint");}
			} else
				/* Start Autodetect thread */
				startAutodetectThread();
		}
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#stop()
	 */
	public final void stop() {
		Sensor s;
		if(started.compareAndSet(true, false)) {
			/* Unregister from EP */
			if(epIds!=null) {
				try {ep.unregisterDeviceListener(this);}
				catch (UnsupportedOperationException e) {}
			}
			
			/* Stop Autodetect thread because we already unregistered from the EP 
			 * newly connected adapters wont re-start the autodetect thread
			 */
			stopAutodetectThread();
			
			/* disable all associated sensors 
			 * IN BETWEEN commands (synchronized (s))
			 */
			synchronized(sensors) {
				Iterator<Sensor> i = sensors.values().iterator();
				while(i.hasNext()) {
					s = i.next();
					synchronized(s){s.disable();}
				}
			}
			/*At this stage, we are sure no commands are being run or will be on our sensors */
			
			/* Stop ourselves */
			internal_stop();
			/* Stop EP */
			ep.stop();
			
			logger.debug("protocol "+type+" stopped");
		}
	}

	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#remove()
	 */
	public final void remove(componentRemovalListener c) {
		synchronized(removed) {
			if(removed.compareAndSet(false, true)) {
				stop();
				internal_remove();
				try { EndPointManager.getEndPointManager().destroyComponent(ep.getID());}
				catch (ConfigurationException e) { logger.error("Cant remove EndPoint...");	}
				
				/* Unregisters with the EventHandler */
				EventDispatcher.getInstance().removeProducer(id.getName());
				logger.debug("protocol " + type +" removed");
			}
		}
	}
	
	/**
	 * This method specifies whether or not mulitple instances of this protocol
	 * can exist at once.
	 * @return whether or not mulitple instances of this protocol can exist at once.
	 */
	public final boolean supportsMultipleInstances(){
		return multipleInstances;
	}
	

	/**
	 * Associate a new sensor managed by this AbstractProtocol.Also checks whether this sensor is supported
	 * @param s the sensor to be added
	 * @throws ConfigurationException if the sensor cannot be associated with the protocol (sensor not supported, protocol removed)  
	 */
	public final void associateSensor(Sensor s) throws ConfigurationException{
		synchronized(removed) {
			if (!removed.get()) {
					if(isSensorSupported(s)) {
						synchronized(sensors) {
							sensors.put(s.getID(), s);
							s.setPid(id);
							probeSensor(s);
						}
						logger.debug("Sensor associated (" + s.toString()+")");
					} else {
						logger.error("Sensor "+s.toString()+" not supported by protocol "+id.toString());
						throw new ConfigurationException("Sensor not supported");
					}
			} else {
				logger.error("cant associate a new sensor, the protocol is about to be removed");
				throw new ConfigurationException("AbstractProtocol removed");
			}
		}
	}
	
	/**
	 * This method returns (a copy of) the list of all sensors associated to this protocol.
	 * Sensors in this list may / may not exist anymore at the time the list is used. 
	 * @return a list of all sensors associated
	 */
	public final List<Sensor> getSensors() {
		synchronized(sensors){
			return new Vector<Sensor>(sensors.values());
		}
	}

	
	/**
	 * Unassociates a sensor managed by this logical port.
	 * This method returns only when the sensor is unassociated, ie if a command is being run while this method is
	 * called, it will block until the command finishes, then unassociated the sensor, thereby preventing it from 
	 * being used again.
	 * @param i the sensorID to be removed
	 */
	public final boolean unassociateSensor(SensorID i) {
		Sensor s;
		if(sensors.containsKey(i)) {
			//prevent other changes to sensors
			synchronized(sensors) {
				//make sure no command is being run on the sensor
				s = sensors.get(i);
				synchronized(s) {
					if(sensors.remove(i) == null) {
						logger.error("Cant unassociate sensor with key " + i.toString() +  ": No such element");
						return false;
					} else {
						logger.debug("unassociated sensor with key " + i.toString() +  " from protocol "+toString());
					}
				}
			}
		} else {
			logger.error("Sensor " + i.toString()+ " doesnt exist and can NOT be unassociated");
			return false;
		}
		return true;
	}
	
	/**
	 * returns a textual representation of a AbstractProtocol's instance
	 * @return the textual representation of the AbstractProtocol's instance
	 */
	public final String toString() {
		return "protocol "+id.getName()+"("+type+")";

	}

	/**
	 * Sends a command to a sensor
	 * @param c the command
	 * @param sid the sensorID
	 * @return the result
	 * @throws BadAttributeValueExpException 
	 * @throws NotActiveException 
	 */
	public final byte[] execute(Command c, SensorID sid) throws BadAttributeValueExpException, NotActiveException {
		byte[] ret_val = {};
		Sensor s = sensors.get(sid);
		if(started.get()) {
			//Check if we have the sensor
			if (s!=null) {
				//sync with respect to other commands
				synchronized(s){
					//Catch the generic commands enable & disable
					if(c.getCID()==AbstractStore.GENERIC_DISABLE_CID)
						s.disable();
					else if(c.getCID()==AbstractStore.GENERIC_ENABLE_CID)
						s.enable();
					else {
						//sensor specific command
						//Check if it s idle
						if(s.startRunCmd()) {
							try {
								Class<?>[] params = {Command.class,Sensor.class};
								//logger.debug("Looking for method name for command ID "+c.getCID()+" - got: "+cmls.getMethodName(internal_getCMLStoreKey(s), c.getCID()));
								Method m = this.getClass().getDeclaredMethod(cmls.getMethodName(internal_getCMLStoreKey(s), c.getCID()), params);
								logger.debug("Running method: "+ m.getName()+" on sensor ID:"+sid.getName() );
								ret_val = (byte[]) m.invoke(this,c, s);
							} catch (ConfigurationException e) {
								logger.error("Cant find the method matching this command "+c.getCID());
								s.finishRunCmd();
								throw new BadAttributeValueExpException("");
							} catch (SecurityException e) {
								logger.error("Not allowed to execute the method matching this command");
								s.finishRunCmd();
								throw new BadAttributeValueExpException("");
							} catch (NoSuchMethodException e) {
								logger.error("Could NOT find the method matching the command");
								s.finishRunCmd();
								throw new BadAttributeValueExpException("");
							} catch (InvocationTargetException e) {
								logger.error("The command returned an exception:" + e.getClass() + " - " +e.getMessage());
								if(e.getCause()!=null) logger.error("Caused by:" + e.getCause().getClass() + " - "+e.getCause().getMessage());
								e.printStackTrace();
								//FIXME: when exceptions are fixed, get subclasses to throw two different exceptions,
								//FIXME: check which one is thrown here, and call either s.finishRunCmd(); or s.disconnect();  
								s.finishRunCmd();
								//s.disconnect();
								throw new NotActiveException("");
							} catch (Exception e) {
								logger.error("Could NOT run the command (error with invoke() )");
								logger.error("exception:" + e.getClass() + " - " +e.getMessage());
								if(e.getCause()!=null) logger.error("caused by:" + e.getCause().getClass() + " - "+e.getCause().getMessage());
								e.printStackTrace();
								s.finishRunCmd();
								throw new BadAttributeValueExpException("");
							}
							s.finishRunCmd();
						} else {
							//logger.error("Sensor "+sid.getName()+" not available to run the command");
							//TODO throw a better exception here
							throw new NotActiveException("Sensor "+sid.getName()+" not available to run the command");
						}
					}
				}
			} else {
				//logger.error("Sensor not present.Cannot execute the command");
				//TODO throw an exception here
			}
		} //else
			//logger.error("protocol not started.Cannot execute the command");
			//TODO throw an exception here
		return ret_val;
	}
	
	/**
	 * This method is called by parseConfig() when this object is contructed to 
	 * check whether a endPoint type is supported by this protocol
	 * @param String the EndPoint type
	 */
	private final boolean isEPTypeSupported(String type) {
		return supportedEndPointTypes.contains(type);
	}
	
	/**
	 * This method is called by associateSensor() and start() to
	 * check whether a sensor is currently plugged-in/reachable/readable 
	 * and set its state accordingly (protocol specific)
	 * @param sensor the sensor to be probed
	 */
	private final boolean probeSensor(Sensor sensor) {
		if(started.get())
			synchronized(sensor) {return internal_probeSensor(sensor);}
		return false;
	}	

	/**
	 * This method is called when the protocol is started and when a new sensor is associated with this protocol.
	 * It must check whether a sensor is currently plugged-in/reachable/readable and set its state accordingly 
	 * using s.enable(). The sensor must NOT be removed ! (calls to this methods are synchronized wrt. commands)
	 * @param s the sensor to be probed
	 */
	protected abstract boolean internal_probeSensor(Sensor s);
	
	/**
	 * This method is called by associateSensor() and getCML() to
	 * check whether a sensor is supported by this protocol
	 * @param sensor the sensor to be probed
	 */
	private final boolean isSensorSupported(Sensor s) {
		try {
			if(s.getConfig(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE).equals(id.getName())) {
				return internal_isSensorSupported(s);
			}
		} catch (BadAttributeValueExpException e) {
			logger.error("Cant find the name of the protocol the sensor is to be associated with");
			return false;
		}
		logger.debug("The protocol associated with this sensor doesnt match this protocol's name. Sensor not supported");
		return false;
	}
	
	/**
	 * Check whether a sensor is supported by this protocol regardless of whether the sensor's current state
	 * @param s the sensor to be probed
	 */
	protected abstract boolean internal_isSensorSupported(Sensor s);
	

	/**
	 * This method must stop the protocol. Calls to this method are synchronized wrt to calls to execute(),
	 * start() and remove()
	 *
	 */
	protected abstract void internal_stop();
	
	/**
	 * Starts the protocol. When this method returns, the protocol must be ready to handle
	 * requests to access (execute()), probe (probeSensor()) sensors without any further configuration.
	 * internal_start should not call probeSensor. Calls to this method are synchronized wrt calls to execute(),
	 * start() and remove().
	 */
	protected abstract void internal_start() throws ConfigurationException;
	
	/**
	 * This method prepares the subclass to be removed. Calls to this method are synchronized wrt calls to execute(),
	 * start() and remove().
	 */
	protected abstract void internal_remove();
	
	/**
	 * Parse the configuration of the protocol itself.
	 * !!! this method is called from the super class constructor and BEFORE the subclass constructor !!!
	 * !!! therefore none of the fields from the subclass are accessible. This method should only check!!
	 * !!! that the required config directive are present and nothing more !!!
	 */
	protected abstract void internal_parseConfig() throws ConfigurationException;
	
	
	/**
	 * This method returns the CML docu for a given sensor
	 * @param i the sensor whose CML we need
	 * @return the CML description document
	 * @throws ConfigurationException if the sensor is not found or isnt supported by this protocol
	 */
	//TODO make me throw a better exception
	public final CMLDescriptions getCML(SensorID i) throws ConfigurationException {
		String key;
		Sensor s =sensors.get(i);
		if(s!=null) {
			if(isSensorSupported(s)) {
				key = internal_getCMLStoreKey(s);
				if (key!=null) {
					return cmls.getCMLDescriptions(key);  
				} else {
					logger.error("Error getting the key for sensor " + s.toString());
					throw new ConfigurationException("Error getting the key for sensor " + s.toString());
				}
			}else {
				logger.error("Sensor " +s.toString()+" not supported by this protocol");
				throw new ConfigurationException("Sensor " +s.toString()+" not supported by this protocol");
			}
		}
		logger.error("Sensor "+ i.toString()+" is not associated with this protocol" );
		throw new ConfigurationException("Sensor "+ i.toString()+" is not associated with this protocol");
	}
	
	/**
	 * Returns the CML store key for a sensor if supported. The key is the one used by the CML store:
	 * one CML doc is associated with one key (sensor native address, sensor family, ...)
	 * @param s the sensor
	 * @return the CML store key as a string
	 */
	protected abstract String internal_getCMLStoreKey(Sensor s) throws ConfigurationException;
	
	/**
	 * this method should be overriden by protocols which provide sensor autodetection
	 * and return a list of native address (strings) of currently connected/visible sensors 
	 */
	protected List<String> detectConnectedSensors() {
		logger.debug(id.toString() + "Not configured for autodetection");
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.EndPoints.DeviceListener#adapterChange(int)
	 */
	public void adapterChange(int n, String id){}
	
	protected final synchronized void startAutodetectThread(){
		if(autoDetectionInterval != 0 && (autodetectThread==null || !autodetectThread.isAlive())) {
			//Start the sensor monitoring thread
			autodetectThread = new Autodetection();
			logger.debug("Starting autodetect thread");
			autodetectThread.start();
		} else {
			logger.info("Autodetect interval set to 0 in the protocol config.");
			logger.info("Disabling sensor autodetection");
		}
	}
	
	protected final synchronized void stopAutodetectThread() {
		if(autodetectThread!=null && autodetectThread.isAlive()) {
			logger.debug("stopping autodetect thread ...");
			autodetectThread.interrupt();
			try { autodetectThread.join();}
			catch (InterruptedException e) {
				logger.error("interrupted while waiting for autodetect thread to finish");
			}
			logger.debug("autodetect thread stopped");
		}
	}
	
	/**
	 * Is the protocol started ? 
	 */
	public final boolean isStarted() {
		return started.get();
	}
	
	private class Autodetection implements Runnable {
		Thread t;
		
		public Autodetection() {
			t= new Thread(this, "AbstractProtocol autodetection_thread_"+id.getName());
		}
		
		public void start() {
			t.start();
		}
		
		public void interrupt() {
			t.interrupt();
		}
		
		public boolean isAlive(){
			return t.isAlive();
		}

		public void join() throws InterruptedException {
			t.join();
		}
		
		/**
		 * Implements the autodetection thread
		 */
		public void run() {
			List<String> detected;
			Sensor stmp;
			List<Sensor> current;
			Iterator<Sensor> iter;
			SensorManager sm = SensorManager.getSensorManager();
			try {
				logger.debug("Autodetect thread started ("+id.toString()+")");
				while(!Thread.interrupted() && (detected = detectConnectedSensors())!=null) {

					//for each sensor in current
					synchronized(sensors) {
						//logger.debug("Checking whats detected..."+detected.size());
						current = new ArrayList<Sensor>(sensors.values());
						iter = current.iterator();
						while(iter.hasNext()) {
							//if that sensor is in detected also, we remove both
							stmp = iter.next();
							if(detected.contains(stmp.getNativeAddress())) {
//								logger.debug("sensor "+stmp.toString()+" found in both current & detected");
								if(stmp.isDisconnected()) {
									logger.debug("reconnecting "+stmp.toString());
									stmp.reconnect();
								}
								detected.remove(stmp.getNativeAddress());
								iter.remove();
							} else if (stmp.isDisconnected()) {
//								logger.debug("sensor "+stmp.toString()+" FOUND in current and NOT FOUND in detected and already DISCONNECTED");
								detected.remove(stmp.getNativeAddress());
								iter.remove();
							} 
						}
					}

					//now we re left with newly-connected sensors in detected and
					//removed sensors in current
					Iterator<String> it = detected.iterator();
					while(it.hasNext()) {
							try {
								if(sm.createComponent(sm.generateSensorConfig(null, it.next(), id))==null)
									logger.error("couldnt create the autodetected sensor from its autogenerated XML config: \n"+t);
							} catch (ConfigurationException e) {
								logger.error("couldnt instanciate component");
								e.printStackTrace();
							} catch (ParserConfigurationException e) {
								logger.error("couldnt instanciate component - most likely an error in the XML doc");
								e.printStackTrace();
							}
					}
					
					iter = current.iterator();
					while(iter.hasNext()) {
							stmp = iter.next();
							logger.debug("disconnecting "+stmp.toString());
							stmp.disconnect();
					}
					if(autoDetectionInterval > 0)
						Thread.sleep(Long.valueOf(autoDetectionInterval));
					else 
						interrupt();
				}
			} catch (InterruptedException e) {}
			logger.debug("Autodetect thread exiting ("+id.toString()+")");
		}
	}

}
