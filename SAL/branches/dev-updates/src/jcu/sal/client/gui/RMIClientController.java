package jcu.sal.client.gui;

import java.rmi.RemoteException;

import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.exceptions.ConfigurationException;

public interface RMIClientController extends ClientController{
	
	/**
	 * This method connects to an RMI SAL agent, registers with it and returns a reference to the agent. 
	 * @param rmiName A unique name associated with the RMI Client. The name is chosen by the caller and must be used in subsequent calls
	 * to execute() and {un}registerEventHandler().
	 * @param ipAddress the IP address of the agent RMI registry
	 * @param ourIpAddress the IP address of the SAL client's RMI registry  
	 * @return a reference to an RMI {@link SALAgent}
	 * @throws ConfigurationException if this name already exists
	 * @throws RemoteException if the registry cant be reached
	 */
	public SALAgent connect(String rmiName, String ipAddress, String ourIpAddress) throws ConfigurationException, RemoteException;
	
	/**
	 * This method disconnects and release a reference to an RMI SAL agent created with
	 * {@link #connect(String, String, String)}.
	 * @param a a reference to an RMI SAL agent
	 */
	public void disconnect(SALAgent a);

}
