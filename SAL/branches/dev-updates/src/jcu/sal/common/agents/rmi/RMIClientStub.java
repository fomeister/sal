package jcu.sal.common.agents.rmi;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Response;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.events.ClientEventHandler;
import jcu.sal.common.events.Event;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.exceptions.SensorControlException;

public class RMIClientStub implements SALAgent{

//	private static Logger logger = Logger.getLogger(RMIClientStub.class);
//	static {Slog.setupLogger(logger);}
	
	private String rmiName;
	private RMIAgent agent;
	private Registry agentRegistry, ourRegistry;
	private Hashtable<String,RMIEventHandlerProxy> handlers;
	
	/**
	 * This method creates an RMI client stub.
	 * @param agentIP the ip address of the agent's RMI registry
	 * @param ourIP the ip address of this client's RMI registry
	 * @param RMIname the RMI name this client will use to register with the agent, and the 
	 * name this client will use to export RMI object in its own registry.
	 * @throws RemoteException if there is an RMI error (looking up either registries,
	 * getting a remote reference to the agent, or exporting our own RMI methods)
	 * @throws ConfigurationException if we cannot register with the SAL agent. Most likely
	 * the supplied RMI name is probably taken.
	 */
	public RMIClientStub(String agentIP, String ourIP, String RMIname) throws RemoteException, ConfigurationException{
		agentRegistry = LocateRegistry.getRegistry(agentIP);
		ourRegistry = LocateRegistry.getRegistry(ourIP);
		rmiName = RMIname;
	
		connect(agentIP, ourIP);
		
		handlers = new Hashtable<String,RMIEventHandlerProxy>();
	}
	
	
	private void connect(String agentIP, String ourIP) throws RemoteException, ConfigurationException{	
		//get agent
		try {
			agent = (RMIAgent) agentRegistry.lookup(RMIAgent.RMI_STUB_NAME);
		} catch (RemoteException e) {
//			logger.error("Cannot get a handle on the SAL Agent ");
			throw e;
		} catch (NotBoundException e) {
//			logger.error("Cannot find the SAL agent in its registry");
			throw new RemoteException("Cannot find the SAL agent in its registry");
		}
		
		//register with agent
		try {
			agent.registerClient(rmiName, ourIP);
		} catch (RemoteException e) {
//			logger.error("Cannot register with SAL Agent");
		}
	}
	
	public void release(){
		try {
			agent.unregisterClient(rmiName);
		} catch (Throwable t) {
//			logger.error("Error unregistering with agent");
		}
		
		agent = null;
		System.gc();
	}

	@Override
	public void addProtocol(String xml, boolean loadSensors)
			throws ConfigurationException, SALDocumentException {
		try {
			agent.addProtocol(xml, loadSensors);
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}		
	}

