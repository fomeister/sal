package jcu.sal.client.gui;

import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;

public interface ClientController {
	/*
	 * Sensor-related methods
	 */
	
	/**
	 * This method instantiate a new sensor given its SML description. The SML description must be generated by
	 * calling the <code>getXMLString()</code> on an <code>SMLdescription</code> object.The returned value is a 
	 * string representation of the sensor identifier. If one is specified in the XML document, it will be ignored
	 * and replaced with a new one (the returned value). 
	 * @param xml the sensor's XML configuration document. It is generated by calling <code>SMLDescription.getXMLString()</code>
	 * @return a string representing the sensor identifier
	 * @throws SALDocumentException if the SML document is malformed
	 * @throws ConfigurationException if the sensor cant be instantiated because of invalid configuration information
	 */
	public String addSensor(String xml) throws SALDocumentException, ConfigurationException, AgentException;
	
	/**
	 * This method removes a sensor with the given identifier. Its configuration information is also removed from the
	 * configuration file.
	 * @param sid the sensor identifier
	 * @throws NotFoundException if the given sensor ID doesnt match any existing sensor
	 */
	public void removeSensor(String sid) throws NotFoundException, AgentException;
	
	/**
	 * This method returns an XML document containing the configuration of all currently active sensors.
	 * An active sensor is one that has been connected at least once since startup. Note that an active
	 * sensor may not be currently connected (for instance if its protocol has been removed). The returned string
	 * can be used to create a <code>SMLDescriptions</code> object which facilitate the parsing of the XML document.
	 * @return the configuration of all active sensors as an XML document, from which a <code>SMLDescriptions</code>
	 * object can be created to facilitate parsing.
	 */
	public String listActiveSensors() throws AgentException;
	
	/**
	 * This method returns an XML document containing the configuration of all known sensors.
	 * A known sensor is one that has its configuration stored in the sensor configuration file 
	 * Known sensors may or may not be currently connected, and may not have been connected at all since
	 * startup. The returned string can be used to create a <code>SMLDescriptions</code> object which facilitate
	 * the parsing of the XML document.
	 * @return the configuration of all known sensors as an XML document, from which a <code>SMLDescriptions</code>
	 * object can be created to facilitate parsing.
	 */
	public String listSensors() throws AgentException;
	
	/**
	 * This method returns an SML description document containing the configuration for a given sensor.
	 * The returned string can be used to create a <code>SMLDescriptions</code> object which facilitate the
	 * parsing of the XML document.
	 * @return the configuration of the given sensors as an XML document, from which a <code>SMLDescriptions</code>
	 * object can be created to facilitate parsing.
	 * @throws NotFoundException if no sensor matches the given identifier
	 */
	public String listSensor(String sid) throws NotFoundException, AgentException;
	
	/**
	 * This method instructs a sensor identified by sid to execute a given command c
	 * @param c the command
	 * @param sid the target sensor identifier
	 * @return the result
	 * @throws NotFoundException if the given sensor id doesnt match any existing sensor
	 * @throws SensorControlException if there is an error controlling the sensor. If this exception is raised,
	 * the cause of this exception will be linked to it and can be retrieved using <code>getCause()</code>  
	 */
	//public Response execute(RMICommand c, String sid) throws NotFoundException, SensorControlException, RemoteException;
	
	/**
	 * This method returns the a string representation of the CML descriptions document for a given sensor.
	 * The returned string can be used to create a <code>CMLDescriptions</code> object to facilitate parsing the
	 * XML document.
	 * @param sid the sensor identifier
	 * @return the CML document, from which a <code>CMLDescriptions</code> can be created to facilitate parsing.
	 * @throws NotFoundException if given sensor ID doesnt match any existing sensor
	 */
	public String getCML(String sid) throws NotFoundException, AgentException;
	
	/*
	 * Protocols-related methods 
	 */
	
	/**
	 * This method instantiates a new protocol given its PCML protocol configuration document (as generated
	 * by <code>ProtocolConfiguration.getXMLString()</code>). If successful, this method will also store the
	 * protocol's PCML configuration information in the platform configuration file
	 * @param xml a string version of the protocol's PCML protocol description document as generated by
	 * <code>ProtocolDesrcription.getXMLString()</code>
	 * @param loadSensors set to true if the sensor configuration file should be checked for sensors associated with 
	 * this protocol and create them.
	 * @throws ConfigurationException if the protocol cant be instantiated because of invalid configuration information
	 * @throws SALDocumentException if the given PCML document is malformed 
	 */
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, SALDocumentException, AgentException;
	
	/**
	 * This method removes a protocol given its ID. The protocol is first stopped so commands are no further 
	 * accepted. It then removes all associated sensors and their configuration if <code>removeSensors</code> is set to true.
	 * @param pid the protocol identifier
	 * @param removeSensor whether or not to remove the sensor configuration associated with this protocol from the config file
	 * @throws NotFoundException if the given protocol ID doesnt match any existing protocols
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws NotFoundException, AgentException;
	
	/**
	 * This method lists the configuration of all existing protocols. The returned value is a string
	 * representation of a PCML protocol descriptions document, which can be used to create a 
	 * <code>ProtocolConfigurations</code> object to facilitate parsing.
	 * @return a string representation of a PCML document listing the protocols configuration, which can be used to create a 
	 * <code>ProtocolConfigurations</code> object to facilitate parsing.
	 */
	public String listProtocols() throws AgentException;	
}