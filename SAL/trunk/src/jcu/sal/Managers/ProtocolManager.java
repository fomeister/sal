/**
 * 
 */
package jcu.sal.Managers;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Command;
import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Components.Sensors.Sensor;
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
	
	
	/**
	 * Private constructor
	 */
	private ProtocolManager() {
		super();
		Slog.setupLogger(this.logger);
	}
	
	/**
	 * Returns the instance of the EndPointManager 
	 * @return
	 */
	public static ProtocolManager getProcotolManager() {
		return p;
	}
	
	/**
	 * Creates all protocols, associated endpoints and sensors given SML and PCML
	 * @throws ConfigurationException if there is a problem parsing the XML files
	 */
	public void init(String sml, String pcml) throws ConfigurationException {
		Sensor s = null;
		ConfigService conf = ConfigService.getService();
		SensorManager sm = SensorManager.getSensorManager();
		
		try {
			conf.init(pcml,sml);
			Iterator<Node> iter = conf.getProtocolIterator();
			while(iter.hasNext()) {
				createComponent(iter.next());
			}
		} catch (ConfigurationException e) {
			logger.error("Could not read/parse the configuration files.");
			throw e;
		} 
		
		Iterator<Node> iter = conf.getSensorIterator();
		while(iter.hasNext()) {
			try {
				associateSensor(createSensor(iter.next()));
			} catch (ConfigurationException e) {
				logger.error("Could not add the sensor to any protocols");
				if(s!=null) sm.destroyComponent(s);
			}
		} 
		
		startAll();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected Protocol build(Node config) throws InstantiationException {
		Protocol p = null;
		String type=null;
		this.logger.debug("building Protocol");
		try {
			ProtocolID i = (ProtocolID) getComponentID(config);
			type=getComponentType(config);
			this.logger.debug("Protocol type: " + type);
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
			
			this.logger.debug("done building protocol "+p.toString());
			
		} catch (ParseException e) {
			this.logger.error("Error while parsing the DOM document. XML doc:");
			this.logger.error(XMLhelper.toString(config));
			//e.printStackTrace();
			throw new InstantiationException();
		}catch (Exception e) {
			this.logger.error("Error in new Protocol instanciation. XML doc:");
			 this.logger.error(XMLhelper.toString(config));
			//e.printStackTrace();
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
		component.remove(this);
	}
	
	/**
	 * Creates a sensor from a XML node
	 * @throws ConfigurationException if the XML node is incorrect
	 */
	public Sensor createSensor(Node n) throws ConfigurationException{
		return SensorManager.getSensorManager().createComponent(n);
	}

	/**
	 * Deletes a sensor
	 */
	private void deleteSensor(Sensor s){
		SensorManager.getSensorManager().destroyComponent(s);
	}
	
	/**
	 * Deletes a sensor
	 */
	private void deleteSensor(SensorID i){
		SensorManager.getSensorManager().destroyComponent(i);
	}
	

	/**
	 * Starts all the protcols  at once
	 */
	public void startAll(){
		Collection<Protocol> cvalues = ctable.values();
		Iterator<Protocol> iter = cvalues.iterator();
		while (iter.hasNext()) {
			Protocol e = iter.next();
			logger.debug("Starting protocol" + e.toString());
			try { e.start(); }
			catch (ConfigurationException ex) { 
				logger.error("Couldnt start protocol " + e.toString()+" removing it");
				destroyComponent(e);
			}
		}
	}
	
	/**
	 * Stops all the protcols  at once
	 */
	public void stopAll(){
		Collection<Protocol> cvalues = ctable.values();
		Iterator<Protocol> iter = cvalues.iterator();
		while (iter.hasNext()) {
			Protocol e = iter.next();
			logger.debug("Stopping protocol" + e.toString());
			e.stop();
		}
	}
	
	/**
	 * remove all the protcols at once
	 */
	public void removeAll(){
		Collection<Protocol> cvalues = ctable.values();
		Iterator<Protocol> iter = cvalues.iterator();
		while (iter.hasNext()) {
			Protocol e = iter.next();
			logger.debug("Removing protocol" + e.toString());
			e.remove(this);
		}
	}
	
	/**
	 * Adds a sensor to the appropriate protocol. Checks if this sensor is supported by the procotocl
	 * @return the protocol to which the sensor has been added 
	 * @throws ConfigurationException if the sensor cannot be added (wrong ProtocolName field, or unsupported sensor)
	 */
	public Protocol associateSensor(Sensor sensor) throws ConfigurationException{
		Protocol p = ctable.get(new ProtocolID(sensor.getProtocolName()));
		if(p!=null)
		{
			logger.debug("Adding sensor " + sensor.toString() + " to Protocol " + p.getID().toString());
			p.associateSensor(sensor);
			return p;
		}
		/* if we get here the sensor couldnt be added */
		logger.error("The sensor " + sensor.getID().toString() + " couldnt be added because ");
		logger.error("no protocol named " + sensor.getProtocolName() +" could be found");
		throw new ConfigurationException();
	}
	
	/**
	 * Unassociates a sensor from the appropriate protocol
	 * The sensor is then destroyed
	 * @args i the Id of the sensor to be removed   
	 */
	public void removeSensor(SensorID i){
		Protocol p = ctable.get(new ProtocolID(i.getPIDName()));
		if(p!=null)
		{
			logger.debug("Removing sensor " + i.toString() + " from Protocol " + p.getID().toString());
			if(p.unassociateSensor(i)) {
				deleteSensor(i);
				logger.debug("sensor removed");
			}
		} else {
			/* if we get here the sensor couldnt be removed*/
			logger.error("The sensor " + i.toString() + " couldnt be removed because ");
			logger.error("no protocol named " + i.getPIDName() +" could be found");
		}
	}
	
	/**
	 * Unassociates all sensor from the appropriate protocol.
	 * The sensors are then destroyed
	 * @args p the protocol whose sensors are to be removed   
	 */
	public void removeSensors(Protocol p){
		ArrayList<Sensor> ss = p.unassociateSensors();
		for (int i = 0; i < ss.size(); i++) {
			logger.debug("Removing sensor " + ss.get(i).toString() );
			deleteSensor(ss.get(i));
			logger.debug("sensor removed");
		}
	}
	
	/**
	 * Unassociates a sensor from the appropriate protocol.
	 * The sensor is then destroyed
	 * @args s the sensor to be removed 
	 */
	public void removeSensor(Sensor s){
		removeSensor(s.getID());
	}
	
	/**
	 * Prints sensors associated with all procotols 
	 */
	public void dumpSensors(){
		Iterator<Protocol> i = getIterator();
		Protocol p;
		while(i.hasNext()) {
			p = i.next();
			logger.debug("Sensors associated with protocol "+p.toString());
			p.dumpSensorsTable();
		}
	}
	
	/**
	 * Prints sensors associated with a specific procotol 
	 */
	public void dumpSensor(Protocol p){
		p.dumpSensorsTable();
	}
	
	public String execute(Command c, SensorID s) throws ConfigurationException, BadAttributeValueExpException {
		return getProtocol(s).execute(c, s);
	}
	

	/**
	 * Returns the protcol associated with a SensorID
	 * @throw ConfigurationException if the protocol can not be found 
	 */
	private Protocol getProtocol(SensorID sid) throws ConfigurationException{
			Protocol p=null;
			ProtocolID pid = null;

			//TODO fix all the methods that should return an exception instead of a null pointer
			//TODO so we can get rid of all the if statments and only have try/catch stuff
			if(SensorManager.getSensorManager().getComponent(sid)==null) {
				logger.error("Cannot find the any sensor with this sensorID: " + sid.toString());
				throw new ConfigurationException();
			}
			pid = sid.getPid();
			if(pid==null){
				logger.error("Cannot find the protocolID associated with this sensorID: " + sid.toString());
				throw new ConfigurationException();
			}
			p=ProtocolManager.getProcotolManager().getComponent(pid);
			if(p==null){
				logger.error("Cannot find the protocol associated with this sensorID: " + sid.toString());
				throw new ConfigurationException();
			}
			return p;
	}
	
	/**
	 * Creates a sensor from a partial SML (passed as a string)
	 */
	public Sensor createSensorFromPartialSML(String s){
			return SensorManager.getSensorManager().createSensorFromPartialSML(s);	}
}
