package jcu.sal;

import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.Random;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Agent.SALAgent;
import jcu.sal.Components.Command;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class StressTest3 {

	private static Logger logger = Logger.getLogger(StressTest3.class);
	static SALAgent s;
	private int NB_THREADS = 5;
	private ArrayList<StressUser> threads;
	static {
	}
	//nt[] sensors;
	//int[] sensors = {1,2,3,4,5,13,14,15,16,17,18,19,20,21,22,23};
	int[] sensors = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
	
	public StressTest3(int n){
		NB_THREADS=n;
		threads = new ArrayList<StressUser>();
		for (int i = 0; i < NB_THREADS; i++) {
			threads.add(new StressUser(String.valueOf(i)));
		}
	}
	
	public StressTest3(){
		threads = new ArrayList<StressUser>();
		for (int i = 0; i < NB_THREADS; i++) {
			threads.add(new StressUser(String.valueOf(i)));
		}
	}
	
	public void start(){
		for (int i = 0; i < NB_THREADS; i++) {
			threads.get(i).start();
		}
	}
	
	public void interrupt(){
		for (int i = 0; i < NB_THREADS; i++) {
			if(threads.get(i).isAlive()) {System.err.println("interrupting stress user "+i);threads.get(i).interrupt();}
		}
	}
	
	public void join(){
		for (int i = 0; i < NB_THREADS; i++) {
			if(threads.get(i).isAlive())
				try {
					System.err.println("Joining stress user "+i);				
					threads.get(i).join();
				} catch (InterruptedException e) {}
		}
	}
	
	public void stop(){
		interrupt();
		join();
	}
	
	class StressUser implements Runnable{
		Thread t;
		
		public StressUser(String n) {
			t= new Thread(this, n);
		}
		
		public void start() {
			t.start();
		}
		
		public void interrupt() {
			t.interrupt();
		}
		
		public boolean isAlive(){
			return t.isAlive();
		}

		public void join() throws InterruptedException {
			t.join();
		}
		
		/**
		 * Implements the Stress test
		 * @throws NotActiveException 
		 */
		public void run() {
			System.out.println("Thread "+Thread.currentThread().getName()+": starting");
			if(Thread.currentThread().getName().equals("0")) {
				try {
					while(!Thread.interrupted()) {
						try {
							System.out.println("Thread "+Thread.currentThread().getName()+": Adding 1-wire protocol");
							s.addProtocol("<Protocol name=\"1wtree\" type=\"owfs\"><EndPoint name=\"usb\" type=\"usb\" /><parameters><Param name=\"Location\" value=\"/opt/owfs/bin/owfs\" /><Param name=\"MountPoint\" value=\"/mnt/w1\" /></parameters>	</Protocol>");
							System.out.println("Thread "+Thread.currentThread().getName()+": 1-wire protocol added");
						} catch (ConfigurationException e) {
							System.out.println("Thread "+Thread.currentThread().getName()+": Error creating 1-wire");
						} catch (ParserConfigurationException e) {
							System.out.println("Thread "+Thread.currentThread().getName()+": Error creating 1-wire");
						}
	
						Thread.sleep(3000);
						
					}
				} catch (InterruptedException e1) {	}
			} else if(Thread.currentThread().getName().equals("1")) {
				try {
					while(!Thread.interrupted()) {
						Thread.sleep(9000);
						try {
							System.out.println("Thread "+Thread.currentThread().getName()+": Removing 1-wire protocol");
							s.removeProtocol("1wtree");
							System.out.println("Thread "+Thread.currentThread().getName()+": 1-wire protocol removed");
						} catch (ConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (InterruptedException e1) {	}
			} else {
				Random r = new Random();
				int ns, count=0, id;
				
				try {
					while(!Thread.interrupted()) {
						id = r.nextInt(sensors.length);
						ns = sensors[id];
						//System.out.println("Thread "+Thread.currentThread().getName()+": Sending to SID "+ns);
						try {
							//System.out.println("Thread "+Thread.currentThread().getName()+": Result: " +s.execute(new Command(100, "", ""), String.valueOf(ns)));
							//success[id]++;
							s.execute(new Command(100, "", ""), String.valueOf(ns));
						} catch (ConfigurationException e) {}//System.out.println("Thread "+Thread.currentThread().getName()+": config excp");}
						catch (BadAttributeValueExpException e) {}// System.out.println("Thread "+Thread.currentThread().getName()+": badAttr excp");}
						catch (NotActiveException e) {}//System.out.println("Thread "+Thread.currentThread().getName()+": NotActive excp");}
						
						if(count++==10) {Thread.sleep(10); count=0;}
					}
				} catch (InterruptedException e) {}			}
			
		}
	}

	
	public static void main(String [] args) throws ConfigurationException, InterruptedException{
		s = new SALAgent();
		logger.setAdditivity(false);
		logger.setLevel(Level.ALL);
		logger.addAppender(new ConsoleAppender(new PatternLayout("%c{1}.%M(%F:%L) %r - %m%n")));
		//s.init("src/platformConfig-empty.xml", "src/sensors.xml");
		
		System.out.println("Starting !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
		StressTest3 st = new StressTest3();
		st.start();
		Thread.sleep(40000);
		st.stop();
		s.stop();
	}
}
