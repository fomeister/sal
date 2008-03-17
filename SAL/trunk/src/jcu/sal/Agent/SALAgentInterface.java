package jcu.sal.Agent;

import java.io.NotActiveException;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.Command;

public interface SALAgentInterface {
	/**
	 * Initialises the SAL agent. Parses the platform & sensor configuration files, and creates the objects 
	 * @param pc the platform config file
	 * @param sc the sensor config file
	 * @throws ConfigurationException if the files can not be parsed, or the configuration is incorrect
	 */
	public void init(String pc, String sc) throws ConfigurationException;
	
	/**
	 * This method returns a list of all sensors as an XML doc
	 * @return the list of all sensors as an XML doc
	 */
	public String listSensors();
	
	/**
	 * Execute the command c on sensor sid
	 * @param c the command
	 * @param sid the sensor
	 * @return the result
	 */
	public String execute(Command c, String sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException;
	
	/**
	 * Returns the CML document for a given sensor
	 * @param sid the sensor id
	 * @return the CML doc
	 * @throws ConfigurationException if the CML doc cant be found
	 */
	public String getCML(String sid) throws ConfigurationException, NotActiveException;
	
	/**
	 * Stops the SAL agent
	 *
	 */
	public void stop();
	
	/**
	 * Instanciate a new protocol given its XML document
	 * @throws ParserConfigurationException if the XML document cannot be parsed
	 * @throws ConfigurationException if the XML document is incorrect
	 */
	public void addProtocol(String xml) throws ConfigurationException, ParserConfigurationException;
	
	/**
	 * Remove a protocol given its ID
	 * @throws ConfigurationException if the ID cannot be found
	 */
	public void removeProtocol(String pid) throws ConfigurationException;
	
	/**
	 * Remove all protocols
	 */
	public void removeProtocols() throws ConfigurationException;
	
	/**
	 * Instanciate a new sensor given its XML document
	 * @throws ParserConfigurationException if the XML document cannot be parsed
	 * @throws ConfigurationException if the XML document is incorrect
	 */
	public void addSensor(String xml) throws ConfigurationException, ParserConfigurationException;
	
	/**
	 * Remove a sensor given its ID
	 * @throws ConfigurationException if the ID cannot be found
	 */
	public void removeSensor(String pid) throws ConfigurationException;
}

