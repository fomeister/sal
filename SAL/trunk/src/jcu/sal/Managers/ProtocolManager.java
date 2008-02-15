/**
 * 
 */
package jcu.sal.Managers;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Components.Sensors.Sensor;
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

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected Protocol build(Node config) throws InstantiationException {
		Protocol p = null;
		this.logger.debug("building Protocol");
		try {
			ProtocolID i = (ProtocolID) getComponentID(config);
			this.logger.debug("Protocol type: " + i.getType());
			String className = ProtocolModulesList.getClassName(i.getType());

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
		String type = null;
		try {
			type = XMLhelper.getAttributeFromName("//" + Protocol.PROTOCOL_TAG, Protocol.PROTOCOLTYPE_TAG, n);
			id = new ProtocolID(XMLhelper.getAttributeFromName("//" + Protocol.PROTOCOL_TAG, Protocol.PROTOCOLNAME_TAG, n), type);
		} catch (Exception e) {
			this.logger.error("Couldnt create the Protocol identifier");
			e.printStackTrace();
			throw new ParseException("Couldnt create the Protocol identifier", 0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(Protocol component) {
		component.remove();
	}
	

	/**
	 * Starts all the components at once
	 * @return the config directives in a hastable
	 */
	public void startAll(){
		Collection<Protocol> cvalues = ctable.values();
		Iterator<Protocol> iter = cvalues.iterator();
		while (iter.hasNext()) {
			Protocol e = iter.next();
			logger.debug("Starting protocol" + e.toString());
			try { e.start(); }
			catch (ConfigurationException ex) { logger.error("Couldnt start protocol " + e.toString()); }
		}
	}
	
	/**
	 * Adds a sensor to the appropriate protocol. Checks if this sensor is supported by the procotocl
	 * @return the protocol to which the sensor has been added 
	 * @throws ConfigurationException if the sensor cannot be added (wrong ProtocolName field, or unsupported sensor)
	 */
	public Protocol addSensor(Sensor sensor) throws ConfigurationException{
		Protocol p = ctable.get(new ProtocolID(sensor.getProtocolName(), Identifier.ANY_TYPE));
		if(p!=null)
		{
			logger.debug("Adding sensor " + sensor.toString() + " to Protocol " + p.getID().toString());
			p.addSensor(sensor);
			return p;
		}
		/* if we get here the sensor couldnt be added */
		logger.error("The sensor " + sensor.getID().toString() + " couldnt be added because ");
		logger.error("no protocol named " + sensor.getProtocolName() +" could be found");
		throw new ConfigurationException();
	}

	
	public static void main(String[] args)  {
		ProtocolManager e = getProcotolManager();
		Protocol p = null;
		try {
			p = e.createComponent(XMLhelper.createDocument("<Protocol name='1wirefs' type='owfs'><EndPoint name='usb' type='usb' /><parameters><Param name='Location' value='/opt/owfs/bin/owfs' /><Param name='MountPoint' value='/mnt/w1' /></parameters></Protocol>"));
		} catch (ParserConfigurationException ex) {
			System.out.println("Cannot parse Endpoint / PRotocol configuration");
		} catch (ConfigurationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		if(p!=null)
			e.destroyComponent(p.getID());
	}
}
