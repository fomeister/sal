/**
 * 
 */
package jcu.sal.Config;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author gilles
 * Makes configuration documents available to SAL components
 */
public class ConfigService{
	
	public static String PLATFORMCONFIG_TAG = "PlatformConfiguration";
	public static String SENSORCONFIG_TAG = "SensorConfiguration";
	
	private Document platformCC, sensorconfig;
	private Vector<Node> cprotocol, csensor;
	
	private Logger logger = Logger.getLogger(ConfigService.class);
	private static ConfigService c =  new ConfigService();
	
	public static ConfigService getService() { return c; }
	
	private ConfigService() {
		Slog.setupLogger(this.logger);
		cprotocol = new Vector<Node>();
		csensor = new Vector<Node>();
	}
	
	public void init(String p, String s) throws ConfigurationException{
		NodeList nl = null;
		try {
			platformCC = XMLhelper.createDocument(new File(p));
			sensorconfig = XMLhelper.createDocument(new File(s));
			
			nl = XMLhelper.getNodeList("//" + Protocol.PROTOCOL_TAG, platformCC);
			for(int i=0; i<nl.getLength(); i++)
				cprotocol.add(XMLhelper.duplicateNode(nl.item(i)));
			
			nl = XMLhelper.getNodeList("//" + Sensor.SENSOR_TAG, sensorconfig);
			for(int i=0; i<nl.getLength(); i++)
				csensor.add(XMLhelper.duplicateNode(nl.item(i)));
			
		} catch (ParserConfigurationException e) {
			logger.error("Could not parse the XML configuration file");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (IOException e) {
			logger.error("Could not find configuration file: " + e.getMessage());
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (XPathExpressionException e) {
			logger.error("Could not parse the XML configuration file");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	public Document getPlatformCC() {
		Document d = null;
		try {
			d = XMLhelper.getSubDocument("//" + PLATFORMCONFIG_TAG, platformCC);
			
		} catch (XPathExpressionException e) {
			logger.error("error parsing the document");
			e.printStackTrace();
		}
		return d;
	}
	
	public Document getSensorConfig() {
		Document d = null;
		try {
			d = XMLhelper.getSubDocument("//" + SENSORCONFIG_TAG, sensorconfig);
		} catch (XPathExpressionException e) {
			logger.error("error parsing the document");
			e.printStackTrace();
		}
		return d;
	}
	
	/**
	 * This method returns a copy of all protocol configuration nodes as found in the configuration section
	 * of the PCML document. 
	 * @return a node enumeration
	 */
	public Enumeration<Node> getProtocolNodes() {
		return cprotocol.elements();
	}


	 /** This method returns a copy of all sensor configuration nodes as found in the sensor configuration document
	 * @return a node enumeration
	 */
	public Enumeration<Node> getSensorNodes() {
		return csensor.elements();
	}
	
	public static void main(String[] args) throws ConfigurationException {
		ConfigService e = ConfigService.getService();
		e.init("/home/gilles/workspace/SALv1/src/sensors.xml", "/home/gilles/workspace/SALv1/src/sensors.xml");
		System.out.println(XMLhelper.toString(e.getSensorConfig()));
		System.out.println(XMLhelper.toString(e.getPlatformCC()));
	}
	
	
}
