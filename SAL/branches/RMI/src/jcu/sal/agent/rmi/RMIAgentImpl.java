package jcu.sal.agent.rmi;

import java.io.IOException;
import java.io.NotActiveException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.agent.SALAgent;
import jcu.sal.common.CommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.events.EventHandler;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

public class RMIAgentImpl implements RMIAgent {
	private static class SALClient {
		public static int RMI_REGISTRY_PORT = 1099;
		private int port;
		private String ip;
		private Registry registry;
		
		public SALClient(String ip, int port) throws RemoteException{
			registry = LocateRegistry.getRegistry(ip);
			this.ip = ip;
			this.port = port;
		}
		
		public SALClient(String ip) throws RemoteException{
			this(ip, RMI_REGISTRY_PORT);
		}
		
		public Remote getRef(String objName) throws RemoteException{
			try {
				return registry.lookup(objName);
			} catch (Exception e) {
				logger.error("Cant find object '"+objName+"' at "+ip+":"+port);
				e.printStackTrace();
				throw new RemoteException();
			}
		}
	}
	
	public static String RMI_STUB_NAME = "RMI SAL Agent";
	
	private SALAgent agent;
	private Map<String, SALClient> clients;
	private static Logger logger = Logger.getLogger(RMIAgentImpl.class);
	
	public RMIAgentImpl(){
		Slog.setupLogger(logger);
		clients = new Hashtable<String, SALClient>();
		agent = new SALAgent();
	}
	
	public void start(String pc, String sc) throws ConfigurationException{
		agent.start(pc,sc);
	}
	
	public void stop(){
		agent.stop();
	}

	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, ParserConfigurationException {
		agent.addProtocol(xml, loadSensors);
	}

	public String addSensor(String xml) throws ConfigurationException, ParserConfigurationException {
		return agent.addSensor(xml);
	}

	public Response execute(RMICommand c, String sid) throws ConfigurationException, BadAttributeValueExpException,	NotActiveException {
		Map<String,List<String>> src = c.getRMIStreamCallBack();
		Map<String, StreamCallback> target = new Hashtable<String, StreamCallback>();
		Iterator<String> i = src.keySet().iterator();
		List<String> l;
		String name;
		
		while(i.hasNext()){
			name = i.next();
			l = src.get(name);
			try {
				target.put(name, (StreamCallback) clients.get(l.get(0)).getRef(l.get(1)));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ConfigurationException();
			}	
		}
		
		return agent.execute(CommandFactory.getCommand(c, target), sid);
	}

	public String getCML(String sid) throws ConfigurationException, NotActiveException{
		return agent.getCML(sid);
	}

	public String listActiveSensors(){
		return agent.listActiveSensors();
	}

	public String listSensors() {
		return agent.listSensors();
	}

	public void registerClient(String name, String ipAddress) throws ConfigurationException, RemoteException {
		SALClient c = new SALClient(ipAddress);
		synchronized(clients){
			if(clients.containsKey(name))
				throw new ConfigurationException();
			clients.put(name, c);
		}
	}

	public void unregisterClient(String name) throws ConfigurationException {
		synchronized(clients){
			if(!clients.containsKey(name))
				throw new ConfigurationException();
			clients.remove(name);
		}
	}

	public void registerEventHandler(String rmiName, String objName, String producerID) throws ConfigurationException, RemoteException {
		EventHandler eh;
		synchronized (clients) {
			if(!clients.containsKey(rmiName))
				throw new ConfigurationException();
			
			try {
				eh = (EventHandler) clients.get(rmiName).getRef(objName);
			} catch (RemoteException e) {
				logger.error("Cant find specified object "+objName+" for client "+rmiName);
				throw e;
			}

			agent.registerEventHandler(eh, producerID);
		}
	}
	
	public void unregisterEventHandler(String rmiName, String objName, String producerID) throws ConfigurationException, RemoteException {
		EventHandler eh;
		synchronized (clients) {
			if(!clients.containsKey(rmiName))
				throw new ConfigurationException();
			
			try {
				eh = (EventHandler) clients.get(rmiName).getRef(objName);
			} catch (RemoteException e) {
				logger.error("Cant find specified object "+objName+" for client "+rmiName);
				throw e;
			} 

			agent.unregisterEventHandler(eh, producerID);
		}
	}

	public void removeProtocol(String pid, boolean removeSensors) throws ConfigurationException {
		agent.removeProtocol(pid, removeSensors);

	}

	public void removeSensor(String sid) throws ConfigurationException {
		agent.removeSensor(sid);
	}
	
	public static void main (String args[]) throws ConfigurationException, IOException {
		Registry registry = LocateRegistry.getRegistry(args[0]);
		RMIAgentImpl agent = new RMIAgentImpl();
		RMIAgent stub = (RMIAgent) UnicastRemoteObject.exportObject(agent, 0);
		agent.start(args[1], args[2]);
		registry.rebind(RMI_STUB_NAME, stub);
		System.out.println("RMI SAL Agent ready. Press <Enter> to quit");
		System.in.read();
		agent.stop();
		System.exit(0);
	}

}
