/**
 * @author gilles
 */
package jcu.sal.Components.Protocols;

import java.io.NotActiveException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.Command;
import jcu.sal.Components.componentRemovalListener;
import jcu.sal.Components.EndPoints.DeviceListener;
import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Protocols.CMLStore.CMLStore;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.Components.Sensors.SensorID;
import jcu.sal.Managers.EndPointManager;
import jcu.sal.Managers.SensorManager;
import jcu.sal.events.EventDispatcher;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * @author gilles
 *
 */
public abstract class Protocol extends AbstractComponent<ProtocolID>  implements DeviceListener {

	private Logger logger = Logger.getLogger(Protocol.class);
	
	public static final String PROTOCOLTYPE_TAG = "type";
	public static final String PROTOCOLNAME_TAG = "name";
	public static final String PROTOCOL_TAG="Protocol";
	public static final String PROTOCOLSECTION_TAG="protocols";
	
	/**
	 * how often the autodetection process should kick in (in milliseconds)
	 */
	protected static int AUTODETECT_INTERVAL = 2 * 1000;
	
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
	 * The CML store associated with this protocol. This field must be instanciated by the subclass
	 */
	protected CMLStore cmls;
	
	/**
	 * Is this protocol started ?
	 */
	//private boolean started;
	private AtomicBoolean started;
	
