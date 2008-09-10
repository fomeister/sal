package jcu.sal.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.common.RMICommandFactory;
import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;
import jcu.sal.components.EndPoints.FSEndPoint;
import jcu.sal.components.protocols.dummy.DummyProtocol;
import jcu.sal.utils.XMLhelper;

public class RmiClientDummyStressTest{
	public static int RUN_LENGTH=120*1000;
	public static int NB_SENSORS = 5000;
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
		int i=0;
		try {
			for(SMLDescription s: new SMLDescriptions(agent.listActiveSensors()).getDescriptions())
				sids[i++] = s.getID();

		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	public void createDummySensors(){
		Vector<Parameter> v = new Vector<Parameter>();
		Integer id = new Integer(1);

		ProtocolConfiguration p = new ProtocolConfiguration("dummy0", DummyProtocol.PROTOCOL_TYPE,
															new EndPointConfiguration("fs",FSEndPoint.ENDPOINT_TYPE));
		try {
			agent.addProtocol(p.getXMLString(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}


		for(int i=0;i<NB_SENSORS; i++) {
			v.removeAllElements();
			v.add(new Parameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, "dummy0"));
			v.add(new Parameter(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE, DummyProtocol.PROTOCOL_TYPE));
			v.add(new Parameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE, "_"+i));
			
			try {
				agent.addSensor(new SMLDescription(id, new Parameters(v)).getSMLString());
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
			e.printStackTrace();
		}

	}
	
	public void run(){
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
				for(Thread th: threads)
					th.start();
				
				Thread.sleep(RUN_LENGTH);
				
				//Stops all the threads
				for(Thread th: threads)
					th.interrupt();
				
				//Joins all the threads
				for(Thread th: threads)
					th.join();
				
				System.out.println("("+(System.currentTimeMillis()-t)+" ms)");
	
				System.out.println("Grand Total");
				System.out.println(NB_CLIENTS[i]+" clients - "+NB_SENSORS+" sensors");
				System.out.println(sumCounts+" commands processed ("+((1000.0*sumCounts)/RUN_LENGTH)+"cmd/sec)");
				System.out.println("Avg exe time: "+(sumAvgExe/NB_CLIENTS[i]));
				sumCounts = sumAvgExe = 0;
			}
			agent.removeProtocol("dummy0", true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) throws RemoteException, NotBoundException{
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: our RMI name - 2: the IP address of our agentRegistry - 3: the IP address of the Agent agentRegistry");
			System.exit(1);
		}
		
		new RmiClientDummyStressTest(args[0], args[2], args[1]).run();
		
		System.out.println("Main exiting");
	}
}

