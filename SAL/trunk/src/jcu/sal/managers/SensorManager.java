/**
 * 
 */
package jcu.sal.managers;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.Constants;
import jcu.sal.common.sml.SMLConstants;
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
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * @author gilles
 * 
 */
public class SensorManager extends AbstractManager<Sensor> {
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
	protected Sensor build(Node n, Identifier id) throws InstantiationException {
		SensorID i = (SensorID) id;
		Sensor sensor = null;
		logger.debug("building sensor: "+id.getName());
		
		//build the sensor
		try { sensor = new Sensor(i, getComponentConfig(n)); }
		catch (ConfigurationException e) {
			logger.error("Couldnt instanciate the sensor: " + i.toString());
			//e.printStackTrace();
			throw new InstantiationException();
		}
		
		//saves its config
		try { conf.addSensor(n); }
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
	protected Identifier getComponentID(Node n){
		/*
		 * The order in which a sensor id is looked up:
		 * first, the sensor config file is checked. if it isnt in the config file, a new one is generated
		 */
		Identifier id = null;
		try {
			//we first check to see if the sensor exists in the sensor configuration file
			id =conf.findSensor(n);
			logger.debug("Found the sid "+id.getName()+" in sensor config file");
		} catch (Exception e) {
			//we havent found a matching sensor in the sensor config file, so we are going to generate a new ID
			id = new SensorID(generateNewSensorID());
			logger.debug("created a new sensor id "+id.getName());
		}
		
		//Now we insert/update the newly created/found ID into the XML node n
		try {
			//try to set the value for the sid attribute...
			XMLhelper.setAttributeFromName("//" + SMLConstants.SENSOR_TAG, SMLConstants.SENSOR_ID_ATTRIBUTE_NODE, id.getName(), n);
		} catch (Exception e) {
			//if we re here, there was no existing sid attribute so we have to create one
			try {XMLhelper.addAttribute(XMLhelper.getNode("//"+SMLConstants.SENSOR_TAG, n, false), SMLConstants.SENSOR_ID_ATTRIBUTE_NODE , id.getName());}
			catch (Exception e1) {logger.error("Error setting the SID in the sensor XML node");e1.printStackTrace();}
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
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#getComponentType(org.w3c.dom.Node)
	 */
	@Override
	protected String getComponentType(Node n) throws ParseException {
		return Sensor.SENSOR_TYPE;
	}
	
	/**
	 * This method returns an SMLDescriptions object for all currently-active sensors 
	 * @return an SMLDescriptions object for all currently-active sensors
	 */
	public SMLDescriptions listActiveSensors(){
		Sensor s;
		Integer sid;
		Map<Integer, SMLDescription> m = new Hashtable<Integer, SMLDescription>();
		try {
			synchronized(ctable){
				Iterator<Sensor> i = ctable.values().iterator();
				while(i.hasNext()) {
					s = i.next();
					sid = new Integer(s.getID().getName());
					if(!s.isDisconnected())
						m.put(sid, new SMLDescription(sid, s.getConfig()));
				}	
			}
	    } catch (ConfigurationException e) {
	    	logger.error("we shouldnt be here !!");
	    	e.printStackTrace();
		}
		return new SMLDescriptions(m);
	}
	
	/**
	 * This method returns an SMLDescriptions object for all  sensors 
	 * @return an SMLDescriptions object for all sensors
	 */
	public SMLDescriptions listSensors(){
		Sensor s;
		Integer sid;
		Map<Integer, SMLDescription> m = new Hashtable<Integer, SMLDescription>();
		try {
			synchronized(ctable){
				Iterator<Sensor> i = ctable.values().iterator();
				while(i.hasNext()) {
					s = i.next();
					sid = new Integer(s.getID().getName());
					m.put(sid, new SMLDescription(sid, s.getConfig()));
				}	
			}
	    } catch (ConfigurationException e) {
	    	logger.error("we shouldnt be here !!");
	    	e.printStackTrace();
		}
		return new SMLDescriptions(m);
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
		Node n;
		Iterator<Node> iter = conf.listSensors(pid).iterator();
		while(iter.hasNext()) { n = iter.next(); logger.debug("Creating sensor "+XMLhelper.toString(n));createComponent(n);};
	}
	
	/**
	 * this method generates a partial SML doc using the newly detected sensor's
	 * native address. The SML document is partial because it may or may not contains the 
	 * sensor ID if sid is null(in which case the sid attribue is omitted from the Sensor tag).
	 * @param sid the sensor id (can be null)
	 * @param nativeAddress the newly detected sensor's native addres
	 * @param pid the protocol id associated with this sensor
	 * @return a string which is the SML doc for this new sensor  
	 * @throws ParserConfigurationException If the document can not be created
	 */
	public Document generateSensorConfig(String sid, String nativeAddress, ProtocolID pid, String type) throws ParserConfigurationException{
		StringBuffer xml = new StringBuffer();
		if (sid==null)
			xml.append("<Sensor>\n");
		else
			xml.append("<Sensor "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\""+sid+"\">\n");
		xml.append("\t<parameters>\n");
		xml.append("\t\t<Param name=\""+SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" value=\""+pid.getName()+"\" />\n");
		xml.append("\t\t<Param name=\""+SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"\" value=\""+type+"\" />\n");
		xml.append("\t\t<Param name=\""+SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE+"\" value=\""+nativeAddress+"\" />\n");
		xml.append("\t</parameters>\n");
		xml.append("</Sensor>\n");
			
		return XMLhelper.createDocument(xml.toString());
	}
	
	/**
	 * Returns the first available unused sensor ID
	 * Must hold the lock to ctable
	 * @return the first available unused sensor ID
	 */
	private String generateNewSensorID() {
		//Get the SIDs existing in sensor config file
		List<String> sids = conf.listSensorID();
		
		if(sids.size()==0)
			return "1";
		
		int[] arr = new int[sids.size()];
		Iterator<String> iter = sids.iterator();
		int i=0;
		while(iter.hasNext()){
			arr[i++] = Integer.parseInt(iter.next());
		}
		Arrays.sort(arr);
		if(arr[0]==1) {
			for (i = 1; i < arr.length; i++)
				if(arr[i]>(arr[i-1]+1))
					break;
			i=arr[i-1]+1;
		} else i=1;
		
		logger.debug("Created new sensor id:"+i);
		
		return String.valueOf(i);
	}
}
