/**
 * 
 */
package jcu.sal.Managers;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
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
	private ConfigService conf;
	private SensorManager sm;
	
	
	/**
	 * Private constructor
	 */
	private ProtocolManager() {
		super();
		Slog.setupLogger(this.logger);
		conf = ConfigService.getService();
		sm = SensorManager.getSensorManager();
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
			
			sm.start();
			this.logger.debug("done building protocol "+p.toString());
			
		} catch (ParseException e) {
			this.logger.error("Error while parsing the DOM document. XML doc:");
			this.logger.error(XMLhelper.toString(config));
			//e.printStackTrace();
			throw new InstantiationException();
		}catch (Exception e) {
			this.logger.error("Error in new Protocol instanciation.");
			this.logger.error("Exception: "+e.getClass()+" - "+e.getMessage() );
			this.logger.error("caused by: "+e.getCause().getClass()+" - "+e.getCause().getMessage());
			this.logger.error("XML doc:\n");
			this.logger.error(XMLhelper.toString(config));
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
		logger.debug("Removing protocol " + component.toString());
		component.remove(this);
		if(getSize()==0)
			sm.stop();
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
		Sensor s = null;
		
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
				s = createSensor(iter.next());
				associateSensor(s);
			} catch (ConfigurationException e) {
				logger.error("Could not add the sensor to any protocols");
				if(s!=null) sm.destroyComponent(s.getID());
			}
		} 
		
		startAll();
	}
	
	/**
	 * Creates a sensor from a XML node
	 * @throws ConfigurationException if the XML node is incorrect
	 */
	private Sensor createSensor(Node n) throws ConfigurationException{
		return sm.createComponent(n);
	}

	/**
	 * Deletes a sensor
	 */
	private void deleteSensor(Sensor s){
		sm.destroyComponent(s.getID());
	}
	
	/**
	 * Deletes a sensor
	 */
	private void deleteSensor(SensorID i){
		sm.destroyComponent(i);
	}
	

	/**
	 * Starts all the protcols  at once
	 */
	public void startAll(){
		synchronized(this){
			Iterator<Protocol> iter = getIterator();
			while (iter.hasNext()) {
				Protocol e = iter.next();
				logger.debug("Starting protocol" + e.toString());
				try { e.start(); }
				catch (ConfigurationException ex) { 
					logger.error("Couldnt start protocol " + e.toString()+" removing it");
					destroyComponent(e.getID());
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
	 * Adds a sensor to the appropriate protocol. Checks if this sensor is supported by the procotocl
	 * @return the protocol to which the sensor has been added 
	 * @throws ConfigurationException if the sensor cannot be added (wrong ProtocolName field, or unsupported sensor)
	 */
	private Protocol associateSensor(Sensor sensor) throws ConfigurationException{
		synchronized(this) {
			Protocol p = getComponent(new ProtocolID(sensor.getProtocolName()));
			if(p!=null)
			{
				p.associateSensor(sensor);
				logger.debug("Associated sensor " + sensor.toString() + " to Protocol " + p.getID().toString());
				return p;
			}
		}
		/* if we get here the sensor couldnt be added */
		logger.error("Cant associate sensor " + sensor.getID().toString() + "(Cant find protocol " + sensor.getProtocolName()+")");
		throw new ConfigurationException();
	}
	
	/**
	 * Unassociates a sensor from the appropriate protocol
	 * The sensor is then destroyed
	 * @args i the Id of the sensor to be removed   
	 */
	public void removeSensor(SensorID i){
		synchronized(this) {
			Protocol p = getComponent(new ProtocolID(i.getPIDName()));
			logger.debug("Removing sensor " + i.toString() + " from Protocol " + p.getID().toString());
			if(p!=null)
				p.unassociateSensor(i);
			deleteSensor(i);
			logger.debug("sensor removed");
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
		synchronized(this) {
			Iterator<Protocol> i = getIterator();
			Protocol p;
			while(i.hasNext()) {
				p = i.next();
				System.out.println("Sensors associated with protocol "+p.toString());
				dumpSensor(p);
			}
		}
	}
	
	/**
	 * Prints sensors associated with a specific procotol 
	 */
	public void dumpSensor(Protocol p){
		p.dumpSensorsTable();
	}
	
	public String execute(Command c, int id) throws ConfigurationException, BadAttributeValueExpException {
		return getProtocol(id).execute(c, new SensorID(String.valueOf(id)));
	}
	

	/**
	 * Returns the protcol associated with a SensorID
	 * @throws ConfigurationException if the protocol can not be found 
	 */
	public Protocol getProtocol(int  id) throws ConfigurationException{
			Protocol p=null;
			ProtocolID pid = null;
			Sensor s;
			SensorID sid = new SensorID(String.valueOf(id));

			//TODO fix all the methods that should return an exception instead of a null pointer
			//TODO so we can get rid of all the if statments and only have try/catch stuff.
			//TODO fix the Identifier issue: with a sensor ID (a single int), to get the protocol associated
			//TODO with it, we have to do the following 4 lines ... ugly !
			if((s=sm.getComponent(sid))==null) {
				logger.error("Cannot find the any sensor with this sensorID: " + sid.toString());
				throw new ConfigurationException();
			}
			pid = s.getID().getPid();
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
	 * @throws ConfigurationException if the sensor couldnt be created or associated with a protocol
	 */
	public Sensor createSensorFromPartialSML(String s) throws ConfigurationException{
		Sensor tmp =  sm.createSensorFromPartialSML(s);
		try {
			associateSensor(tmp);
		} catch (ConfigurationException e) {
			logger.error("the sensor couldnt be associated with a protocol.");
			logger.error("Destroying it");
			sm.destroyComponent(tmp.getID());
			throw e;
		}
		return tmp;
	}
}
