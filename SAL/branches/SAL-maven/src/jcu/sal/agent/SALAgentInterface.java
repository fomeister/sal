package jcu.sal.agent;

import java.io.NotActiveException;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.Command;
import jcu.sal.common.Response;
import jcu.sal.events.EventHandler;

public interface SALAgentInterface {
	/**
	 * This method initialises the SAL agent. It parses the platform & sensor configuration files
	 * and creates the required components as per configuration files. 
	 * @param pc the platform config file
	 * @param sc the sensor config file
	 * @throws ConfigurationException if the files can not be parsed, or the configuration is incorrect
	 */
	public void start(String pc, String sc) throws ConfigurationException;
	
	/**
	 * This method stops the SAL agent
	 *
	 */
	public void stop();
	
	/*
	 * Sensor-related methods
	 */
	
	/**
	 * This method instanciate a new sensor given its XML document. the returned value is a representation
	 * of the sensor identifier. If one is specified in the XML document, it will be ignored and replaced
	 * with a new one (the returned value). 
	 * @param xml the sensor's XML configuration document
	 * @return a string representing the sensor identifier
	 * @throws ParserConfigurationException if the XML document cannot be parsed
	 * @throws ConfigurationException if the XML document is incorrect
	 */
	public String addSensor(String xml) throws ConfigurationException, ParserConfigurationException;
	
	/**
	 * This method removes a sensor given its identifier
	 * @param sid the sensor identifier
	 * @throws ConfigurationException if the ID cannot be found
	 */
	public void removeSensor(String sid) throws ConfigurationException;
	
	/**
	 * This method returns an XML document containing the configuration of all currently active sensors.
	 * An active sensor is one that has been connected at least once since startup. Note that an active
	 * sensor may not be currently connected (for instance if its protocol has been removed).
	 * @return the list of all active sensors as an XML doc
	 */
	public String listActiveSensors();
	
	/**
	 * This method returns an XML document containing the configuration of all known sensors.
	 * A known sensor is one that has its configuration stored in the sensor configuration file 
	 * Known sensors may or may not be currently connected, and may not have been connected at all since
	 * startup.
	 * @return the list of all known sensors as an XML doc
	 */
	public String listSensors();
	
	/**
	 * This method instructs a sensor identified by sid to execute the command c 
	 * @param c the command to be executed
	 * @param sid the target sensor identifier
	 * @return the result
	 */
	public Response execute(Command c, String sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException;
	
	/**
	 * This method returns the CML document for a given sensor
	 * @param sid the sensor identifier
	 * @return the CML doc
	 * @throws ConfigurationException if the CML doc cant be found
	 */
	public String getCML(String sid) throws ConfigurationException, NotActiveException;
	
	/*
	 * Protocols-related methods 
	 */
	
	/**
	 * This method instanciate a new protocol given its XML document. If successful, this method will also
	 * store the protocol's XML configuration information in the platform configuration file
	 * @param xml the protocol's XML configuration document
	 * @param loadSensors set to true if the sensor configuration file should be checked for sensors associated with 
	 * this protocol and create them.
	 * @throws ParserConfigurationException if the XML document cannot be parsed
	 * @throws ConfigurationException if the XML document is incorrect
	 */
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, ParserConfigurationException;
	
	/**
	 * This method removes a protocol given its ID. The protocol is first stopped so commands are no further 
	 * accepted. It then removes all associated sensors and their configuration if <code>removeSensors</code> is set to true. 
	 * @throws ConfigurationException if the ID cannot be found
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws ConfigurationException;
	
	/*
	 * Event-related methods 
	 */
	
	/**
	 * This method registers an event handler. Whenever the producer <code>producerID</code> generates an event, the method
	 * <code>handle</code> will be called on the EventHandler <code>ev</code> with a matching Event object as the sole argument.
	 * A Producers ID is a protocol name. Three special producers also exist: <code>SensorManager.PRODUCER_ID</code> which generates
	 * <code>SensorNodeEvent</code> events when sensors are created and deleted, <code>ProtocolManager.PRODUCER_ID</code> which
	 * generates <code>ProtocolListEvent</code> events when protocols are created and deleted, <code>SensorState.PRODUCER_ID</code>
	 * which generates <code>SensorStateEvent</code> events when a sensor is connected or disconnected.  
	 * @param eh an instance of a class implementing the EventHandler interface which will receive events.
	 * @param producerID the identifier of a protocol or the special identifiers "SensorManager", "ProtocolManager" or "SensorState"
	 * @throws ConfigurationException if the given producerID doesnt exist
	 */
	public void registerEventHandler(EventHandler eh, String producerID) throws ConfigurationException;
	
	/**
	 * This method unregisters an EventHandler previously registered with <code>registerEventHandler()</code>
	 * @param eh the EventHandler to re be removed
	 * @param producerID the producer to which it is associated
	 * @throws ConfigurationException if the handler can not be found/removed
	 */
	public void unregisterEventHandler(EventHandler eh, String producerID) throws ConfigurationException;
}

