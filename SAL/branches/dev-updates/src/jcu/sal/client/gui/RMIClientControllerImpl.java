package jcu.sal.client.gui;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import jcu.sal.agent.RMISALAgent;
import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
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
			throws ConfigurationException, SALDocumentException {
		try {
			agent.addProtocol(xml, loadSensors);
		} catch (RemoteException e) {
			throw new AgentException("Cannot add protocol", e);
		}

	}

	@Override
	public String addSensor(String xml) throws SALDocumentException,
			ConfigurationException {
		try {
			return agent.addSensor(xml);
		} catch (RemoteException e) {
			throw new AgentException("Cannot add sensor", e);
		}
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
	public String getCML(String sid) throws NotFoundException {
		try {
			return agent.getCML(sid);
		} catch (RemoteException e) {
			throw new AgentException("Cannot get CML document", e);
		}
	}

	@Override
	public String listActiveSensors() {
		try {
			return agent.listActiveSensors();
		} catch (RemoteException e) {
			throw new AgentException("Cannot list active sensors", e);
		}
	}

	@Override
	public String listProtocols() {
		try {
			return agent.listProtocols();
		} catch (RemoteException e) {
			throw new AgentException("Cannot list protocols", e);
		}
	}

	@Override
	public String listSensors() {
		try {
			return agent.listSensors();
		} catch (RemoteException e) {
			throw new AgentException("Cannot list sensors", e);
		}
	}

	@Override
	public void removeProtocol(String pid, boolean removeSensors)
			throws NotFoundException {
		try {
			agent.removeProtocol(pid, removeSensors);
		} catch (RemoteException e) {
			throw new AgentException("Cannot remove protocol", e);
		}

	}

	@Override
	public void removeSensor(String sid) throws NotFoundException {
		try {
			agent.removeSensor(sid);
		} catch (RemoteException e) {
			throw new AgentException("Cannot remove sensor", e);
		}

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
			String producerID) throws NotFoundException,
			AgentException {
		try {
			agent.registerEventHandler(RMIname, objName, producerID);
		} catch (RemoteException e) {
			throw new AgentException("Cannot register event handler", e);
		}
		
	}

	@Override
	public void unregisterEventHandler(String objName,
			String producerID) throws NotFoundException, RemoteException {
		agent.unregisterEventHandler(RMIname, objName, producerID);		
	}

	@Override
	public String listSensor(String sid) throws NotFoundException{
		try {
			return agent.listSensor(sid);
		} catch (RemoteException e) {
			throw new AgentException("Cannot list sensor", e);
		}
	}

}
