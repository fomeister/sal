/**
 * 
 */
package jcu.sal.Config;

import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * @author gilles
 * Makes configuration documents available to SAL components
 */
public class ConfigService {
	
	public static String PLATFORMCONFIG_TAG = "PlatformConfiguration";
	public static String SENSORCONFIG_TAG = "SensorConfiguration";
	
	private Document platformconfig = null, sensorconfig = null;  
	
	private Logger logger = Logger.getLogger(ConfigService.class);
	private static ConfigService c =  new ConfigService();
	
	public static ConfigService getService() { return c; }
	
	private ConfigService() {
		Slog.setupLogger(this.logger);
	}
	
	public void init(String platformConfigFile, String sensorConfigFile) throws ConfigurationException{
		try {
			platformconfig = XMLhelper.createDocument(new File(platformConfigFile));
			sensorconfig = XMLhelper.createDocument(new File(sensorConfigFile));
		} catch (ParserConfigurationException e) {
			logger.error("Could not parse the XML configuration file");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (IOException e) {
			logger.error("Could not find configuration file: " + e.getMessage());
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	public Document getPlatformConfig() {
		Document d = null;
		try {
			d = XMLhelper.getSubDocument("//" + PLATFORMCONFIG_TAG, platformconfig);
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
	
	public static void main(String[] args) throws ConfigurationException {
		ConfigService e = ConfigService.getService();
		e.init("/home/gilles/workspace/SALv1/src/sensors.xml", "/home/gilles/workspace/SALv1/src/sensors.xml");
		System.out.println(XMLhelper.toString(e.getSensorConfig()));
		System.out.println(XMLhelper.toString(e.getPlatformConfig()));
	}
	
	
}