	/**
	 * Can the protocol automatically detect sensor addition/removal ? 
	 */
	protected boolean autodetect;
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
	 * @throws ConfigurationException 
	 */
	public Protocol(ProtocolID i, String t, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super();
		Slog.setupLogger(logger);
		started= new AtomicBoolean(false);
		removed=new AtomicBoolean(false);
		autodetect = false;
		
		/* construct the endpoint first */
		ep = EndPointManager.getEndPointManager().createComponent(d);
		if(ep==null)
			throw new ConfigurationException("Couldnt create the EdnPoint");

		/* Sets the PID field of the EndPointID */
		ep.setPid(i);
		
		/* init the rest of the fields */
		id = i;
		type = t;
		config = c;
		sensors = new Hashtable<SensorID, Sensor>();
		
		/* Registers with the EventHandler */
		EventDispatcher.getInstance().addProducer(id.getName());
		
		/* parse the configuration */
		try {
			parseConfig();
		} catch (ConfigurationException e) {
			logger.error("Error creating the protocol, destroying the endpoint");
			EndPointManager.getEndPointManager().destroyComponent(ep.getID());
			throw e;
		}
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
		if(started.compareAndSet(false, true)) {
			/* Register ourselves with EndPoint for notifications BEFORE starting the endPoint
			 * so we get a notification of the initially connected devices !*/ 
			if(epIds!=null) {
				try { ep.registerDeviceListener(this, epIds); }
				catch (UnsupportedOperationException e) { logger.debug("Autodetect not supported by the EndPoint");}
			}
			
			/* Start the EndPoint */
			ep.start();
			/* Start the ourselves */
			internal_start();
			/* Probe associted sensors */
			logger.debug("Probing associated sensors for Protocol " + toString());
			synchronized(sensors) {
				Iterator<Sensor> i = sensors.values().iterator();
				while(i.hasNext())
					probeSensor(i.next());
			}
			/* Start Autodetect thread */
			startAutodetectThread();
			
			logger.debug("protocol "+type+" started");
		}
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#stop()
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
	 * @see jcu.sal.Components.HWComponent#remove()
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
				this.logger.debug("protocol " + type +" removed");
			}
		}
	}

	/**
	 * Associate a new sensor managed by this Protocol
	 * Also checks whether this sensor is supported, requires the access to be
	 * synchronized with respect to this protocol (synchronized(p))
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
				throw new ConfigurationException("Protocol removed");
			}
		}
	}
	
	/**
	 * Lists all sensors associated to this logical port
	 * @return a list of all sensors associated
	 */
	public final ArrayList<Sensor> getSensors() {
		synchronized(sensors){
			return new ArrayList<Sensor>(sensors.values());
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
	 * returns a textual representation of a Protocol's instance
	 * @return the textual representation of the Protocol's instance
	 */
	public final String toString() {
		return "Protocol "+id.getName()+"("+type+"), EndPoint: " + ep.toString();

	}

	/**
	 * Sends a command to a sensor
	 * @param c the command
	 * @param sid the sensorID
	 * @return the result
	 * @throws BadAttributeValueExpException 
	 * @throws NotActiveException 
	 */
	public String execute(Command c, SensorID sid) throws BadAttributeValueExpException, NotActiveException {
		String ret_val = null;
		Sensor s = sensors.get(sid);
		if(started.get()) {
			//Check if we have the sensor
			if (s!=null) {
				//sync with respect to other commands
				synchronized(s){
					//Catch the generic commands enable & disable
					if(c.getCID().intValue()==CMLStore.DISABLE_CID)
						s.disable();
					else if(c.getCID().intValue()==CMLStore.ENABLE_CID)
						s.enable();
					else {
						//sensor specific command
						//Check if it s idle
						if(s.startRunCmd()) {
							try {
								Class<?>[] params = {Hashtable.class,Sensor.class};
								Method m = this.getClass().getDeclaredMethod(commands.get(c.getCID()), params);
//								logger.debug("running method: "+ m.getName()+" SID:"+sid.getName() );
								ret_val = (String) m.invoke(this,c.getParameters(), s);
								logger.debug("running method: "+ m.getName()+" SID:"+sid.getName()+" returned "+ret_val );
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
								logger.error("The command returned an exception:" + e.getClass() + " - " +e.getMessage());
								logger.error("Caused by:" + e.getCause().getClass() + " - "+e.getCause().getMessage());
								e.printStackTrace();
								//s.finishRunCmd();
								s.disconnect();
								throw new NotActiveException("");
							} catch (Exception e) {
								logger.error("Could NOT run the command (error with invoke() )");
								logger.error("exception:" + e.getClass() + " - " +e.getMessage());
								logger.error("caused by:" + e.getCause().getClass() + " - "+e.getCause().getMessage());
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
		if(started.get())
			synchronized(sensor) {return internal_probeSensor(sensor);}
		return false;
	}
	
	/**
	 * Check whether a sensor is supported by this protocol
	 * @param sensor the sensor to be probed
	 */
	public final boolean isSensorSupported(Sensor s) {
		try {
			if(s.getConfig(Sensor.PROTOCOLATTRIBUTE_TAG).equals(id.getName())) {
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
	 * Check whether a sensor is supported by this protocol (protocol specific)
	 * @param s the sensor to be probed
	 */
	protected abstract boolean internal_isSensorSupported(Sensor s);
	
	/**
	 * Check whether a sensor is currently plugged-in/reachable/readable 
	 * and set its state accordingly (protocol specific). The sensor must NOT
	 * be removed ! (calls to this methods are synchronized wrt. commands
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
	 * Parse the configuration of the protocol itself.
	 * !!! this method is called BEFORE the subclass constructor !!!
	 */
	protected abstract void internal_parseConfig() throws ConfigurationException;
	
	/**
	 * Returns the CML store key for a sensor if supported. The key is the one used by the CML store:
	 * one CML doc is associated with one key (sensor native address, sensor family, ...)
	 * @param s the sensor
	 * @return the CML store key as a string
	 */
	protected abstract String internal_getCMLStoreKey(Sensor s) throws ConfigurationException;
	
	/**
	 * this method should be overriden by protocols which provide sensor autodetection
	 * and return a Vector of native address (strings) of currently connected/visible sensors 
	 */
	protected Vector<String> detectConnectedSensors() {
		logger.debug(id.toString() + "Not configured for autodetection");
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Components.EndPoints.DeviceListener#adapterChange(int)
	 */
	public void adapterChange(int n){
	}
	
	/**
	 * This method returns the CML docu for a given sensor
	 * @param i the sensor whose CML we need
	 * @return the CML
	 * @throws ConfigurationException if the sensor is not found or isnt supported by this protocol
	 */
	//TODO make me throw a better exception
	public String getCML(SensorID i) throws ConfigurationException {
		String cml, key;
		Sensor s =sensors.get(i);
		if(s!=null) {
			if(isSensorSupported(s)) {
				key = internal_getCMLStoreKey(s);
				if (key!=null) {
					cml =cmls.getCML(key);  
					if(cml!=null) {
						return "<commands xmlns=\"http://jcu.edu.au/sal\">\n"+cml+"<commands>";
					} else {
						logger.error("cant find a CML doc for Sensor " +s.toString()+" key:" + key);
						throw new ConfigurationException("cant find a CML doc for Sensor " +s.toString()+" key:" + key);
					}
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
	
	protected synchronized void startAutodetectThread(){
		if(autodetectThread==null || !autodetectThread.isAlive()) {
			//Start the sensor monitoring thread
			autodetectThread = new Autodetection();
			if(autodetect && AUTODETECT_INTERVAL!=0) {
				logger.debug("Starting autodetect thread");
				autodetectThread.start();
			} else if (AUTODETECT_INTERVAL==0){
				logger.info("Autodetect interval set to 0 in the protocol config.");
				logger.info("Disabling sensor autodetection");
			} else {
				logger.debug("Sensor autodetection not supported");
			}
		}
	}
	
	protected synchronized void stopAutodetectThread() {
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
			t= new Thread(this, "Protocol autodetection_thread_"+id.getName());
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
			Vector<String> detected;
			Sensor stmp;
			ArrayList<Sensor> current;
			Iterator<Sensor> iter;
			try {
				logger.debug("Autodetect thread started ("+id.toString()+")");
				while(!Thread.interrupted() && ((detected = detectConnectedSensors())!=null)) {

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
					SensorManager sm = SensorManager.getSensorManager();
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
					Thread.sleep(Long.valueOf(AUTODETECT_INTERVAL));
				}
			} catch (InterruptedException e) {}
			logger.debug("Autodetect thread exiting ("+id.toString()+")");
		}
	}

}
