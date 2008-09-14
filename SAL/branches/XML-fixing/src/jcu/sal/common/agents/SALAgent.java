package jcu.sal.common.agents;

import jcu.sal.common.Response;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.events.EventHandler;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.ParserException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;

public interface SALAgent {
	/**
	 * This method initialises the SAL agent. It parses the platform & sensor configuration files
	 * and creates the required components as per configuration files. 
	 * @param pc the platform config file
	 * @param sc the sensor config file
	 * @throws ConfigurationException if the files can not be written to, parsed, or the configuration is incorrect
	 */
	public void start(String pc, String sc) throws ConfigurationException;
	
	/**
	 * This method stops the SAL agent. It must be called if a previous call to <code>start()</code> was successful.
	 *
	 */
	public void stop();
	
	/*
	 * Sensor-related methods
	 */
	
	/**
	 * This method instantiate a new sensor given its SML document. the returned value is a representation
	 * of the sensor identifier. If one is specified in the XML document, it will be ignored and replaced
	 * with a new one (the returned value). 
	 * @param xml the sensor's XML configuration document
	 * @return a string representing the sensor identifier
	 * @throws ParserException if the XML document cannot be parsed
	 * @throws SALDocumentException if the SML document is incorrect
	 * @throws ConfigurationException if the sensor cant be instantiated because of invalid configuration information
	 */
	public String addSensor(String xml) throws SALDocumentException, ParserException, ConfigurationException;
	
	/**
	 * This method removes a sensor with the given identifier. Its configuration information is also removed from the
	 * configuration file.
	 * @param sid the sensor identifier
	 * @throws NotFoundException if the ID cannot be found
	 */
	public void removeSensor(String sid) throws NotFoundException;
	
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
	 * @throws NotFoundException if the given sensor id doesnt match any existing sensor
	 * @throws SensorControlException if there is an error controlling the sensor. If this exception is raised,
	 * the cause of this exception will be linked to it and can be retrieved using <code>getCause()</code>  
	 */
	public Response execute(Command c, String sid) throws NotFoundException, SensorControlException;	
	/**
	 * This method returns the CML document for a given sensor
	 * @param sid the sensor identifier
	 * @return the CML doc
	 * @throws NotFoundException if given sensor ID doesnt match any existing sensor
	 */
	public String getCML(String sid) throws NotFoundException;
	
	/*
	 * Protocols-related methods 
	 */
	
	/**
	 * This method instantiate a new protocol given its PCML document. If successful, this method will also
	 * store the protocol's PCML configuration information in the platform configuration file
	 * @param xml the protocol's PCML configuration document
	 * @param loadSensors set to true if the sensor configuration file should be checked for sensors associated with 
	 * this protocol and create them.
	 * @throws ParserException if the given string isnt a valid XML document
	 * @throws ConfigurationException if the protocol cant be instantiated because of invalid configuration information
	 * @throws SALDocumentException if the given PCML is invalid  
	 */
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, ParserException, SALDocumentException;
	
	/**
	 * This method removes a protocol given its ID. The protocol is first stopped so commands are no further 
	 * accepted. It then removes all associated sensors and their configuration if <code>removeSensors</code> is set to true. 
	 * @throws NotFoundException if the given protocol ID doesnt match any existing protocols
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws NotFoundException;
	
	/**
	 * This method lists the configuration of all existing protocols
	 * @return a PCML document listing the protocols configuration
	 */
	public String listProtocols();
	
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
	 * @throws NotFoundException if the given producerID doesnt exist
	 */
	public void registerEventHandler(EventHandler eh, String producerID) throws NotFoundException;
	
	/**
	 * This method unregisters an EventHandler previously registered with <code>registerEventHandler()</code>
	 * @param eh the EventHandler to re be removed
	 * @param producerID the producer to which it is associated
	 * @throws NotFoundException if the handler can not be found/removed
	 */
	public void unregisterEventHandler(EventHandler eh, String producerID) throws NotFoundException;
}

