/**
 * 
 */
package jcu.sal.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Components.Protocols.ProtocolID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.Components.Sensors.SensorID;
import jcu.sal.Managers.SensorManager;
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
	private File platformConfigFile, sensorConfigFile;
	
	private Logger logger = Logger.getLogger(ConfigService.class);
	private static ConfigService c =  new ConfigService();
	
	public static ConfigService getService() { return c; }
	
	private ConfigService() {
		Slog.setupLogger(this.logger);
	}
	
	public synchronized void init(String pc, String sc) throws ConfigurationException{
		File p = new File(pc);
		File s = new File(sc);
		if (p.exists()) {
			if (!p.isFile())
				throw new ConfigurationException("Should not be a directory: " + p);

		    if (!p.canWrite())
		    	throw new ConfigurationException("File cannot be written: " + p);
		} else {
			logger.debug("Platform config file " + p.getName() +" does not exist - creating one");
			try {writeDocumentToFile(p, createEmptyPlatformConfig());}
			catch (IOException e) {
				logger.error("Cant create an empty platform config file - " +e.getMessage());
				throw new ConfigurationException();
			}catch (ParserConfigurationException e) { 
				logger.error("Cant generate an empty platform config document - " +e.getMessage());
				throw new ConfigurationException();
			}
		}


		if (s.exists()) {
			if (!s.isFile())
				throw new ConfigurationException("Should not be a directory: " + s);
			
		    if (!s.canWrite())
		    	throw new ConfigurationException("File cannot be written: " + s);
			
		} else {
			logger.debug("Sensor config file " + s.getName() +" does not exist - creating one");
			try {writeDocumentToFile(s, createEmptySensorConfig());}
			catch (IOException e) {
				logger.error("Cant create an empty sensor config file - " +e.getMessage());
				throw new ConfigurationException();
			}catch (ParserConfigurationException e) { 
				logger.error("Cant generate an empty sensor config document - " +e.getMessage());
				throw new ConfigurationException();
			}
		}

		platformConfigFile = p;
		sensorConfigFile = s;
		reloadConfig();
	}
	
	private synchronized void reloadConfig() throws ConfigurationException{
		try {
			platformCC = XMLhelper.createDocument(platformConfigFile);
			sensorconfig = XMLhelper.createDocument(sensorConfigFile);
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
	
	/**
	 * This method add the XML configuration for a new protocol to the protocol config document
	 * It assumes the argument d contains ONLY this new protocol's configuration. If not, use the other
	 * version of {@link #addProtocol(Node) addProtocol}
	 * @param d the document containing the new protocol configuration only
	 * @throws ConfigurationException if the document can not be parsed or saved
	 */
	public synchronized void addProtocol(Document d) throws ConfigurationException{
		addProtocol(d.getFirstChild());
	}
	
	/**
	 * This method add the XML configuration for a new protocol to the protocol config document
	 * @param d the node containing the new protocol configuration
	 * @throws ConfigurationException if the node can not be parsed or saved
	 */
	public synchronized void addProtocol(Node n) throws ConfigurationException{
		try {
			//check if the node we re trying to add already exists
			Node parent = XMLhelper.getNode("//" + Protocol.PROTOCOL_TAG, n, false);
			String name = XMLhelper.getAttributeFromName(Protocol.PROTOCOLNAME_TAG, parent);
			if(XMLhelper.getNode("//"+ Protocol.PROTOCOL_TAG +"[@"+Protocol.PROTOCOLNAME_TAG+"='"+name+"']", platformCC, false)==null) {
				parent = XMLhelper.getNode("//" + Protocol.PROTOCOLSECTION_TAG, platformCC, false);
				XMLhelper.addChild(parent, n);
		        writeDocumentToFile(platformConfigFile,platformCC);
			} //else {
				//logger.debug("A protocol named '"+name+"' already exists");
	    } catch (IOException e) {
	    	logger.error("Cannot write the new protocol to the platformConfig file");
	    	e.printStackTrace();
	    	throw new ConfigurationException();
	    } catch (XPathExpressionException e) {
	    	logger.error("Looks like the platform config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (ParserConfigurationException e) {
			logger.error("Looks like the platform config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method removes the XML configuration for a protocol from the protocol config document
	 * @param pid the Id of the protocol to be remvoed
	 * @throws ConfigurationException if the protocol doesnt exist in the current config file
	 */
	public synchronized void removeProtocol(ProtocolID pid) throws ConfigurationException{
		Node n;
		try {
			if((n=XMLhelper.getNode("//"+ Protocol.PROTOCOL_TAG +"[@"+Protocol.PROTOCOLNAME_TAG+"='"+pid.getName()+"']", platformCC, false))!=null) {
				XMLhelper.deleteNode(n);
		        writeDocumentToFile(platformConfigFile,platformCC);
			} else {
				logger.debug("The protocol named '"+pid.getName()+"' doesnt exist in the config file");
			}
	    } catch (IOException e) {
	    	logger.error("Cannot write the new protocol to the platformConfig file");
	    	e.printStackTrace();
	    	throw new ConfigurationException();
	    } catch (XPathExpressionException e) {
	    	logger.error("Looks like the platform config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (ParserConfigurationException e) {
			logger.error("Looks like the platform config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method add the XML configuration for a new sensor to the sensor config document
	 * It assumes the argument d contains ONLY this new sensor's configuration. If not, use the other
	 * version of {@link #addSensor(Node) addSensor}
	 * @param d the document containing the new sensor configuration only
	 * @throws ConfigurationException if the document can not be parsed or saved
	 */
	public synchronized void addSensor(Document d) throws ConfigurationException{
		addSensor(d.getFirstChild());
	}
	
	/**
	 * This method add the XML configuration for a new sensor to the sensor config document
	 * @param d the node containing the new sensor configuration
	 * @throws ConfigurationException if the node can not be parsed or saved
	 */
	public synchronized void addSensor(Node n) throws ConfigurationException{
		try {
			//check if the node we re trying to add already exists
			Node parent = XMLhelper.getNode("//" + Sensor.SENSOR_TAG, n, false);
			String name = XMLhelper.getAttributeFromName(Sensor.SENSORID_TAG, parent);
			if(XMLhelper.getNode("//"+ Sensor.SENSOR_TAG+"[@"+Sensor.SENSORID_TAG+"='"+name+"']", sensorconfig, false)==null) {
				parent = XMLhelper.getNode("//" + Sensor.SENSORSECTION_TAG, sensorconfig, false);
				XMLhelper.addChild(parent, n);
		        writeDocumentToFile(sensorConfigFile,sensorconfig);
			} //else
				//logger.debug("A sensor named '"+name+"' already exists");
	    } catch (IOException e) {
	    	logger.error("Cannot write the new protocol to the sensorConfig file");
	    	e.printStackTrace();
	    	throw new ConfigurationException();
	    } catch (XPathExpressionException e) {
	    	logger.error("Looks like the sensor config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (ParserConfigurationException e) {
			logger.error("Looks like the sensor config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method removes the XML configuration for a sensor from the sensor config document
	 * @param sid the ID of the sensor to be removed
	 * @throws ConfigurationException if the sensor doesnt exist in the current config file
	 */
	public synchronized void removeSensor(SensorID sid) throws ConfigurationException{
		Node n;
		try {
			if((n=XMLhelper.getNode("//"+ Sensor.SENSOR_TAG+"[@"+Sensor.SENSORID_TAG+"='"+sid.getName()+"']", sensorconfig, false))!=null) {
				XMLhelper.deleteNode(n);
		        writeDocumentToFile(sensorConfigFile,sensorconfig);
			} else {
				logger.debug("The sensor named '"+sid.getName()+"' doesnt exist in the config file");
			}
	    } catch (IOException e) {
	    	logger.error("Cannot write the new sensor to the sensorConfig file");
	    	e.printStackTrace();
	    	throw new ConfigurationException();
	    } catch (XPathExpressionException e) {
	    	logger.error("Looks like the sensor config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (ParserConfigurationException e) {
			logger.error("Looks like the sensor config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method returns a list of sensors' XML configuration for all sensors belonging to a specified protocol 
	 * @param pid the ProtocolID of the protocol whose sensors are to be listed
	 * @throws ConfigurationException if there is a problem with the sensor config file
	 */
	public synchronized Vector<Node> listSensors(ProtocolID pid) throws ConfigurationException{
		Vector<Node> v = new Vector<Node>();
		NodeList nl;
		try {
			nl = XMLhelper.getNodeList("//"+ Sensor.SENSOR_TAG+"//parameters["
					+SensorManager.COMPONENTPARAM_TAG+"[@name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\" and @value=\""+pid.getName()+"\"]]/parent::*"
					, sensorconfig);
			for(int i=0; i<nl.getLength(); i++)
				v.add(XMLhelper.duplicateNode(nl.item(i)));
	    } catch (XPathExpressionException e) {
	    	logger.error("Looks like the sensor config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (ParserConfigurationException e) {
	    	logger.error("Cant duplicate the node...");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	    return v;
	}
	
	/**
	 * This method removes sensors' XML configuration for all sensors belonging to a specified protocol 
	 * @param pid the ProtocolID of the protocol whose sensors are to be removed
	 * @throws ConfigurationException if there is a problem with the sensor config file
	 */
	public synchronized void removeSensors(ProtocolID pid) throws ConfigurationException{
		NodeList nl;
		try {
			nl = XMLhelper.getNodeList("//"+ Sensor.SENSOR_TAG+"//parameters["
					+SensorManager.COMPONENTPARAM_TAG+"[@name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\" and @value=\""+pid.getName()+"\"]]/parent::*"
					, sensorconfig);
			for(int i=0; i<nl.getLength(); i++)
				XMLhelper.deleteNode(nl.item(i));
	        writeDocumentToFile(sensorConfigFile,sensorconfig);
	    } catch (IOException e) {
	    	logger.error("Cannot write the new sensor to the sensorConfig file");
	    	e.printStackTrace();
	    	throw new ConfigurationException();
	    } catch (XPathExpressionException e) {
	    	logger.error("Looks like the sensor config file is malformed");
			e.printStackTrace();
			throw new ConfigurationException();
		} 
	}
	
	/**
	 * This method looks in the sensor configuration file for a sensor with the same address and protocol
	 * name as the one given in argument. If one is found, a new SensorID based on the sid found in the
	 * config file for that sensor is returned. 
	 * @param n the node whose address and protocol name will be looked for in the sensor configuration document.
	 * @return a new SensorID as found in the configuration document
	 * @throws ConfigurationException if the sensor Id cant be found
	 */
	public SensorID findSID(Node n) throws ConfigurationException{
		ArrayList<String> params = null;
		String addr=null, pname=null, xpathQuery;
		int i=0;		
		try {
			//fetches the native address
			params = XMLhelper.getAttributeListFromElement("//"+SensorManager.COMPONENTPARAM_TAG+"[@name=\""+Sensor.SENSORADDRESSATTRIBUTE_TAG+"\"]", n);
			if(params.size()>0 && params.contains("value")) {
				//FIXME hardcoded value below
				while(!params.get(i++).equals("value"));
				addr = params.get(i);
				//logger.debug("found address: "+addr);
			} else {
				logger.error("Cant find the sensor address from given sensor XML node ("+(params.size()>0)+")");
				throw new ConfigurationException();
			}

			//fetches the protocol name
			params = XMLhelper.getAttributeListFromElement("//"+SensorManager.COMPONENTPARAM_TAG+"[@name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\"]", n);
			if(params.size()>0 && params.contains("value")) {
				//FIXME hardcoded value below
				i=0;
				while(!params.get(i++).equals("value"));
				pname = params.get(i);
				//logger.debug("found Protocol name: "+pname);
			} else {
				logger.error("Cant find the protocol name from given sensor XML node ("+(params.size()>0)+")");
				throw new ConfigurationException();
			}
			
			//look up those two value (native address and protocol name) in the sensor config file
			//FIXME hardcoded values below
			xpathQuery = "//parameters["+SensorManager.COMPONENTPARAM_TAG+"[@name=\""+Sensor.SENSORADDRESSATTRIBUTE_TAG+"\" and @value=\""+addr+"\"]"
						+" and "+SensorManager.COMPONENTPARAM_TAG+"[@name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\" and @value=\""+pname+"\"]]/parent::*";
			//logger.debug("XPATH query: "+xpathQuery);
			params = XMLhelper.getAttributeListFromElement(xpathQuery, sensorconfig);
			if(params.size()>0 && params.contains(Sensor.SENSORID_TAG)) {
				i=0;
				while(!params.get(i++).equals(Sensor.SENSORID_TAG));
				//logger.debug("Found matching sensor in config file: returning id: "+params.get(i));
				return new SensorID(params.get(i));
			}
			
			logger.debug("Cant find the sensor sid for matching sensor in sensor config file ("+(params.size()>0)+")");
			throw new ConfigurationException();
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method returns a copy of all protocol configuration nodes as found in the configuration section
	 * of the PCML document. 
	 * @return a node enumeration
	 * @throws ConfigurationException 
	 */
	public synchronized Vector<Node> getProtocols() throws ConfigurationException {
		Vector<Node> cprotocol = new Vector<Node>();
		NodeList nl = null;
		try {
			nl = XMLhelper.getNodeList("//" + Protocol.PROTOCOL_TAG, platformCC);
			for(int i=0; i<nl.getLength(); i++)
				cprotocol.add(XMLhelper.duplicateNode(nl.item(i)));
			return cprotocol;
		} catch (ParserConfigurationException e) {
			logger.error("Could not parse the XML configuration file");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (XPathExpressionException e) {
			logger.error("Could not parse the XML configuration file");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}


	 /** This method returns a copy of all sensor configuration nodes as found in the sensor configuration document
	 * @return a node enumeration
	 * @throws ConfigurationException 
	 */
	public synchronized Vector<Node> getSensors() throws ConfigurationException {
		Vector<Node> csensor = new Vector<Node>();
		NodeList nl = null;
		try {
			nl = XMLhelper.getNodeList("//" + Sensor.SENSOR_TAG, sensorconfig);
			for(int i=0; i<nl.getLength(); i++)
				csensor.add(XMLhelper.duplicateNode(nl.item(i)));
			return csensor;
		} catch (ParserConfigurationException e) {
			logger.error("Could not parse the XML configuration file");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (XPathExpressionException e) {
			logger.error("Could not parse the XML configuration file");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method returns a copy of the sensor configuration file
	 * @return a copy of the sensor configuration file
	 * @throws ParserConfigurationException if there is a problem duplicating the config file
	 */
	public synchronized Document getSensorConfigFile() throws ParserConfigurationException{
		return XMLhelper.duplicateDocument(sensorconfig);
	}
	
	/**
	 * This returns a list of Sensor IDs currently found in the sensor config file
	 * @return a list of Sensor IDs currently found in the sensor config file
	 */
	public synchronized ArrayList<String> listSensorID(){
		ArrayList<String> list = new ArrayList<String>();
		NodeList nl = null;
		try {
			nl = XMLhelper.getNodeList("//" + Sensor.SENSOR_TAG, sensorconfig);
			for(int i=0; i<nl.getLength(); i++) {
				list.add(XMLhelper.getAttributeFromName(Sensor.SENSORID_TAG, nl.item(i)));
			}
		} catch (XPathExpressionException e) {
			logger.error("Could not parse the XML configuration file");
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * This method generates an empty platform configuration file 
	 * @return an empty document containing the barebone structure for a platform config file
	 * @throws ParserConfigurationException if the document can not be created
	 */
	public Document createEmptyPlatformConfig() throws ParserConfigurationException {
		String s = "<SAL>\n"
				+"\t<PlatformConfiguration>\n"
				+"\t\t<general>\n"
				+"\t\t\t<logging />\n"
				+"\t\t\t<readingDirectory>/home/sensor/readings</readingDirectory>\n"
				+"\t\t</general>\n"
				+"\t\t<protocols />\n"
				+"\t</PlatformConfiguration>\n"
				+"</SAL>";
		return XMLhelper.createDocument(s);
	}
	
	/**
	 * This method generates an empty sensor configuration file 
	 * @return an empty document containing the barebone structure for a sensor config file
	 * @throws ParserConfigurationException if the document can not be created
	 */
	public Document createEmptySensorConfig() throws ParserConfigurationException {
		String s = "<SAL>\n"
				+"\t<SensorConfiguration />\n"
				+"</SAL>";
		return XMLhelper.createDocument(s);
	}
	
	public static void main(String[] args) throws ConfigurationException, ParserConfigurationException {
		ConfigService e = ConfigService.getService();
		e.init("/home/gilles/workspace/SAL/src/platformConfig-owfs.xml", "/home/gilles/workspace/SAL/src/sensors-owfs-hb1.xml");
		Iterator<Node> iter = e.getProtocols().iterator();
		System.out.println("");
		while(iter.hasNext()) {
			System.out.println("Protocol:");
			System.out.println(XMLhelper.toString(iter.next()));
		}
		System.out.println("");
		iter = e.getSensors().iterator();
		while(iter.hasNext()) {
			System.out.println("Sensor:");
			System.out.println(XMLhelper.toString(iter.next()));
		}
		
		String ns = "<Sensor sid=\"4\"><parameters><Param name=\"ProtocolName\" value=\"1wtree\"/>"
				+"<Param name=\"Address\" value=\"26.0D7F61000000\"/></parameters></Sensor>";
		
		e.addSensor(XMLhelper.createDocument(ns));
		
		ns = "<Sensor sid=\"5\"><parameters><Param name=\"ProtocolName\" value=\"1wtree\"/>"
			+"<Param name=\"Address\" value=\"26.0D7F61000000\"/></parameters></Sensor>";
		e.addSensor(XMLhelper.createDocument(ns));
		
		ns = "<Protocol name=\"1wtree\" type=\"PlatformData\">"
				+"<EndPoint name=\"filesystem\" type=\"fs\" /><parameters>"
					+"<Param name=\"CPUTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp1_input\" />"
					+"<Param name=\"NBTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp3_input\" />"
					+"<Param name=\"SBTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp2_input\" />"
				+"</parameters></Protocol>";
		e.addProtocol(XMLhelper.createDocument(ns));
		
		ns = "<Protocol name=\"osData\" type=\"PlatformData\">"
			+"<EndPoint name=\"filesystem\" type=\"fs\" /><parameters>"
				+"<Param name=\"CPUTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp1_input\" />"
				+"<Param name=\"NBTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp3_input\" />"
				+"<Param name=\"SBTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp2_input\" />"
			+"</parameters></Protocol>";
	e.addProtocol(XMLhelper.createDocument(ns));
	ArrayList<String> l = e.listSensorID();
	for (int i = 0; i < l.size(); i++) {
		System.out.println("SID: "+ l.get(i));		
	}
	}
	
	private void writeDocumentToFile(File f, Document d) throws IOException{
//        logger.debug("Writing new config to file "+f.getName()+" :");
//        logger.debug(XMLhelper.toString(d));
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write(XMLhelper.toString(d));
        out.close();
	}
	
}
