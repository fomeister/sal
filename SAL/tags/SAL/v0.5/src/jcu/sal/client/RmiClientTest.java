package jcu.sal.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.Constants;
import jcu.sal.common.RMICommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.RMIStreamCallback;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.RMIEventHandler;
import jcu.sal.utils.XMLhelper;

import org.w3c.dom.NodeList;

public class RmiClientTest implements RMIEventHandler, RMIStreamCallback{
	public static int RUN_LENGTH=120*1000;
	private Map<String,Control> sensors;
	private Vector<Thread> threads;
	private RMISALAgent agent;
	private Registry agentRegistry, ourRegistry;
	private String RMIname;
	
	private class Control implements Runnable{
		private RMICommand firstCommand, command, endCommand;
		private int interval;
		private long  lastRun;
		private String sid, description;
		private int count;
		private long ts;
		
		public Control(RMICommand f,RMICommand c, RMICommand e, int t, String s, String d){
			firstCommand = f;
			command = c;
			sid = s;
			description = d;
			endCommand = e;
			interval = t*1000;
			count = 0;
			ts = 0;
		}
		
		public Control(RMICommand c, int t, String s, String d){
			this(c,c,c,t,s,d);
		}
		
		public void run(){
			Random r = new Random();
			Long l = (long) r.nextInt(5)*1000;
			try {
				Thread.sleep(l);
				runFirst();
				while(!Thread.interrupted()){
					Thread.sleep(interval);
					runCommand();
				}
			} catch (InterruptedException e){}
			runLast();
		}
		
		private void  runCommand(){
			runCommand(command,sid);
		}
		
		private void runLast(){
			runCommand(endCommand, sid);
		}
		
		private void runFirst(){
			runCommand(firstCommand, sid);
		}
		
