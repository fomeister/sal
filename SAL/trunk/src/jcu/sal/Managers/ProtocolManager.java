/**
 * 
 */
package jcu.sal.Managers;

import java.io.NotActiveException;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Command;
import jcu.sal.Components.Identifier;
import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Components.Protocols.ProtocolID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.Components.Sensors.SensorID;
import jcu.sal.Config.ConfigService;
import jcu.sal.utils.ProtocolModulesList;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @author gilles
 * 
 */
public class ProtocolManager extends ManagerFactory<Protocol> {
	
	private static ProtocolManager p = new ProtocolManager();
	private Logger logger = Logger.getLogger(ProtocolManager.class);
	private ConfigService conf;
	
	
	/**
	 * Private constructor
	 */
	private ProtocolManager() {
		super();
		Slog.setupLogger(this.logger);
		conf = ConfigService.getService();
	}
	
	/**
	 * Returns the instance of the ProtocolManager 
	 * @return
	 */
	public static ProtocolManager getProcotolManager() {
		return p;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected Protocol build(Node config) throws InstantiationException {
		Protocol p = null;
		String type=null;
		logger.debug("building Protocol");
		try {
			ProtocolID i = (ProtocolID) getComponentID(config);
			type=getComponentType(config);
			logger.debug("Protocol type: " + type);
			String className = ProtocolModulesList.getClassName(type);

			Class<?>[] params = {ProtocolID.class, Hashtable.class, Node.class};
			Constructor<?> c = Class.forName(className).getConstructor(params);
			Object[] o = new Object[3];
			o[0] = i;
			o[1] = getComponentConfig(config);
			logger.debug("Protocol config: " + XMLhelper.toString(config));
			o[2] = XMLhelper.getNode("/" + Protocol.PROTOCOL_TAG + "/" + EndPoint.ENPOINT_TAG, config, true);
			logger.debug("EndPoint config: " + XMLhelper.toString((Node) o[2])); 
			p = (Protocol) c.newInstance(o);

			logger.debug("done building protocol "+p.toString());
			
		} catch (ParseException e) {
			logger.error("Error while parsing the DOM document. XML doc:");
			logger.error(XMLhelper.toString(config));
			//e.printStackTrace();
			throw new InstantiationException();
		}catch (Exception e) {
			logger.error("Error in new Protocol instanciation.");
			logger.error("Exception: "+e.getClass()+" - "+e.getMessage() );
			logger.error("caused by: "+e.getCause().getClass()+" - "+e.getCause().getMessage());
			logger.error("XML doc:\n");
			logger.error(XMLhelper.toString(config));
			e.printStackTrace();
			throw new InstantiationException();
		}
		return p;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(Node n) throws ParseException {
		Identifier id = null;
		try {
			id = new ProtocolID(XMLhelper.getAttributeFromName("//" + Protocol.PROTOCOL_TAG, Protocol.PROTOCOLNAME_TAG, n));
		} catch (Exception e) {
			throw new ParseException("Couldnt find the Protocol identifier", 0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentType(org.w3c.dom.Document)
	 */
	@Override
	protected String getComponentType(Node n) throws ParseException {
		String type = null;
		try {
			type = XMLhelper.getAttributeFromName("//" + Protocol.PROTOCOL_TAG, Protocol.PROTOCOLTYPE_TAG, n);
		} catch (Exception e) {
			throw new ParseException("Couldnt find the protocol type", 0);
		}
		return type;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(Protocol component) {
		ProtocolID pid=component.getID();
		logger.debug("Removing protocol " + pid.toString());
		component.remove(this);
		SensorManager.getSensorManager().destroyComponents(component.getSensors());
		componentRemovable(pid);
	}
	
	/*
	 * 
	 *  START OF SALAgent API methods
	 * 
	 */

	/**
	 * Creates all protocols, associated endpoints and sensors given SML and PCML
	 * @throws ConfigurationException if there is a problem parsing the XML files
	 */
	public void init(String sml, String pcml) throws ConfigurationException {
		try {
			conf.init(pcml,sml);
			Enumeration<Node> iter = conf.getProtocolNodes();
			while(iter.hasMoreElements()) {
				createComponent(iter.nextElement());
			}
		} catch (ConfigurationException e) {
			logger.error("Could not read/parse the configuration files.");
			throw e;
		} 
		
		Enumeration<Node> iter = conf.getSensorNodes();
		while(iter.hasMoreElements()) {
			try {
				SensorManager.getSensorManager().createComponent(iter.nextElement());
			} catch (ConfigurationException e) {
				logger.error("Could not create the sensor");
			}
		} 
		
		startAll();
	}

	/**
	 * Starts all the protcols  at once
	 */
	public void startAll(){
		synchronized(this){
			Iterator<Protocol> iter = getIterator();
			while (iter.hasNext()) {
				Protocol e = iter.next();
				//logger.debug("Starting protocol" + e.toString());
				try { e.start(); }
				catch (ConfigurationException ex) { 
					logger.error("Couldnt start protocol " + e.toString()+"...");
					//destroyComponent(e.getID());
				}
			}
		}
	}
	
	/**
	 * Stops all the protcols  at once
	 */
	public void stopAll(){
		synchronized(this){
			Iterator<Protocol> iter = getIterator();
			while (iter.hasNext()) {
				Protocol e = iter.next();
				logger.debug("Stopping protocol" + e.toString());
				e.stop();
			}
		}
	}
	
	/**
	 * remove all the protcols at once
	 */
	public void removeAll(){
		Protocol p;
		synchronized(this){
			Enumeration<Identifier> e = getKeys();
			while (e.hasMoreElements())
				if((p=getComponent(e.nextElement()))!=null) remove(p);
		}
	}
	
	/**
	 * Sends a command to a sensor
	 * @param c the command
	 * @param sid the sensor
	 * @return the result
	 * @throws ConfigurationException if the sensor isnt associated with any protocol
	 * @throws BadAttributeValueExpException if the command cannot be parsed/is incorrect
	 * @throws NotActiveException if the sensor is not available to run commands
	 */
	public String execute(Command c, SensorID sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException {
		return getProtocol(sid).execute(c, sid);
	}
	
	/**
	 * Retrieves the CML doc for a given sensor
	 * @param sid the sensorID
	 * @return the CML document
	 * @throws ConfigurationException if the sensor isnt associated with a protocol
	 * @throws NotActiveException
	 */
	public String getCML(SensorID sid) throws ConfigurationException, NotActiveException {
		return getProtocol(sid).getCML(sid);
	}
		
	/**
	 * Adds a sensor to the appropriate protocol. Checks if this sensor is supported by the protocol
	 * @return the protocol to which the sensor has been added 
	 * @throws ConfigurationException if the sensor cannot be added (wrong ProtocolName field, or unsupported sensor)
	 */
	Protocol associateSensor(Sensor sensor) throws ConfigurationException{
		Protocol p = null;
		String pname = null;
		try {
			pname = sensor.getConfig(Sensor.PROTOCOLATTRIBUTE_TAG);
			if((p = getComponent(new ProtocolID(pname)))!=null) {
					p.associateSensor(sensor);
			} else
				throw new ConfigurationException("Cant find "+pname);
		} catch (BadAttributeValueExpException e) {
			logger.error("Can not find the protocol name to associate the sensor with");
			logger.error("Cant associate sensor " + sensor.getID().toString() + "(Cant find protocol " + pname+")");
			throw new ConfigurationException("cant find protocol from sensor config");
		} catch (ConfigurationException e) {
			throw e;
		}

		return p;
	}
	
	/**
	 * Unassociate a sensor from the protocol.
	 * @throws ConfigurationException if the sensor cannot be unassociated (wrong ProtocolName field, or unsupported sensor)
	 */
	void unassociateSensor(Sensor s) throws ConfigurationException{
		Protocol p = null;
		String pname = null;
		try {
			pname = s.getConfig(Sensor.PROTOCOLATTRIBUTE_TAG);
			if((p = getComponent(new ProtocolID(pname)))!=null)
				p.unassociateSensor(s.getID());
			else
				throw new ConfigurationException("Cant find "+pname);
		} catch (BadAttributeValueExpException e) {
			logger.error("Can not find the protocol name to unassociate the sensor from");
			logger.error("Cant unassociate sensor " + s.getID().toString() + "(Cant find protocol " + pname+")");
			throw new ConfigurationException("cant find protocol from sensor config");
		} catch (ConfigurationException e) {
			logger.error("Can not find the protocol name to unassociate the sensor from");
			logger.error("Cant unassociate sensor " + s.getID().toString() + "(Cant find protocol " + pname+")");
			throw e;
		}
	}	
	
	/**
	 * Returns the protcol associated with a SensorID (assuming it is already associated with one)
	 * @throws ConfigurationException if the protocol can not be found
	 * @throws NotActiveException if the sensor can not be foundx 
	 */
	Protocol getProtocol(SensorID sid) throws NotActiveException, ConfigurationException{
			Protocol p=null;
			String pName = null;
			Sensor s;

			//TODO fix all the methods that should return an exception instead of a null pointer
			//TODO so we can get rid of all the if statments and only have try/catch stuff.
			//TODO fix the Identifier issue: with a sensor ID (a single int), to get the protocol associated
			//TODO with it, we have to do the following 4 lines ... ugly !
			if((s=SensorManager.getSensorManager().getComponent(sid))==null) {
				//logger.error("Cannot find the any sensor with this sensorID: " + sid.toString());
				throw new NotActiveException();
			}
			if((pName = s.getID().getPIDName())==null){
				logger.error("Cannot find the protocolID associated with this sensorID: " + sid.toString());
				throw new ConfigurationException();
			}
			if((p=getComponent(new ProtocolID(pName)))==null){
				logger.error("Cannot find the protocol associated with this sensorID: " + sid.toString());
				throw new ConfigurationException();
			}

			return p;
	}

}
