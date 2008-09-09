package jcu.sal.client.stressTest;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import jcu.sal.client.stressTest.actions.Action;
import jcu.sal.client.stressTest.actions.AddProtocolAction;
import jcu.sal.client.stressTest.actions.AddSensorAction;
import jcu.sal.client.stressTest.actions.ExecuteSensorAction;
import jcu.sal.client.stressTest.actions.RemoveProtocolAction;
import jcu.sal.client.stressTest.actions.RemoveSensorAction;
import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.RMIEventHandler;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

public class RmiClientDummyStressTest2 implements RMIEventHandler{
	private static Logger logger = Logger.getLogger(RmiClientDummyStressTest2.class);
	static {
		Slog.setupLogger(logger);
	}
	
	public static int RUN_LENGTH=120*1000;
	public static int NB_CLIENTS = 1;
	public static int SLEEP = 100;
	private Vector<Client> clients;
	private RMISALAgent agent;
	private Registry agentRegistry;
	private AtomicBoolean setupFlag;
	
	private class Client implements Runnable{
		private Action[] actions = new Action[10];
		private Thread t;
		private String name;
		private Random r;
		
		
		public Client(String n){
			name = n;
			int i=0;
			actions[i++] = new AddProtocolAction(agent);
			actions[i++] = new RemoveProtocolAction(agent);
			actions[i++] = new AddSensorAction(agent);
			actions[i++] = new RemoveSensorAction(agent);
			actions[i++] = new ExecuteSensorAction(agent);
			r = new Random();
		}
		
		public void start(){
			t = new Thread(this, name);
			t.start();
		}
		
		public void stop(){
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {}			
			logger.info("Client "+name+" joined");
		}
		
		public void run(){
			Action a;
			if(setupFlag.compareAndSet(false, true)){
				actions[0].execute();
				actions[2].execute();
				actions[2].execute();
				actions[2].execute();
			}
			try {
				Thread.sleep(r.nextInt(5)*1000);
				while(!Thread.interrupted()){
					if(r.nextInt(3)>2){
						a = actions[r.nextInt(4)];
					} else {
						a = actions[4];
					}
					a.execute();
					Thread.sleep(SLEEP);
				}
			} catch (InterruptedException e) {}
			logger.info("client "+name+" exiting");
		}
	}	
	
	public RmiClientDummyStressTest2(String rmiName, String agentRMIRegIP, String ourIP) throws RemoteException {
		agentRegistry = LocateRegistry.getRegistry(agentRMIRegIP);
		clients = new Vector<Client>();
		setupFlag = new AtomicBoolean(false);
		try {
			agent = (RMISALAgent) agentRegistry.lookup(RMISALAgent.RMI_STUB_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException();
		}
	}	

	
	public void createClients(int nb){
		for(int i = 0; i<nb; i++)
			clients.add(new Client("Client "+i));
	}
	
	public void startClients(){
		for(Client c: clients)
			c.start();
	}
	
	public void stopClients(){
		for(Client c: clients)
			c.stop();
	}
	
	public void run(){
		createClients(NB_CLIENTS);
		startClients();
		try {
			Thread.sleep(RUN_LENGTH);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopClients();
	}
	
	public static void main(String [] args) throws RemoteException, NotBoundException{
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: our RMI name - 2: the IP address of our agentRegistry - 3: the IP address of the Agent agentRegistry");
			System.exit(1);
		}
		
		new RmiClientDummyStressTest2(args[0], args[2], args[1]).run();
		System.out.println("Main exiting");
		System.exit(0);
	}

	public void handle(Event e) {
		System.out.println("Received "+e.toString());
	}
}

