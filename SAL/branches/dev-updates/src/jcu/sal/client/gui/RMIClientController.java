package jcu.sal.client.gui;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;

public interface RMIClientController {
	
	/**
	 * This method registers a new SAL client with an agent 
	 * @param rmiName A unique name associated with the RMI Client. The name is chosen by the caller and must be used in subsequent calls
	 * to execute() and {un}registerEventHandler().
	 * @param ipAddress the IP address of the agent RMI registry
	 * @param ourIpAddress the IP address of the SAL client's RMI registry  
	 * @throws ConfigurationException if this name already exists
	 * @throws RemoteException if the registry cant be reached
	 */
	public void connect(String rmiName, String ipAddress, String ourIpAddress) throws ConfigurationException, RemoteException;
	
	/**
	 * This method unregisters a SAL client from an agent
	 * @param rmiName The unique name associated with the Client
	 * @throws ConfigurationException if this name already exists
	 */
	public void disconnect(String rmiName) throws ConfigurationException, RemoteException;
	
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
	public String addSensor(String xml) throws SALDocumentException, ConfigurationException, RemoteException;
	
	/**
	 * This method removes a sensor with the given identifier. Its configuration information is also removed from the
	 * configuration file.
	 * @param sid the sensor identifier
	 * @throws NotFoundException if the given sensor ID doesnt match any existing sensor
	 */
	public void removeSensor(String sid) throws NotFoundException, RemoteException;
	
	/**
	 * This method returns an XML document containing the configuration of all currently active sensors.
	 * An active sensor is one that has been connected at least once since startup. Note that an active
	 * sensor may not be currently connected (for instance if its protocol has been removed). The returned string
	 * can be used to create a <code>SMLDescriptions</code> object which facilitate the parsing of the XML document.
	 * @return the configuration of all active sensors as an XML document, from which a <code>SMLDescriptions</code>
	 * object can be created to facilitate parsing.
	 */
	public String listActiveSensors() throws RemoteException;
	
	/**
	 * This method returns an XML document containing the configuration of all known sensors.
	 * A known sensor is one that has its configuration stored in the sensor configuration file 
	 * Known sensors may or may not be currently connected, and may not have been connected at all since
	 * startup. The returned string can be used to create a <code>SMLDescriptions</code> object which facilitate
	 * the parsing of the XML document.
	 * @return the configuration of all known sensors as an XML document, from which a <code>SMLDescriptions</code>
	 * object can be created to facilitate parsing.
	 */
	public String listSensors() throws RemoteException;
	
	/**
	 * This method instructs a sensor identified by sid to execute the command c 
	 * @param c the command to be executed
	 * @param sid the target sensor identifier
	 * @return the result
	 * @throws NotFoundException if the given sensor id doesnt match any existing sensor
	 * @throws SensorControlException if there is an error controlling the sensor. If this exception is raised,
	 * the cause of this exception will be linked to it and can be retrieved using <code>getCause()</code>  
	 */
	public Response execute(RMICommand c, String sid) throws NotFoundException, SensorControlException, RemoteException;
	
	/**
	 * This method returns the a string representation of the CML descriptions document for a given sensor.
	 * The returned string can be used to create a <code>CMLDescriptions</code> object to facilitate parsing the
	 * XML document.
	 * @param sid the sensor identifier
	 * @return the CML document, from which a <code>CMLDescriptions</code> can be created to facilitate parsing.
	 * @throws NotFoundException if given sensor ID doesnt match any existing sensor
	 */
	public String getCML(String sid) throws NotFoundException, RemoteException;
	
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
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, SALDocumentException, RemoteException;
	
	/**
	 * This method removes a protocol given its ID. The protocol is first stopped so commands are no further 
	 * accepted. It then removes all associated sensors and their configuration if <code>removeSensors</code> is set to true.
	 * @param pid the protocol identifier
	 * @param removeSensor whether or not to remove the sensor configuration associated with this protocol from the config file
	 * @throws NotFoundException if the given protocol ID doesnt match any existing protocols
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws NotFoundException, RemoteException;
	
	/**
	 * This method lists the configuration of all existing protocols. The returned value is a string
	 * representation of a PCML protocol descriptions document, which can be used to create a 
	 * <code>ProtocolConfigurations</code> object to facilitate parsing.
	 * @return a string representation of a PCML document listing the protocols configuration, which can be used to create a 
	 * <code>ProtocolConfigurations</code> object to facilitate parsing.
	 */
	public String listProtocols() throws RemoteException;
	
	/*
	 * Event handling
	 */
	
	/**
	 * This method registers an RMI event handler. Whenever the producer <code>producerID</code> generates an event, the method
	 * <code>handle</code> will be called on the RMI EventHandler <code>ev</code> with a matching Event object as the sole argument.
	 * A Producers ID is a protocol name. Three special producers also exist: <code>SensorManager.PRODUCER_ID</code> which generates
	 * <code>SensorNodeEvent</code> events when sensors are created and deleted, <code>ProtocolManager.PRODUCER_ID</code> which
	 * generates <code>ProtocolListEvent</code> events when protocols are created and deleted, <code>SensorState.PRODUCER_ID</code>
	 * which generates <code>SensorStateEvent</code> events when a sensor is connected or disconnected.  
	 * @param objName the name of the RMI event handler to lookup in the RMI registry.
	 * @param producerID the identifier of a protocol or the special identifiers "SensorManager", "ProtocolManager" or "SensorState"
	 * @param r the RMI event handler to be registered with the client's RMI registry. 
	 * @throws NotFoundException if the given producerID doesnt exist
	 * @throws RemoteException if the RMI event handler object cant be found in the RMI registry
	 */
	public void registerEventHandler(String objName, String producerID, Remote r) throws NotFoundException, RemoteException;
	
	/**
	 * This method unregisters an EventHandler previously registered with <code>registerEventHandler()</code>
	 * @param objName the name of the object to lookup in the RMI registry.
	 * @param producerID the producer to which it is associated
	 * @throws NotFoundException if the handler can not be found/removed
	 * @throws RemoteException if the RMI event handler object cant be found in the RMI registry
	 */
	public void unregisterEventHandler(String objName, String producerID) throws NotFoundException, RemoteException;
	
}