	@Override
	public String addSensor(String xml) throws SALDocumentException,
			ConfigurationException {
		try {
			return agent.addSensor(xml);
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
	}

	@Override
	public Response execute(Command c, String sid) throws NotFoundException,
			SensorControlException {
		try {
			return agent.execute(
					RMICommandFactory.getCommand(c, adjustCallbacks(c.getStreamCallBack()))
					, sid);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
	}
	
	@Override
	public String getCML(String sid) throws NotFoundException {
		try {
			return agent.getCML(sid);
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
	}

	@Override
	public String listActiveSensors() {
		try {
			return agent.listActiveSensors();
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
	}

	@Override
	public String listProtocols() {
		try {
			return agent.listProtocols();
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
	}

	@Override
	public String listSensor(String sid) throws NotFoundException {
		try {
			return agent.listSensor(sid);
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
	}

	@Override
	public String listSensors() {
		try {
			return agent.listSensors();
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
	}

	@Override
	public void removeProtocol(String pid, boolean removeSensors)
			throws NotFoundException {
		try {
			agent.removeProtocol(pid, removeSensors);
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
		
	}

	@Override
	public void removeSensor(String sid) throws NotFoundException {
		try {
			agent.removeSensor(sid);
		} catch (RemoteException e) {
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
		
	}
	
	@Override
	public void registerEventHandler(ClientEventHandler eh, String producerID)
			throws NotFoundException {
		RMIEventHandlerProxy proxy = null;
		try {
			proxy = new RMIEventHandlerProxy(eh, this, ourRegistry); 
			synchronized(handlers){
				agent.registerEventHandler(rmiName, proxy.getName(), producerID);
				handlers.put(producerID, proxy);
			}
		} catch (RemoteException e) {
			if(proxy!=null)
				proxy.removeRMIEventHandler();
			
			throw new SALRunTimeException("RMI exception "+e.getMessage());
		}
	}

	@Override
	public void unregisterEventHandler(ClientEventHandler eh, String producerID)
			throws NotFoundException {

		synchronized(handlers){
			try {
				agent.unregisterEventHandler(rmiName, handlers.get(producerID).getName(), producerID);
			} catch (RemoteException e) {
				throw new SALRunTimeException("RMI exception "+e.getMessage());
			}
			if(handlers.containsKey(producerID)){
				handlers.get(producerID).removeRMIEventHandler();
				handlers.remove(producerID);
			}
		}
	}

	/**
	 * This method returns a list of string representing an RMI callback argument. The returned
	 * value is as follow
	 * The key of the map is the argument name.
	 * The string at position 0 in the list is the rmiName (the name the Client used when calling RMISALAgent.registerClient()). 
	 * The string at position 1 in the list is the objName (the name of the object in the RMI registry representing the callback object).
	 * @param cb a map of argument names and associated {@link StreamCallback} objects
	 * @return (see above)
	 * @throws RemoteException if there is an error registering an {@link RMIStreamCallback} object with RMI
	 */
	private Map<String, List<String>> adjustCallbacks(Map<String, StreamCallback> cb) throws RemoteException{
		Hashtable<String, List<String>> map = new Hashtable<String, List<String>>();
		Vector<String> v;
		//for each stream callback object
		for(String name : cb.keySet()){
			//create a CallBackProxy object (registration with RMI is 
			//handled by the object itself)
			v = new Vector<String>();
			v.add(0, rmiName);
			v.add(1, new RMICallbackProxy(cb.get(name), ourRegistry).getRMIName());
			map.put(name, v);
		}
		
		return map;
	}
	
	/**
	 * this class acts as an adapter around a {@link ClientEventHandler} object, transforming
	 * it into an {@link RMIEventHandler} object.
	 * @author gilles
	 *
	 */
	public static class RMIEventHandlerProxy implements RMIEventHandler{
//		private static Logger logger = Logger.getLogger(RMIEventHandlerProxy.class);
//		static {Slog.setupLogger(logger);}
		
		ClientEventHandler client;
		SALAgent agent;
		Registry registry;
		String name;
		
		public RMIEventHandlerProxy(ClientEventHandler c, SALAgent a, Registry r) throws RemoteException{
			client = c;
			agent = a;
			registry = r;
			name = toString();
			
			//register ourselves with our registry
			//logger.debug("Binding event handler "+name);
			try {
				registry.rebind(name, UnicastRemoteObject.exportObject(this, 0));
			} catch (RemoteException e) {
				//logger.error("Error binding with RMI registry");
				throw e;
			}
		}
		
		public void removeRMIEventHandler(){
			try {
				//logger.debug("Unbinding RMI event handler "+name);
				registry.unbind(name);
			} catch (Throwable t) {
				//logger.error("Error unbinding event handler "+name);
			} 
		}

		@Override
		public void handle(Event e) throws RemoteException {
			try {
				client.handle(e, agent);
			} catch (IOException e1) {
				removeRMIEventHandler();
				throw new RemoteException("Error dispatching the event:\n"+e1.getMessage());
			}
		}
		
		public String getName(){
			return name;
		}
	}
	
	
	/**
	 * this class acts as an adapter around a {@link StreamCallback} object, transforming
	 * it into an {@link RMIStreamCallback} object.
	 * @author gilles
	 *
	 */
	public class RMICallbackProxy implements RMIStreamCallback{
//		private static Logger logger = Logger.getLogger(RMICallbackProxy.class);
//		static {Slog.setupLogger(logger);}
		private StreamCallback c;
		private String name;
		private Registry registry;

		/**
		 * This method builds an {@link RMIStreamCallback} object from a
		 * {@link StreamCallback} object.
		 * @param c the {@link StreamCallback} object.
		 * @throws RemoteException if there is an error registering
		 * the new {@link RMIStreamCallback} object with the RMI registry.
		 */
		public RMICallbackProxy(StreamCallback cb,Registry r) throws RemoteException{
			c = cb;
			name = toString();
			registry = r;

			//register ourselves with our registry
//			logger.debug("Binding RMI callback "+name);
			registry.rebind(name, UnicastRemoteObject.exportObject(this, 0));
		}

		@Override
		public void collect(Response r) throws RemoteException {
			try {
				if(r.hasException())
					removeRMIStreamCallBack();
				c.collect(r);
			} catch (IOException e) {
//				logger.debug("Error collecting response");
				removeRMIStreamCallBack();
				throw new RemoteException("Error while collecting response",e);
			}			
		}

		private void removeRMIStreamCallBack(){
			try {
//				logger.debug("Unbinding RMI callback "+name);
				registry.unbind(name);
			} catch (Throwable t) {
//				logger.error("Error unbinding RMI callback "+name);
			} 
		}

		/**
		 * This method returns the name of this {@link RMIStreamCallback}
		 * as registered with the RMI registry.
		 * @return the RMI name of this {@link RMIStreamCallback}
		 */
		public String getRMIName(){
			return name;
		}
	}
}
