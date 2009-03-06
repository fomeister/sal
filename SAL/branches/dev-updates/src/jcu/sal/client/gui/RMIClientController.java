package jcu.sal.client.gui;

import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import jcu.sal.common.Constants;
import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SensorControlException;

public interface RMIClientController extends ClientController{
	
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
	

	/**
	 * This method instructs a sensor identified by sid to execute a given command c
	 * @param c the command
	 * @param sid the target sensor identifier
	 * @return the result
	 * @throws NotFoundException if the given sensor id doesnt match any existing sensor
	 * @throws SensorControlException if there is an error controlling the sensor. If this exception is raised,
	 * the cause of this exception will be linked to it and can be retrieved using <code>getCause()</code>  
	 */
	public Response execute(RMICommand c, String sid) throws NotFoundException, SensorControlException, RemoteException;
	
	/*
	 * Event handling
	 */
	
	/**
	 * This method exports the RMI Remote object r, with the given name
	 * @param name the name of the exported Remote object in the RMI registry
	 * @param r the RMI remote object to be exported
	 * @thows AccessException
	 * @throws RemoteException
	 */
	public void bind(String name, Remote r) throws AccessException, RemoteException;
	
	/**
	 * This method unbinds a Remote object previously exported with <code>bind<code> 
	 * @param name the name of the Remote object 
	 * @throws AccessException
	 * @throws RemoteException
	 */
	public void unbind(String name) throws AccessException, RemoteException;
	
	/**
	 * This method registers an RMI event handler. Whenever the producer <code>producerID</code> generates an event, the method
	 * <code>handle</code> will be called on the RMI EventHandler identified by <code>objName</code> with a matching Event object as the sole argument.
	 * A Producers ID is a protocol name. Three special producers also exist: {@link Constants#SENSOR_MANAGER_PRODUCER_ID} which generates
	 * <code>SensorNodeEvent</code> events when sensors are created and deleted, {@link Constants#SENSOR_MANAGER_PRODUCER_ID} which
	 * generates <code>ProtocolListEvent</code> events when protocols are created and deleted, {@link Constants#SENSOR_MANAGER_PRODUCER_ID}
	 * which generates <code>SensorStateEvent</code> events when a sensor is connected or disconnected.  
	 * @param objName the name of the RMI event handler to lookup in the RMI registry.
	 * @param producerID the identifier of a protocol or the special identifiers 
	 * {@link Constants#SENSOR_MANAGER_PRODUCER_ID}, {@link Constants#SENSOR_MANAGER_PRODUCER_ID} or {@link Constants#SENSOR_MANAGER_PRODUCER_ID}
	 * @throws NotFoundException if the given producerID doesnt exist
	 * @throws RemoteException if the RMI event handler object cant be found in the RMI registry
	 */
	public void registerEventHandler(String objName, String producerID) throws NotFoundException, RemoteException;
	
	/**
	 * This method unregisters an EventHandler previously registered with <code>registerEventHandler()</code>
	 * @param objName the name of the object to lookup in the RMI registry.
	 * @param producerID the producer to which it is associated
	 * @throws NotFoundException if the handler can not be found/removed
	 * @throws RemoteException if the RMI event handler object cant be found in the RMI registry
	 */
	public void unregisterEventHandler(String objName, String producerID) throws NotFoundException, RemoteException;
	
}
