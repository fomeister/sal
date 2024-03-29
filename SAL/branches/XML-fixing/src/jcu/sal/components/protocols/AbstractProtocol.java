/**
 * @author gilles
 */
package jcu.sal.components.protocols;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;


import jcu.sal.common.Parameters;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.exceptions.SensorDisconnectedException;
import jcu.sal.common.pcml.PCMLConstants;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLDescription;
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


/**
 * @author gilles
 *
 */
public abstract class AbstractProtocol extends AbstractComponent<ProtocolID, ProtocolConfiguration>
										implements DeviceListener {

	private static Logger logger = Logger.getLogger(AbstractProtocol.class);
	static {	Slog.setupLogger(logger); }

	/**
	 * A list of endpoints types supported by this protocol. Must be filled in by subclass
	 */
	public final Vector<String> supportedEndPointTypes;
	

	/**
	 * A table of sensors managed by this protocol and associated with it. 
	 */
	protected Hashtable<SensorID, Sensor> sensors;
	
	/**
	 * The endpoint associated with this protocol.
	 */
	protected EndPoint ep;
	
	/**
	 * Can there be multiple instances of this protocol at once ? Must be set by the subclass
	 */
	protected boolean multipleInstances;
	
	/**
	 * The CML store associated with this protocol. This field must be instanciated by the
	 *  subclass in <code>internal_parseConfig()</code>
	 */
	protected AbstractStore cmls=null;
	
	/**
	 * Is this protocol started ?
	 */
	private AtomicBoolean started;
	
	
	/**
	 * Can the protocol automatically detect sensor addition/removal ?
	 * If this value is negative, then the autodetection thread is executed only once.
	 * If it is set to 0, the autodetection thread never starts
	 * A positive value specifies how often (in milliseconds) the autodetect thread will try to detect
	 * what s currently connected.
	 * <b>Only the default value must be set by the subclass constructor. <code>parseConfig()</code> will set the
	 * value to that of the "AutoDetectSensor" parameter if it exists. If not, the value is untouched,
	 * and stays to the default set by the subclass's constructor. Also note that if the value MUST be kept
	 * to its default, it must be set AGAIN in <code>internal_parseConfig()</code> to override any
	 * change introduce by the presence of the "AutoDetectSensor" parameter.</b>
	 */
	protected int autoDetectionInterval = 0;
	private Autodetection autodetectThread = null;
	
	/**
	 * The subclass stores here the IDs of devices the EndPoint should watch for
	 * Example: OWFS stores the USB IDs of DS2490 so UsbEndPoint can notify OWFS when new ds2490
	 * are connected  
	 */
	protected String[] epIds = null;
	
	protected AtomicBoolean removed;
	
	/**
	 * Construct a new protocol gien its ID, configuration object and its type.
	 * This constructor checks that the configuration obejct is of type 't'. If not, a ConfigurationException
	 * is thrown
	 * @param i the protocol identifier
	 * @param t the type of this protocol
	 * @param p the protocol configuration object
	 * @throw ConfigurationException if the configuration obejct is not of type 't'
	 */
	public AbstractProtocol(ProtocolID i, String t, ProtocolConfiguration p) throws ConfigurationException{
		super(p, i);
		if(!p.getType().equals(t))
			throw new ConfigurationException("Configuration object is of type '"+p.getType()+"', expected "+t);
		started= new AtomicBoolean(false);
		removed=new AtomicBoolean(false);


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
		//logger.debug("Parsing our configuration");
		
		try { autoDetectionInterval = Integer.parseInt(getParameter(PCMLConstants.AUTODETECTSENSORS_TAG));}
		catch (NotFoundException e) {}
		//keep default value supplied by the Protocol subclass if not found in config
		
		/* check if we have already instancicated CMl store (we shouldnt, CML store instanciation msut be done in
		 * (internal_)parse_config() 
		 */
		if(cmls!=null)
			throw new ConfigurationException();

		/*
		 * check if the endpoint type is one of the supported types
		 */
		if(!isEPTypeSupported(config.getEPConfig().getType())) {
			logger.error("This AbstractProtocol has been setup with the wrong enpoint: got endpoint type: "
							+config.getEPConfig().getType()+", expected: ");
			for(String s: supportedEndPointTypes)
				logger.error(s);
			throw new ConfigurationException("Wrong Endpoint type");
		}

		/* construct the endpoint  */
		ep = EndPointManager.getEndPointManager().createComponent(config.getEPConfig());
		if(ep==null)
			throw new ConfigurationException("Couldnt create the EndPoint");

		/* Sets the PID field of the EndPointID */
		ep.setPid(id);
			
		try { internal_parseConfig(); }
		catch (ConfigurationException e) {
			logger.error("Error configuring the protocol, destroying the endpoint");
			try {
				EndPointManager.getEndPointManager().destroyComponent(ep.getID());
			} catch (NotFoundException e1) {
				logger.error("We shouldnt be here - cant destroy the endpoint");
				throw new SALRunTimeException("cant destroy newly created endpoint",e1);
			}
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
			//logger.debug("Probing associated sensors for AbstractProtocol " + toString());
			synchronized(sensors) {
				for(Sensor s: sensors.values())
					probeSensor(s);
			}
			
			//logger.debug("protocol "+config.getType()+" started");
			
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
				catch (UnsupportedOperationException e) { logger.error("Autodetect not supported by the EndPoint");}
			} else
				/* Start Autodetect thread */
				startAutodetectThread();
		}
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#stop()
	 */
	public final void stop() {
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
				for(Sensor s: sensors.values()){
					synchronized(s){s.disable();}
				}
			}
			/*At this stage, we are sure no commands are being run or will be on our sensors */
			
			/* Stop ourselves */
			internal_stop();
			/* Stop EP */
			ep.stop();
			
			//logger.debug("protocol "+config.getType()+" stopped");
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
				catch (NotFoundException e) { logger.error("Cant remove EndPoint...");	}
				
				/* Unregisters with the EventHandler */
				EventDispatcher.getInstance().removeProducer(id.getName());
				//logger.debug("protocol " + config.getType() +" removed");
			}
		}
	}
	
	/**
	 * This method specifies whether or not multiple instances of this protocol
	 * can exist at once.
	 * @return whether or not multiple instances of this protocol can exist at once.
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
						//no need to check if we already have a sensor with the same native address as this is done
						//by the SensorManager. it will check the config file to see if a sensor with the same characteristics
						//(address, protocol name) already exists and if yes, the same SID will be returned, and the new sensor wont
						//be created because a sensor with the same ID already exist, so here, we wont even know about it !
						sensors.put(s.getID(), s);
						s.setPid(id);
						probeSensor(s);
					}
					//logger.debug("Sensor associated (" + s.toString()+")");
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
	 * @throws NotFoundException if the sensor isnt associated with this protocol
	 */
	public final void unassociateSensor(SensorID i) throws NotFoundException {
		Sensor s;
		//prevent other changes to sensors
		synchronized(sensors) {
			if(sensors.containsKey(i)) {
				//make sure no command is being run on the sensor
				s = sensors.get(i);
				synchronized(s) {
					sensors.remove(i);
				}
			} else {
				logger.error("Sensor " + i.toString()+ " doesnt exist and can NOT be unassociated");
				throw new NotFoundException("no sensor named '"+i.toString()+"'");
			}
		}
	}
	
	/**
	 * returns a textual representation of a AbstractProtocol's instance
	 * @return the textual representation of the AbstractProtocol's instance
	 */
	public final String toString() {
		return "protocol "+id.getName()+"("+config.getType()+")";

	}

	/**
	 * Sends a command to a sensor
	 * @param c the command
	 * @param sid the sensorID
	 * @return the result
	 * @throws NotFoundException if the sensor ID doesnt match any existing sensor
	 * @throws SensorControlException if anything went wrong 
	 * @throws SALRunTimeException if a programming error occurs
	 */
	public final byte[] execute(Command c, SensorID sid) throws SensorControlException, NotFoundException{
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
								//logger.debug("Running method: "+ m.getName()+" on sensor ID:"+sid.getName() );
								ret_val = (byte[]) m.invoke(this,c, s);
							} catch (SecurityException e) {
								logger.error("Not allowed to execute the method matching this command");
								s.finishRunCmd();
								throw new SALRunTimeException("Programming error in the protocol subclass",e);
							} catch (NoSuchMethodException e) {
								logger.error("Could NOT find the method matching the command");
								s.finishRunCmd();
								throw new SALRunTimeException("Programming error in the protocol subclass",e);
							} catch (InvocationTargetException e) {
								//logger.error("The command returned an exception:" + e.getClass() + " - " +e.getMessage());
								//e.printStackTrace();
								if(e.getCause() instanceof SensorDisconnectedException){
									logger.debug("Disconnecting sensor '"+sid.getName()+"("+s.getNativeAddress()+")'");
									s.disconnect();
								} else 
									s.finishRunCmd();

								throw new SensorControlException("Sensor control error",e.getCause());
							} catch (Exception e) {
								logger.error("Could NOT run the command (error with invoke() )");
								e.printStackTrace();
								s.finishRunCmd();
								throw new SALRunTimeException("Programming error in the protocol subclass",e);
							}
							s.finishRunCmd();
						} else {
							//logger.error("Sensor "+sid.getName()+" not available to run the command");
							throw new SensorControlException("sensor unavailable", new SensorDisconnectedException("Sensor "+sid.getName()+" not available to run the command"));
						}
					}
				}
			} else {
				//logger.error("Sensor not present.Cannot execute the command");
				throw new NotFoundException("No sensor matching name '"+sid.getName()+"'");
			}
		} else {
			logger.error("protocol not started.Cannot execute the command");
			throw new SALRunTimeException("protocol not started - cant run the command");
		}
		return ret_val;
	}
	
	/**
	 * This method is called by parseConfig() when this object is constructed to 
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
			if(s.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE).equals(id.getName())) {
				return internal_isSensorSupported(s);
			}
		} catch (NotFoundException e) {
			logger.error("Cant find the name of the protocol the sensor is to be associated with");
			return false;
		}
		//logger.debug("The protocol associated with this sensor doesnt match this protocol's name. Sensor not supported");
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
	 * Parse the configuration of the protocol itself. This method should check the values of the parameters in the
	 * <code>config</code> object, and apply them if sensible. Also, note that the value of <code>autoDetectionInterval</code>
	 * will be changed to that of the "AutoDetecSensor" parameter if present (If not present, the value is untouched).
	 */
	protected abstract void internal_parseConfig() throws ConfigurationException;
	
	
	/**
	 * This method returns the CML docu for a given sensor
	 * @param i the sensor whose CML we need
	 * @return the CML description document
	 * @throws NotFoundException if the sensor is not found or isnt supported by this protocol
	 */
	public final CMLDescriptions getCML(SensorID i) throws NotFoundException {
		String key;
		Sensor s =sensors.get(i);
		if(s!=null) {
			if(isSensorSupported(s)) {
				key = internal_getCMLStoreKey(s);
				if (key!=null) {
					return cmls.getCMLDescriptions(key);  
				} else {
					logger.error("Error getting the key for sensor " + s.toString());
					throw new NotFoundException("Error getting the key for sensor " + s.toString());
				}
			}else {
				logger.error("Sensor " +s.toString()+" not supported by this protocol");
				throw new NotFoundException("Sensor " +s.toString()+" not supported by this protocol");
			}
		}
		logger.error("Sensor "+ i.toString()+" is not associated with this protocol" );
		throw new NotFoundException("Sensor "+ i.toString()+" is not associated with this protocol");
	}
	
	/**
	 * Returns the CML store key for a sensor if supported. The key is the one used by the CML store:
	 * one CML doc is associated with one key (sensor native address, sensor family, ...)
	 * @param s the sensor
	 * @return the CML store key as a string
	 */
	protected abstract String internal_getCMLStoreKey(Sensor s);
	
	/**
	 * this method should be overriden by protocols which provide sensor autodetection
	 * and return a list of native address (strings) of currently connected/visible sensors 
	 */
	protected List<String> detectConnectedSensors() {
		//logger.debug(id.toString() + "Not configured for autodetection");
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
			//logger.debug("Starting autodetect thread");
			autodetectThread.start();
		} else {
			//logger.info("Autodetect interval set to 0 in the protocol config.");
			//logger.info("Disabling sensor autodetection");
		}
	}
	
	protected final synchronized void stopAutodetectThread() {
		if(autodetectThread!=null && autodetectThread.isAlive()) {
			//logger.debug("stopping autodetect thread ...");
			autodetectThread.interrupt();
			try { autodetectThread.join();}
			catch (InterruptedException e) {
				logger.error("interrupted while waiting for autodetect thread to finish");
			}
			//logger.debug("autodetect thread stopped");
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
			List<Sensor> current;
			Iterator<Sensor> iter;
			Sensor stmp;
			SensorManager sm = SensorManager.getSensorManager();
			try {
				//logger.debug("Autodetect thread started ("+id.toString()+")");
				while(!Thread.interrupted() && (detected = detectConnectedSensors())!=null) {

					//for each sensor in current
					synchronized(sensors) {
						//logger.debug("Checking whats detected..."+detected.size());
						current = new ArrayList<Sensor>(sensors.values());
						for(iter = current.iterator(); iter.hasNext();) {
							//if that sensor is in detected also, we remove both
							stmp = iter.next();
							if(detected.contains(stmp.getNativeAddress())) {
							//logger.debug("sensor "+stmp.toString()+" found in both current & detected");
								if(stmp.isDisconnected()) {
									//logger.debug("reconnecting "+stmp.toString());
									stmp.reconnect();
								}
								detected.remove(stmp.getNativeAddress());
								iter.remove();
							} else if (stmp.isDisconnected()) {
								//logger.debug("sensor "+stmp.toString()+" FOUND in current and NOT FOUND in detected and already DISCONNECTED");
								detected.remove(stmp.getNativeAddress());
								iter.remove();
							}
						}
					}

					//now we re left with newly-connected sensors in detected and
					//removed sensors in current
					Vector<Parameter> plist = new Vector<Parameter>();
					for(String s: detected){
						plist.removeAllElements();
						plist.add(new Parameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE,s));
						plist.add(new Parameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, id.getName()));
						plist.add(new Parameter(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE, config.getType()));
							try {
								if(sm.createComponent(new SMLDescription(new Integer(1),new Parameters(plist)))==null)
									logger.error("couldnt create the autodetected sensor from its autogenerated XML config: \n"+t);
							} catch (ConfigurationException e) {
								logger.error("couldnt instanciate component");
								e.printStackTrace();
							} catch (SALDocumentException e) {
								logger.error("We shouldnt be here - cant create an SML description");
								e.printStackTrace();
							}
					}
					
					for(Sensor s: current)
							//logger.debug("disconnecting "+s.toString());
							s.disconnect();
					
					if(autoDetectionInterval > 0)
						Thread.sleep(Long.valueOf(autoDetectionInterval));
					else 
						interrupt();
				}
			} catch (InterruptedException e) {}
			//logger.debug("Autodetect thread exiting ("+id.toString()+")");
		}
	}

}
