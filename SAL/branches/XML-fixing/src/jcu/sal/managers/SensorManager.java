/**
 * 
 */
package jcu.sal.managers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.ConfigurationException;

import jcu.sal.common.Constants;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;
import jcu.sal.components.Identifier;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.components.sensors.SensorID;
import jcu.sal.config.FileConfigService;
import jcu.sal.events.EventDispatcher;
import jcu.sal.events.SensorNodeEvent;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 * 
 */
public class SensorManager extends AbstractManager<Sensor, SMLDescription> {
	private FileConfigService conf;
	
	/**
	 * specifies (in seconds) how often the sensor removal thread kick in
	 */
	public static int REMOVE_SENSOR_INTERVAL = 0;
	
	private static SensorManager s = new SensorManager();
	private Logger logger = Logger.getLogger(SensorManager.class);
	private ProtocolManager pm;
	private EventDispatcher ev;
	
	
	/**
	 * Private constructor
	 */
	private SensorManager() {
		super();
		Slog.setupLogger(this.logger);
		pm = ProtocolManager.getProcotolManager();
		conf = FileConfigService.getService();
		ev = EventDispatcher.getInstance();
		ev.addProducer(Constants.SENSOR_MANAGER_PRODUCER_ID);
	}

	/**
	 * Returns the instance of the SensorManager 
	 * @return
	 */
	public static SensorManager getSensorManager() {
		return s;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected Sensor build(SMLDescription s, Identifier id) throws InstantiationException {
		SensorID i = (SensorID) id;
		Sensor sensor = null;
		logger.debug("building sensor: "+id.getName());
		
		//check if the ID is the same as in the config object
		if(!id.getName().equals(s.getID())) {
			try {
				s = new SMLDescription(new Integer(id.getName()),s.getParameters());
			} catch (Exception e1) {
				logger.error("We shouldnt be here - error creating the new SMLDescription from the old one");
				e1.printStackTrace();
				s=null;
			}
		}
		
		//build the sensor
		try { sensor = new Sensor(i, s); }
		catch (ConfigurationException e) {
			logger.error("Couldnt instanciate the sensor: " + i.toString());
			//e.printStackTrace();
			throw new InstantiationException();
		}
		
		//Raise save sensor config flag
		try { conf.addSensor(s); }
		catch (ConfigurationException e) {
			logger.error("Couldnt saves the sensor's configuration ("+i.toString()+")");
			throw new InstantiationException();
		}

		//associate it with its protocol
		try { pm.associateSensor(sensor); }
		catch (ConfigurationException e) {
			logger.error("Couldnt associate the sensor with its protocol");
			throw new InstantiationException();
		}
		
		try {
			ev.queueEvent(new SensorNodeEvent(SensorNodeEvent.SENSOR_NODE_ADDED, i.getName(), Constants.SENSOR_MANAGER_PRODUCER_ID));
		} catch (ConfigurationException e) {logger.error("Cant queue event");}
		
		return sensor;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(SMLDescription s){
		/*
		 * The order in which a sensor id is looked up:
		 * first, the sensor config file is checked. if it isnt in the config file, a new one is generated
		 */
		Identifier id = null;
		try {
			//we first check to see if the sensor exists in the sensor configuration file
			id =conf.findSensor(s);
			logger.debug("Found the sid "+id.getName()+" in sensor config file");
		} catch (Exception e) {
			//we havent found a matching sensor in the sensor config file, so we are going to generate a new ID
			id = new SensorID(generateNewSensorID());
			logger.debug("created a new sensor id "+id.getName());
		}

		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(Sensor component) {
		try {
			pm.unassociateSensor(component);
		} catch (ConfigurationException e) {
			logger.error("Error unassociating sensor");
			e.printStackTrace();
		}
		component.remove(this);
		try {
			ev.queueEvent(new SensorNodeEvent(SensorNodeEvent.SENSOR_NODE_REMOVED,component.getID().getName(),Constants.SENSOR_MANAGER_PRODUCER_ID));
		} catch (ConfigurationException e) {logger.error("Cant queue event");}
	}

	
	/**
	 * This method returns an SMLDescriptions object for all sensors (if <code>onlyActive</code> is false), or only for
	 * currently-active sensors otherwise.
	 * @param onlyActive if set, the returned SMLDescriptions will be limited to cuurently active sensors. Otherwise, all
	 * known sensors will be included.   
	 * @return an SMLDescriptions object for the selected set of sensors
	 */
	public SMLDescriptions listSensors(boolean onlyActive){
		if(onlyActive){
			Sensor s;
			HashSet<SMLDescription> m = new HashSet<SMLDescription>();
			synchronized(ctable){
				Iterator<Sensor> i = ctable.values().iterator();
				while(i.hasNext()) {
					s = i.next();
					if(!onlyActive || (onlyActive && !s.isDisconnected()))
						m.add(s.getConfig());
				}	
			}
			return new SMLDescriptions(m);
		} else
			return new SMLDescriptions(conf.getSensors());		
	}
	
	/**
	 * This method removes a sensor's XML config information from the sensor config file
	 * @param sid the sensor ID for which the configuration information must be removed
	 * @throws ConfigurationException if the sensor is still active or the config info cant be deleted
	 */
	public void removeSensorConfig(SensorID sid) throws ConfigurationException {
		//Check if the sensor is still active
		if(getComponent(sid)!=null) {
			logger.error("Cant remove an active sensor configuration");
			throw new ConfigurationException();
		}

		try { conf.removeSensor(sid);}
		catch (ConfigurationException e) { logger.error("error deleting the sensor config");}
	}
	
	/**
	 * This method creates sensors for the specified protocol using information found 
	 * in the sensor configuration file.
	 * @param pid the protocol ID for which sensors must be created
	 * @throws ConfigurationException if the sensor is still active or the config info cant be deleted
	 */
	public void loadSensorsFromConfig(ProtocolID pid) throws ConfigurationException {
		logger.debug("Loading sensors from config file associated with protocol "+pid.getName());
		SMLDescription s;
		Iterator<SMLDescription> iter = conf.listSensors(pid).iterator();
		while(iter.hasNext()) { s = iter.next(); logger.debug("Creating sensor "+s.getSMLString());createComponent(s);};
	}

	
	/**
	 * Returns the first available unused sensor ID
	 * Must hold the lock to ctable
	 * @return the first available unused sensor ID
	 */
	private String generateNewSensorID() {
		//Get the SIDs existing in sensor config file
		Set<String> sids = conf.listSensorID();

		int i = 1;
		while(sids.contains(String.valueOf(i))) i++;
		
		logger.debug("Created new sensor id:"+i);
		
		return String.valueOf(i);
	}
}
