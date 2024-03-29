package jcu.sal.agent;

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

import jcu.sal.common.CommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.cml.RMIStreamCallback;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.EventHandler;
import jcu.sal.common.events.RMIEventHandler;

public class RMIAgentImpl implements RMISALAgent {
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
				System.out.println("Cant find object '"+objName+"' at "+ip+":"+port);
				e.printStackTrace();
				throw new RemoteException();
			}
		}
	}
	
	private SALAgentImpl agent;
	private Map<String, SALClient> clients;
	
	public RMIAgentImpl(){
		clients = new Hashtable<String, SALClient>();
		agent = new SALAgentImpl();
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
				target.put(name, new ProxyStreamCallback((RMIStreamCallback )clients.get(l.get(0)).getRef(l.get(1)), l.get(0), l.get(1) ));
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
		System.out.println("Client '"+name+"' registered");
	}

	public void unregisterClient(String name) throws ConfigurationException {
		synchronized(clients){
			if(!clients.containsKey(name))
				throw new ConfigurationException();
			clients.remove(name);
		}
		System.out.println("Client '"+name+"' unregistered");
	}

	public void registerEventHandler(String rmiName, String objName, String producerID) throws ConfigurationException, RemoteException {
		EventHandler eh;
		synchronized (clients) {
			if(!clients.containsKey(rmiName))
				throw new ConfigurationException();
			
			try {
				eh = new ProxyEventHandler((RMIEventHandler) clients.get(rmiName).getRef(objName),rmiName, objName,producerID);
			} catch (RemoteException e) {
				System.out.println("Cant find specified object "+objName+" for client "+rmiName);
				throw e;
			}

			agent.registerEventHandler(eh, producerID);
			
			/* null any ref to remote objects for GC*/
			eh = null;
		}
	}
	
	public void unregisterEventHandler(String rmiName, String objName, String producerID) throws ConfigurationException, RemoteException {
		EventHandler eh;
		synchronized (clients) {
			if(!clients.containsKey(rmiName))
				throw new ConfigurationException();
			
			try {
				eh = new ProxyEventHandler((RMIEventHandler) clients.get(rmiName).getRef(objName), rmiName, objName,producerID);
			} catch (RemoteException e) {
				System.out.println("Cant find specified object "+objName+" for client "+rmiName);
				throw e;
			} 

			agent.unregisterEventHandler(eh, producerID);
			
			/* null any ref to remote objects for GC*/
			eh = null;
		}
	}

	public void removeProtocol(String pid, boolean removeSensors) throws ConfigurationException {
		agent.removeProtocol(pid, removeSensors);

	}

	public void removeSensor(String sid) throws ConfigurationException {
		agent.removeSensor(sid);
	}
	
	public static void main (String args[]) throws ConfigurationException, IOException {
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: the IP address of our registry - 2: the platform configuration file - 3: the sensor configuration file");
			System.exit(1);
		}
		Registry registry = LocateRegistry.getRegistry(args[0]);
		RMIAgentImpl agent = new RMIAgentImpl();
		agent.start(args[1], args[2]);
		RMISALAgent stub = (RMISALAgent) UnicastRemoteObject.exportObject((RMISALAgent) agent, 0);
		try {
			registry.rebind(RMI_STUB_NAME, stub);
			System.out.println("RMI SAL Agent ready. Press <Enter> to quit");
			System.in.read();
		} catch (RemoteException e) {
			System.out.println("Error binding agent to the RMI registry");
			e.printStackTrace();
		}
		agent.stop();
		System.exit(0);
	}
	
	private static class ProxyEventHandler implements EventHandler{
		private RMIEventHandler r;
		private String rmiName, objName, producerID;
		public ProxyEventHandler(RMIEventHandler r, String rmiName, String objName, String producerID){
			this.r = r;
			this.rmiName = rmiName;
			this.objName = objName;
			this.producerID = producerID;
		}

		@Override
		public void handle(Event e) {
			try {
				r.handle(e);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		}
		@Override
		public int hashCode() {
			final int PRIME = 57;
			int result = 1;
			result = PRIME * result + ((objName == null) ? 0 : objName.hashCode());
			result = PRIME * result + ((producerID == null) ? 0 : producerID.hashCode());
			result = PRIME * result + ((rmiName == null) ? 0 : rmiName.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ProxyEventHandler other = (ProxyEventHandler) obj;
			if (objName == null) {
				if (other.objName != null)
					return false;
			} else if (!objName.equals(other.objName))
				return false;
			if (producerID == null) {
				if (other.producerID != null)
					return false;
			} else if (!producerID.equals(other.producerID))
				return false;
			if (rmiName == null) {
				if (other.rmiName != null)
					return false;
			} else if (!rmiName.equals(other.rmiName))
				return false;
			return true;
		}		
	}
	
	private static class ProxyStreamCallback implements StreamCallback{
		private RMIStreamCallback s;
		private String rmiName, objName;
		public ProxyStreamCallback(RMIStreamCallback s, String rmiName, String objName){
			this.s = s;
			this.rmiName = rmiName;
			this.objName = objName;
		}

		@Override
		public void collect(Response r) throws IOException {
			try {
				s.collect(r);
			} catch (Throwable e1) {
				throw new IOException();
			}			
		}
		
		@Override
		public int hashCode() {
			final int PRIME = 13;
			int result = 1;
			result = PRIME * result + ((objName == null) ? 0 : objName.hashCode());
			result = PRIME * result + ((rmiName == null) ? 0 : rmiName.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ProxyEventHandler other = (ProxyEventHandler) obj;
			if (objName == null) {
				if (other.objName != null)
					return false;
			} else if (!objName.equals(other.objName))
				return false;
			if (rmiName == null) {
				if (other.rmiName != null)
					return false;
			} else if (!rmiName.equals(other.rmiName))
				return false;
			return true;
		}
	}


}
