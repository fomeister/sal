package jcu.sal.common.agents;

import java.io.NotActiveException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;

public interface RMISALAgent extends Remote{
	/**
	 * This string is the name of the SAL agent stub as found in the RMI registry.
	 */
	public static String RMI_STUB_NAME = "RMI SAL Agent";
	
	/**
	 * This method registers a new SAL client. 
	 * @param rmiName A unique name associated with the RMI Client. The name is chosen by the caller and must be used in subsequent calls
	 * to execute() and {un}registerEventHandler().
	 * @param ipAddress the IP address of the RMI registry the client will use to register its objects. The RMI registry
	 * will be accessed by this Agent to invoke methods on the Client (StreamCallbacks and EventHandlers) 
	 * @throws ConfigurationException if this name already exists
	 * @throws RemoteException if the registry cant be reached
	 */
	public void registerClient(String rmiName, String ipAddress) throws ConfigurationException, RemoteException;
	
	/**
	 * This method unregisters a SAL client. 
	 * @param rmiName The unique name associated with the Client
	 * @throws ConfigurationException if this name already exists
	 */
	public void unregisterClient(String rmiName) throws ConfigurationException, RemoteException;
	
	/*
	 * Sensor-related methods
	 */
	
	/**
	 * This method instanciate a new sensor given its XML document. the returned value is a representation
	 * of the sensor identifier. If one is specified in the XML document, it will be ignored and replaced
	 * with a new one (the returned value). public static String RMI_STUB_NAME = "RMI SAL Agent";
	 * @param xml the sensor's XML configuration document
	 * @return a string representing the sensor identifier
	 * @throws ParserConfigurationException if the XML document cannot be parsed
	 * @throws ConfigurationException if the XML document is incorrect
	 */
	public String addSensor(String xml) throws ConfigurationException, ParserConfigurationException, RemoteException;
	
	/**
	 * This method removes a sensor given its identifier
	 * @param sid the sensor identifier
	 * @throws ConfigurationException if the ID cannot be found
	 */
	public void removeSensor(String sid) throws ConfigurationException, RemoteException;
	
	/**
	 * This method returns an XML document containing the configuration of all currently active sensors.
	 * An active sensor is one that has been connected at least once since startup. Note that an active
	 * sensor may not be currently connected (for instance if its protocol has been removed).
	 * @return the list of all active sensors as an XML doc
	 */
	public String listActiveSensors() throws RemoteException;
	
	/**
	 * This method returns an XML document containing the configuration of all known sensors.
	 * A known sensor is one that has its configuration stored in the sensor configuration file 
	 * Known sensors may or may not be currently connected, and may not have been connected at all since
	 * startup.
	 * @return the list of all known sensors as an XML doc
	 */
	public String listSensors() throws RemoteException;
	
	/**
	 * This method instructs a sensor identified by sid to execute the command c 
	 * @param c the command to be executed
	 * @param sid the target sensor identifier
	 * @return the result
	 */
	public Response execute(RMICommand c, String sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException, RemoteException;
	
	/**
	 * This method returns the CML document for a given sensor
	 * @param sid the sensor identifier
	 * @return the CML doc
	 * @throws ConfigurationException if the CML doc cant be found
	 */
	public String getCML(String sid) throws ConfigurationException, NotActiveException, RemoteException;
	
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
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, ParserConfigurationException, RemoteException;
	
	/**
	 * This method removes a protocol given its ID. The protocol is first stopped so commands are no further 
	 * accepted. It then removes all associated sensors and their configuration if <code>removeSensors</code> is set to true. 
	 * @throws ConfigurationException if the ID cannot be found
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws ConfigurationException, RemoteException;
	
	/*
	 * Event-related methods 
	 */
	
	/**
	 * This method registers an RMI event handler. Whenever the producer <code>producerID</code> generates an event, the method
	 * <code>handle</code> will be called on the RMI EventHandler <code>ev</code> with a matching Event object as the sole argument.
	 * A Producers ID is a protocol name. Three special producers also exist: <code>SensorManager.PRODUCER_ID</code> which generates
	 * <code>SensorNodeEvent</code> events when sensors are created and deleted, <code>ProtocolManager.PRODUCER_ID</code> which
	 * generates <code>ProtocolListEvent</code> events when protocols are created and deleted, <code>SensorState.PRODUCER_ID</code>
	 * which generates <code>SensorStateEvent</code> events when a sensor is connected or disconnected.  
	 * @param rmiName the name of the RMI client as previously registered with registerClient().
	 * @param objName the name of the RMI event handler to lookup in the RMI registry.
	 * @param producerID the identifier of a protocol or the special identifiers "SensorManager", "ProtocolManager" or "SensorState"
	 * @throws ConfigurationException if the given producerID doesnt exist
	 * @throws RemoteException if the RMI event handler obejct cant be found in the RMI registry
	 */
	public void registerEventHandler(String rmiName, String objName, String producerID) throws ConfigurationException, RemoteException;
	
	/**
	 * This method unregisters an EventHandler previously registered with <code>registerEventHandler()</code>
	 * @param rmiName the name of the RMI client as previously registered with registerClient().
	 * @param objName the name of the object to lookup in the RMI registry.
	 * @param producerID the producer to which it is associated
	 * @throws ConfigurationException if the handler can not be found/removed
	 * @throws RemoteException if the RMI event handler obejct cant be found in the RMI registry
	 */
	public void unregisterEventHandler(String rmiName, String objName, String producerID) throws ConfigurationException, RemoteException;
}
