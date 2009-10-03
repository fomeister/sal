package jcu.sal.common.agents;

import jcu.sal.common.CommandFactory;
import jcu.sal.common.Constants;
import jcu.sal.common.Response;
import jcu.sal.common.StreamID;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.events.ClientEventHandler;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;

public interface SALAgent{
	
	/**
	 * This method returns the unique ID of this SAL Agent
	 * @return the unique ID of this SAL Agent
	 */
	public String getID();
	
	/**
	 * This method returns a string representation of the type of this agent
	 * Possible values are: {@link Constants#Local_Agent_type}, {@link Constants#RMI_Agent_type}
	 * @return the type of this agent
	 */
	public String getType();
	
	/*
	 * Sensor-related methods
	 */
	
	/**
	 * This method instantiate a new sensor given its {@link SMLDescription}. The SML description must be generated by
	 * calling the {@link SMLDescription#getXMLString()} on a valid <code>SMLDescription</code> object. The returned value is a 
	 * string representation of the sensor's SML description document (XML). If a sensor ID is specified in the SML document, 
	 * it will be ignored and replaced with a new one (the returned value). 
	 * @param xml the sensor's SML description document. It is generated by calling {@link SMLDescription#getXMLString()}
	 * @return a string representing the sensor identifier
	 * @throws SALDocumentException if the SML document is malformed
	 * @throws ConfigurationException if the sensor cannot be instantiated because of invalid configuration information
	 * in the SML document
	 */
	public String addSensor(String xml) throws SALDocumentException,  ConfigurationException;
	
	/**
	 * This method removes a sensor with the given identifier. Its configuration information is also removed from the
	 * configuration file.
	 * @param sid the sensor identifier
	 * @throws NotFoundException if a sensor with the given sensor ID cannot be found
	 */
	public void removeSensor(String sid) throws NotFoundException;
	
	/**
	 * This method returns a string representation of the SML descriptions document containing the configuration of all currently active sensors.
	 * An active sensor is one that has been connected at least once since startup. Note that an active
	 * sensor may not be currently connected (for instance if its protocol has been removed). The returned string
	 * can be used to create a {@link SMLDescriptions} object which facilitate the parsing of the XML document.
	 * @return the configuration of all active sensors as an XML document, from which a {@link SMLDescriptions}
	 * can be created to facilitate parsing.
	 */
	public String listActiveSensors();
	
	/**
	 * This method returns a string representation of the SML descriptions document containing the configuration of all known sensors.
	 * A known sensor is one that has its configuration stored in the sensor configuration file.
	 * Known sensors may or may not be currently connected, and may not have been connected at all since
	 * startup. The returned string can be used to create a {@link SMLDescriptions} object which facilitate the
	 * parsing of the XML document.
	 * @return the configuration of all known sensors as an XML document, from which a {@link SMLDescriptions}
	 * object can be created to facilitate parsing.
	 */
	public String listSensors();
	
	/**
	 * This method returns a string representation of the SML description document containing the configuration for a given sensor.
	 * The returned string can be used to create a {@link SMLDescription} object which facilitate the
	 * parsing of the XML document.
	 * @param sid the sensor ID
	 * @return the configuration of the given sensors as an XML document, from which a {@link SMLDescription}
	 * object can be created to facilitate parsing.
	 * @throws NotFoundException if no sensor matches the given identifier
	 */
	public String listSensor(String sid) throws NotFoundException;
	
	/**
	 * This method sets up a stream given a sensor and a command.
	 * The command will be sent to the sensor at fixed intervals in time (as specified
	 * in the {@link Command} object), creating a stream of {@link Response}s, sent
	 * back to the client. After setting up the stream, the {@link #startStream(StreamID)}
	 * method must be called with the returned value of this method (a {@link StreamID}), to start
	 * streaming, EXCEPT if the command is to be run only once, in which case, 
	 * the returned {@link StreamID} is null and {@link #startStream(StreamID)} 
	 * will be called automatically. 
	 * @param c the command to be executed. {@link Command}s are created using a 
	 * {@link CommandFactory} object.
	 * @param sid the target sensor identifier
	 * @return a {@link StreamID} which uniquely identifies this stream, or <code>null</codE>
	 * if the command will run only once, in which case, this method will also call 
	 * {@link #startStream(StreamID)}.
	 * @throws NotFoundException if the given sensor id does not match any existing sensor
	 * @throws SensorControlException if there is an error controlling the sensor. If this exception is raised,
	 * the cause of this exception will be linked to it and can be retrieved using <code>getCause()</code>  
	 */
	public StreamID setupStream(Command c, String sid) throws NotFoundException, SensorControlException;
	
