/**
 * 
 */
package jcu.sal.Managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author gilles
 * 
 */
public class SensorManager extends ManagerFactory<Sensor> {
	
	private static SensorManager s = new SensorManager();
	private Logger logger = Logger.getLogger(SensorManager.class);
	
	
	/**
	 * Private constructor
	 */
	private SensorManager() {
		super();
		Slog.setupLogger(this.logger);
	}
	
	/**
	 * Returns the instance of the SensorManager 
	 * @return
	 */
	public static SensorManager getSensorManager() {
		return s;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected Sensor build(Document doc) throws InstantiationException {
		Sensor sensor = null;
		this.logger.debug("building Sensor");
		try {
			String type = this.getComponentType(doc);
			SensorID i = (SensorID) this.getComponentID(doc);
			this.logger.debug("Component type: " +type);

			sensor = new Sensor(i, type, getComponentConfig(doc));
			
		} catch (ParseException e) {
			this.logger.error("Error while parsing the DOM document. XML doc:");
			this.logger.error(XMLhelper.toString(doc));
			e.printStackTrace();
			throw new InstantiationException();
		} 
		return sensor;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentConfig(org.w3c.dom.Document)
	 */
	@Override
	protected Hashtable<String, String> getComponentConfig(Document doc) throws ParseException {
		ArrayList<String> xml = null;
		Hashtable<String, String> config = new Hashtable<String, String>();
		String name = null, value = null;
		
		try {
			xml = XMLhelper.getAttributeListFromElements("//" + Sensor.SENSORPARAM_TAG, doc);
		} catch (XPathExpressionException e) {
			this.logger.error("Cannot find parameters for this Sensor");
			throw new ParseException("Cannot find parameters for this Sensor", 0);
		}
		
		Iterator<String> iter = xml.iterator();
		
		while(iter.hasNext()) {
			iter.next();
			name = iter.next();
			iter.next();
			value = iter.next();
			config.put(name,value);
		}
		
		return config;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentType(org.w3c.dom.Document)
	 */
	@Override
	protected String getComponentType(Document doc) throws ParseException{
		return new String(Sensor.SENSOR_TYPE);
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(Document doc) throws ParseException {
		Identifier id = null;
		try {
			id = new SensorID(XMLhelper.getAttributeFromName("//" + Sensor.SENSOR_TAG, Sensor.SENSORID_TAG, doc));
		} catch (XPathExpressionException e) {
			this.logger.error("Couldnt find the Sensor id");
			e.printStackTrace();
			throw new ParseException("Couldnt create the Sensor identifier", 0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(Sensor component) {
		component.stop();
		component.remove();
	}

	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, BadAttributeValueExpException {
		SensorManager e = getSensorManager();
		Sensor n = e.createComponent(XMLhelper.createDocument("<Sensor sid='6'><parameters><Param name='LogicalPortID' value='IDU' /><Param name='Address' value='1.3.6.1.4.1.10132.7.1.4.7.0' /><Param name='SamplingInterval' value='10' /></parameters></Sensor>"));
		n.dumpConfig();
		e.createComponent(XMLhelper.createDocument("<Sensor sid='6'><parameters><Param name='LogicalPortID' value='IDU' /><Param name='Address' value='1.3.6.1.4.1.10132.7.1.4.7.0' /><Param name='SamplingInterval' value='10' /></parameters></Sensor>"));
		Sensor o = e.createComponent(XMLhelper.createDocument("<Sensor sid='7'><parameters><Param name='LogicalPortID' value='PL40' /><Param name='Address' value='1.3.6.1.4.1.10132.7.1.4.7.0' /><Param name='SamplingInterval' value='10' /></parameters></Sensor>"));
		o.dumpConfig();
		e.destroyComponent(new SensorID("eth01"));
		e.destroyComponent(new SensorID("6"));
		e.destroyComponent(new SensorID("usb2"));
		e.destroyComponent(new SensorID("7"));
		e.destroyComponent(new SensorID("eth0"));
		e.destroyComponent(new SensorID("files"));
	}
}
