package jcu.sal.client.gui;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;

public class RMIClientControllerImpl implements RMIClientController {
	
	private RMISALAgent agent;
	private Registry agentRegistry, ourRegistry;
	private String RMIname;

	@Override
	public void addProtocol(String xml, boolean loadSensors)
			throws ConfigurationException, SALDocumentException,
			RemoteException {
		agent.addProtocol(xml, loadSensors);

	}

	@Override
	public String addSensor(String xml) throws SALDocumentException,
			ConfigurationException, RemoteException {
		return agent.addSensor(xml);
	}

	@Override
	public void connect(String rmiName, String ipAddress, String ourIpAddress)
			throws ConfigurationException, RemoteException {
		agentRegistry = LocateRegistry.getRegistry(ipAddress);
		ourRegistry = LocateRegistry.getRegistry(ourIpAddress);
		RMIname = rmiName;
		try {
			agent = (RMISALAgent) agentRegistry.lookup(RMISALAgent.RMI_STUB_NAME);
		} catch (Exception e) {
			System.out.println("Cannot find SAL Agent in its registry");
			e.printStackTrace();
			throw new RemoteException();
		}
		
		try {
			agent.registerClient(RMIname, ipAddress);
		} catch (RemoteException e) {
			System.out.println("Cannot register with SAL Agent");
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}

	@Override
	public void disconnect(String rmiName) throws ConfigurationException,
			RemoteException {
		try {
			agent.unregisterClient(RMIname);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		agent = null;
		System.gc();

	}

	@Override
	public Response execute(RMICommand c, String sid) throws NotFoundException,
			SensorControlException, RemoteException {
		return agent.execute(c, sid);
	}

	@Override
	public String getCML(String sid) throws NotFoundException, RemoteException {
		return agent.getCML(sid);
	}

	@Override
	public String listActiveSensors() throws RemoteException {
		return agent.listActiveSensors();
	}

	@Override
	public String listProtocols() throws RemoteException {
		return agent.listProtocols();
	}

	@Override
	public String listSensors() throws RemoteException {
		return agent.listSensors();
	}

	@Override
	public void removeProtocol(String pid, boolean removeSensors)
			throws NotFoundException, RemoteException {
		agent.removeProtocol(pid, removeSensors);

	}

	@Override
	public void removeSensor(String sid) throws NotFoundException,
			RemoteException {
		agent.removeSensor(sid);

	}
	
	public void bind(String name, Remote r) throws AccessException, RemoteException{
		ourRegistry.rebind(name, UnicastRemoteObject.exportObject(r, 0));
	}
	
	public void unbind(String name) throws AccessException, RemoteException{
		try {
			ourRegistry.unbind(name);
		} catch (NotBoundException e) {
			System.out.println("No temote method with name "+name+" to unbind");
		}

	}

	@Override
	public void registerEventHandler(String objName,
			String producerID, Remote r) throws NotFoundException,
			RemoteException {
		agent.registerEventHandler(RMIname, objName, producerID);
		
	}

	@Override
	public void unregisterEventHandler(String objName,
			String producerID) throws NotFoundException, RemoteException {
		agent.unregisterEventHandler(RMIname, objName, producerID);		
	}

	@Override
	public String listSensor(String sid) throws NotFoundException,
			RemoteException {
		return agent.listSensor(sid);
	}

}