		private void runCommand(RMICommand c,String sid){
			long t;
			if(c!=null) {
				try {
					t = System.currentTimeMillis();
					//System.out.println("Sending "+c.getCID()+" to sid: "+sid+ " ("+description+") - ts: "+t);
					agent.execute(c, sid);
					System.out.println("Sent  "+c.getCID()+" to sid: "+sid+ " ("+description+") - exe: "+(System.currentTimeMillis()-t)+" - delta: "+(t-lastRun) );
					lastRun = System.currentTimeMillis();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}	
	
	public RmiClientTest(String rmiName, String agentRMIRegIP, String ourIP) throws RemoteException {
		agentRegistry = LocateRegistry.getRegistry(agentRMIRegIP);
		ourRegistry = LocateRegistry.getRegistry(ourIP);
		sensors = new Hashtable<String,Control>();
		threads = new Vector<Thread>();
		RMIname = rmiName;
		try {
			agent = (RMISALAgent) agentRegistry.lookup(RMISALAgent.RMI_STUB_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException();
		}
	}
	
	
	public void start(String ourRmiIP) throws ConfigurationException{
		
		try {
			agent.registerClient(RMIname, ourRmiIP);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
		try {
			export(RMIname, this);
		} catch (AccessException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
		
		try {
			agent.registerEventHandler(RMIname, RMIname, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(RMIname, RMIname, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(RMIname, RMIname, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	
	public void populateSensorList() throws ConfigurationException{
		String sid, pName, str;
		RMICommandFactory cf;
		RMICommand c = null;
		ArgumentType t;
				
		try {
		NodeList nl = XMLhelper.getNodeList("/SAL/SensorConfiguration/Sensor", XMLhelper.createDocument(agent.listActiveSensors()));
			for (int i = 0; i < nl.getLength(); i++) {
				sid = XMLhelper.getAttributeFromName("sid", nl.item(i));
				System.out.print("Found sensor SID: "+sid);
				System.out.print(" - "+XMLhelper.getAttributeFromName("//Sensor[@sid=\""+sid+"\"]/parameters/Param[@name=\"Address\"]", "value", nl.item(i)));
				pName = XMLhelper.getAttributeFromName("//Sensor[@sid=\""+sid+"\"]/parameters/Param[@name=\"ProtocolName\"]", "value", nl.item(i));
				if(pName.indexOf("v4l")!=-1) { //|| pName.indexOf("v4l-/dev/video1")!=-1 || pName.indexOf("v4l-/dev/video4")!=-1) {
					System.out.println(" - type : V4L");
					cf = new RMICommandFactory(XMLhelper.createDocument(agent.getCML(String.valueOf(sid))),102);
					
					Iterator<String> e = cf.listMissingArgNames().iterator();
					while(e.hasNext()){
						str = e.next();
						t = cf.getArgType(str);
						if(t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
							cf.addArgumentCallback(str,RMIname, RMIname);	
						}
					}
					c = cf.getCommand();
					
					cf = new RMICommandFactory(XMLhelper.createDocument(agent.getCML(String.valueOf(sid))),103);				
					e = cf.listMissingArgNames().iterator();
					while(e.hasNext()){
						str = e.next();
						t = cf.getArgType(str);
						if(t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
							cf.addArgumentCallback(str,RMIname, RMIname);	
						}
					}
					sensors.put(sid,new Control(c,null,cf.getCommand(),0, sid, XMLhelper.getAttributeFromName("//Sensor[@sid=\""+sid+"\"]/parameters/Param[@name=\"ProtocolName\"]", "value", nl.item(i))));
	
				} else {
					System.out.println(" - type : NON-V4L");
					cf = new RMICommandFactory(XMLhelper.createDocument(agent.getCML(String.valueOf(sid))),100);
					sensors.put(sid,new Control(cf.getCommand(),5, sid, XMLhelper.getAttributeFromName("//Sensor[@sid=\""+sid+"\"]/parameters/Param[@name=\"Address\"]", "value", nl.item(i))));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	public void run(){
		Iterator<Control> i;
		Iterator<Thread> it;
		Thread t;
		
		try {
			populateSensorList();
			
			//Starts all control threads 
			i = sensors.values().iterator();
			while(i.hasNext()){
				t = new Thread(i.next());
				t.start();
				threads.add(t);
			}
			
			Thread.sleep(RUN_LENGTH);
			
			//Stops all the threads
			it = threads.iterator();
			while(it.hasNext())
				it.next().interrupt();
			
			//Joins all the threads
			it = threads.iterator();
			while(it.hasNext())
				it.next().join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void stop() throws RemoteException{
		try {
			agent.unregisterEventHandler(RMIname, RMIname, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(RMIname, RMIname, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(RMIname, RMIname, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			agent.unregisterClient(RMIname);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		agent = null;
		System.gc();
		
	}
	
	
	public static void main(String [] args) throws RemoteException, NotBoundException{
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: our RMI name - 2: the IP address of our agentRegistry - 3: the IP address of the Agent agentRegistry");
			System.exit(1);
		}
		
		RmiClientTest c = new RmiClientTest(args[0], args[2], args[1]);
		try {
			c.start(args[1]);
			c.run();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try{c.stop();} catch(RemoteException e){}
			System.out.println("Main exiting");
			System.exit(0);
		}
	}

	public void handle(Event e) {
		System.out.println("Received "+e.toString());
	}
	
	public void collect(Response r) {
		Control c = sensors.get(r.getSID());
		if(c.ts==0) {
			c.ts = System.currentTimeMillis();
			c.count++;
		} else if((c.ts+10000)<System.currentTimeMillis()){
			System.out.println("SID: "+c.sid+" ("+c.description+")- FPS: "+( (float) ((float) 1000*c.count/((float)(System.currentTimeMillis()-c.ts))) ) );
			c.ts = System.currentTimeMillis();
			c.count=0;
		} else
			c.count++;
		
	}
	
	public void export(String name, Remote r) throws AccessException, RemoteException{
		ourRegistry.rebind(name, UnicastRemoteObject.exportObject(r, 0));
	}
}

