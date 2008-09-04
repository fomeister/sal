package jcu.sal.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.RMICommandFactory;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.RMIEventHandler;
import jcu.sal.utils.XMLhelper;

import org.w3c.dom.NodeList;

public class RmiClientDummyStressTest implements RMIEventHandler{
	public static int RUN_LENGTH=120*1000;
	public static int NB_SENSORS = 1000;
	public static int NB_CLIENTS[] = {1, 10, 25, 50, 100};
	private String[] sids = new String[NB_SENSORS];
	private Vector<Thread> threads;
	private RMISALAgent agent;
	private Registry agentRegistry;
	private long sumAvgExe = 0, sumCounts;
	
	private class Control implements Runnable{
		private RMICommand command;
		private long  executionTotal;
		private int count;
		
		public Control(RMICommand c){
			command = c;
			count = 0;
			executionTotal = 0;
		}
		
		public void run(){
			long t;
			Random r = new Random();
			while(!Thread.interrupted()){ 
				t = System.currentTimeMillis();
				try {agent.execute(command, sids[r.nextInt(sids.length)]);} catch(Exception e) {}
				executionTotal += (System.currentTimeMillis() - t);
				count++;
			}
			
			//System.out.println("Sent "+count+" cmds - avg exe: "+getAvgExe());
			sumAvgExe += getAvgExe();
			sumCounts += count;
		}

		public long getAvgExe(){
			return executionTotal/count;
		}
	}	
	
	public RmiClientDummyStressTest(String rmiName, String agentRMIRegIP, String ourIP) throws RemoteException {
		agentRegistry = LocateRegistry.getRegistry(agentRMIRegIP);
		threads = new Vector<Thread>();
		try {
			agent = (RMISALAgent) agentRegistry.lookup(RMISALAgent.RMI_STUB_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException();
		}
	}	
	
	public void populateSensorList() throws ConfigurationException{
		String sid;
				
		try {
		NodeList nl = XMLhelper.getNodeList("/SAL/SensorConfiguration/Sensor/parameters/Param[@value=\"DummyProtocol0\"]/parent::*/parent::*", XMLhelper.createDocument(agent.listActiveSensors()));
			for (int i = 0; i < nl.getLength(); i++) {
				sid = XMLhelper.getAttributeFromName("sid", nl.item(i));
				//System.out.println("Found sensor SID: "+sid);
				sids[i]=sid;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	public void createDummySensors(){
		String xml = "<Protocol name=\"DummyProtocol0\" type=\"DUMMY\">" +
						"<EndPoint name=\"fs0\" type=\"fs\"> "+
						"<parameters />" +
						"</EndPoint>"+
						"<parameters />"+
						"</Protocol>";
		try {
			agent.addProtocol(xml, false);
		} catch (Exception e) {
			e.printStackTrace();
		}


		xml = "<Sensor sid=\"1\">"+
				"<parameters>" +
				"<Param name=\"ProtocolName\" value=\"DummyProtocol0\" /> "+
				"<Param name=\"Address\" value=\"PLACE_HOLDER\" />"+
				"</parameters>"+
				"</Sensor>";
		String t;

		for(int i=0;i<NB_SENSORS; i++) {
			t = xml.replaceFirst("PLACE_HOLDER", "DUMMY_"+i);
			try {
				agent.addSensor(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void createClients(int nb){
		RMICommandFactory cf;
		threads.removeAllElements();
		try {
			cf = new RMICommandFactory(XMLhelper.createDocument(agent.getCML(sids[0])),100);
			
			for(int i=0; i<nb; i++) 
				threads.add(new Thread(new Control(cf.getCommand())));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void start(String ourRmiIP) throws ConfigurationException{}
	
	public void run(){
		Iterator<Thread> it;
		long t;

		try {
			System.out.print("Creating dummy sensors");
			t = System.currentTimeMillis();
			createDummySensors();
			System.out.println("("+(System.currentTimeMillis()-t)+" ms)");
			
			System.out.print("Populating dummy sensor list");
			t = System.currentTimeMillis();
			populateSensorList();
			System.out.println("("+(System.currentTimeMillis()-t)+" ms)");
			
			for(int i=0; i<NB_CLIENTS.length; i++) {
				System.out.println("Creating clients");
				createClients(NB_CLIENTS[i]);
				System.out.print("Running stress test");
				
				//Starts all client threads 
				t = System.currentTimeMillis();
				it = threads.iterator();
				while(it.hasNext())
					it.next().start();
				
				Thread.sleep(RUN_LENGTH);
				
				//Stops all the threads
				it = threads.iterator();
				while(it.hasNext())
					it.next().interrupt();
				
				//Joins all the threads
				it = threads.iterator();
				while(it.hasNext())
					it.next().join();
				
				System.out.println("("+(System.currentTimeMillis()-t)+" ms)");
	
				System.out.println("Grand Total");
				System.out.println(NB_CLIENTS[i]+" clients - "+NB_SENSORS+" sensors");
				System.out.println(sumCounts+" commands processed ("+((1000.0*sumCounts)/RUN_LENGTH)+"cmd/sec)");
				System.out.println("Avg exe time: "+(sumAvgExe/NB_CLIENTS[i]));
				sumCounts = sumAvgExe = 0;
			}
			agent.removeProtocol("DummyProtocol0", true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop() throws RemoteException{
		agent = null;
		System.gc();
		
	}
	
	
	public static void main(String [] args) throws RemoteException, NotBoundException{
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: our RMI name - 2: the IP address of our agentRegistry - 3: the IP address of the Agent agentRegistry");
			System.exit(1);
		}
		
		RmiClientDummyStressTest c = new RmiClientDummyStressTest(args[0], args[2], args[1]);
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
}