	/**
	 * This method starts a stream previously setup using {@link #setupStream(Command, String)}.
	 * @param streamId the id of the stream as returned by {@link #setupStream(Command, String)}.
	 * @throws NotFoundException if the given stream Id has not been setup prior
	 * to calling this method.
	 * @throws SALRunTimeException if the stream has already been started once before.
	 */
	public void startStream(StreamID streamId) throws NotFoundException;
	
	/**
	 * This method stops and deletes a stream previously setup using {@link #setupStream(Command, String)}.
	 * @param streamId the id of the stream as returned by {@link #setupStream(Command, String)}.
	 * @throws NotFoundException if the given stream Id has not been setup prior
	 * to calling this method.
	 */
	public void terminateStream(StreamID streamId) throws NotFoundException;
	
	/**
	 * This method returns the a string representation of the CML descriptions document for a given sensor.
	 * The returned string can be used to create a {@link CMLDescriptions} object to facilitate parsing the
	 * XML document.
	 * @param sid the sensor identifier
	 * @return the CML document, from which a {@link CMLDescriptions} can be created to facilitate parsing.
	 * @throws NotFoundException if the given sensor ID does not match any existing sensor
	 */
	public String getCML(String sid) throws NotFoundException;
	
	/*
	 * Protocols-related methods 
	 */
	
	/**
	 * This method instantiates a new protocol given its PCML protocol configuration document (as generated
	 * by {@link ProtocolConfiguration#getXMLString()} . If successful, this method will also store the
	 * protocol's PCML configuration information in the platform configuration file
	 * @param xml a string version of the protocol's PCML protocol description document as generated by
	 * {@link ProtocolConfiguration#getXMLString()}
	 * @param loadSensors set to true if the sensor configuration file should be checked for sensors associated with 
	 * this protocol and create them.
	 * @throws ConfigurationException if the protocol cannot be instantiated because of invalid configuration information
	 * @throws SALDocumentException if the given PCML document is malformed  
	 */
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, SALDocumentException;
	
	/**
	 * This method removes a protocol given its ID. The protocol is first stopped so commands are no further 
	 * accepted. It then removes all associated sensors and their configuration if <code>removeSensors</code> is set to true.
	 * @param pid the protocol identifier
	 * @param removeSensor whether or not to remove the sensor configuration associated with this protocol from the config file
	 * @throws NotFoundException if the given protocol ID doesnt match any existing protocols
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws NotFoundException;
	
	/**
	 * This method lists the configuration of all existing protocols. The returned value is a string
	 * representation of a PCML protocol descriptions document, which can be used to create a 
	 * {@link ProtocolConfigurations} object to facilitate parsing.
	 * @return a string representation of a PCML document listing the protocols configuration, which can be used to create a 
	 * {@link ProtocolConfigurations} object to facilitate parsing.
	 */
	public String listProtocols();
	
	/*
	 * Event-related methods 
	 */
	/**
	 * This method registers an event handler. Whenever the producer <code>producerID</code> generates an event, the method
	 * {@link ClientEventHandler#handle(jcu.sal.common.events.Event)} will be called on the given EventHandler <code>eh</code>.
	 * A Producers ID is usually a protocol name. However, three special producers also exist:
	 *  {@link Constants#SENSOR_MANAGER_PRODUCER_ID} which generates
	 * <code>SensorNodeEvent</code> events when sensors are created and deleted, {@link Constants#SENSOR_MANAGER_PRODUCER_ID} which
	 * generates <code>ProtocolListEvent</code> events when protocols are created and deleted, {@link Constants#SENSOR_MANAGER_PRODUCER_ID}
	 * which generates <code>SensorStateEvent</code> events when a sensor is connected or disconnected.  
	 * @param eh an instance of a class implementing the EventHandler interface which will receive events.
	 * @param producerID the identifier of a protocol or the special identifiers "SensorManager", "ProtocolManager" or "SensorState"
	 * @throws NotFoundException if the given producerID doesnt exist
	 */
	public void registerEventHandler(ClientEventHandler eh, String producerID) throws NotFoundException;
	
	/**
	 * This method unregisters an EventHandler previously registered with <code>registerEventHandler()</code>
	 * @param eh the EventHandler to re be removed
	 * @param producerID the producer to which it is associated
	 * @throws NotFoundException if the handler can not be found/removed
	 */
	public void unregisterEventHandler(ClientEventHandler eh, String producerID) throws NotFoundException;
	
}
