package jcu.sal.Agent;

import java.io.NotActiveException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.Command;

public interface SALAgentInterface extends Remote{
	/**
	 * This method initialises the SAL agent. It parses the platform & sensor configuration files
	 * and creates the required components as per configuration files. 
	 * @param pc the platform config file
	 * @param sc the sensor config file
	 * @throws ConfigurationException if the files can not be parsed, or the configuration is incorrect
	 */
	public void start(String pc, String sc) throws ConfigurationException, RemoteException;
	
	/**
	 * This method stops the SAL agent
	 *
	 */
	public void stop() throws RemoteException;
	
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
	public String execute(Command c, String sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException, RemoteException;
	
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
	

}

