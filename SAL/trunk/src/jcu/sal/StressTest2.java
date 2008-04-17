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

public class StressTest2 {

	private static Logger logger = Logger.getLogger(StressTest2.class);
	static SALAgent s;
	private int NB_THREADS = 5;
	private ArrayList<StressUser> threads;
	static {
	}
	//nt[] sensors;
	//int[] sensors = {1,2,3,4,5,13,14,15,16,17,18,19,20,21,22,23};
	int[] sensors = {1,2,3,4,5,6,7,8,9,10,11};
	
	public StressTest2(int n){
		NB_THREADS=n;
		threads = new ArrayList<StressUser>();
		for (int i = 0; i < NB_THREADS; i++) {
			threads.add(new StressUser(String.valueOf(i)));
		}
	}
	
	public StressTest2(){
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
			if(Integer.parseInt(Thread.currentThread().getName())>0) {
				Random r = new Random();
				int ns, count=0, id;
				int[] failure = new int[sensors.length];
				int[] success= new int[sensors.length];
				int[] unavailable= new int[sensors.length];
				try {
					while(!Thread.interrupted()) {
						id = r.nextInt(sensors.length);
						ns = sensors[id];
						System.out.println("Thread "+Thread.currentThread().getName()+": Sending to SID "+ns);
						try {
							count++;
							System.out.println("Thread "+Thread.currentThread().getName()+": Result: " +s.execute(new Command(100, "", ""), String.valueOf(ns)));
							success[id]++;
						} catch (ConfigurationException e) {failure[id]++; System.out.println("Thread "+Thread.currentThread().getName()+": config excp");}
						catch (BadAttributeValueExpException e) {failure[id]++; System.out.println("Thread "+Thread.currentThread().getName()+": badAttr excp");}
						catch (NotActiveException e) { unavailable[id]++; System.out.println("Thread "+Thread.currentThread().getName()+": NotActive excp");}
						
						Thread.sleep(10);
					}
				} catch (InterruptedException e) {}
				int nb_cmds;
				synchronized(sensors){
					System.out.println("Thread "+Thread.currentThread().getName()+": "+count+" commands sent");
					nb_cmds=count;
					System.out.print("Sensor\t");
					for(int i = 0; i<failure.length; i++)
						System.out.print(sensors[i]+"\t");
					System.out.println("Total\t%");
					count=0;
					System.out.print("Failure\t");
					for(int i = 0; i<failure.length; i++)
						{ System.out.print(failure[i]+"\t"); count += failure[i]; }
					System.out.print(count+"\t"+((float) 100*count/nb_cmds));
					System.out.println("");
					count = 0;
					System.out.print("Success\t");
					for(int i = 0; i<failure.length; i++)
						{ System.out.print(success[i]+"\t"); count +=  success[i]; }
					System.out.print(count+"\t"+((float) 100*count/nb_cmds));
					System.out.println("");
					count = 0;
					System.out.print("Unavail\t");
					for(int i = 0; i<failure.length; i++)
						{ System.out.print(unavailable[i]+"\t"); count +=  unavailable[i]; }
					System.out.print(count+"\t"+((float) 100*count/nb_cmds));
					System.out.println("");
				}
			} else {
				try {
					while(!Thread.interrupted()) {
						try {
							System.out.println("Thread "+Thread.currentThread().getName()+": Adding 1-wire protocol");
							s.addProtocol("<Protocol name=\"1wtree\" type=\"owfs\"><EndPoint name=\"usb\" type=\"usb\" /><parameters><Param name=\"Location\" value=\"/opt/owfs/bin/owfs\" /><Param name=\"MountPoint\" value=\"/mnt/w1\" /></parameters>	</Protocol>", true);
							System.out.println("Thread "+Thread.currentThread().getName()+": 1-wire protocol added");
						} catch (ConfigurationException e) {
							System.out.println("Thread "+Thread.currentThread().getName()+": Error creating 1-wire");
						} catch (ParserConfigurationException e) {
							System.out.println("Thread "+Thread.currentThread().getName()+": Error creating 1-wire");
						}
	
						Thread.sleep(7000);
						
						try {
							System.out.println("Thread "+Thread.currentThread().getName()+":Adding osData protocol");
							s.addProtocol("<Protocol name=\"osData\" type=\"PlatformData\"><EndPoint name=\"filesystem\" type=\"fs\" /><parameters><Param name=\"CPUTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp2_input\" /><Param name=\"NBTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp1_input\" /><Param name=\"SBTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp3_input\" /></parameters>	</Protocol>", true);
							System.out.println("Thread "+Thread.currentThread().getName()+":osData protocol added");
						} catch (ConfigurationException e) {
							System.out.println("Thread "+Thread.currentThread().getName()+":Error creating endpoint");
						} catch (ParserConfigurationException e) {
							System.out.println("Thread "+Thread.currentThread().getName()+":Error creating endpoint");
						}
						Thread.sleep(1000);
						try {
							System.out.println("Thread "+Thread.currentThread().getName()+": Adding sid 7 protocol");
							s.addSensor("<Sensor sid=\"7\"><parameters><Param name=\"ProtocolName\" value=\"osData\" /><Param name=\"Address\" value=\"FreeMem\" /></parameters></Sensor>");
							System.out.println("Thread "+Thread.currentThread().getName()+": Adding sid 8 protocol");
							s.addSensor("<Sensor sid=\"8\"><parameters><Param name=\"ProtocolName\" value=\"osData\" /><Param name=\"Address\" value=\"UserTime\" /></parameters></Sensor>");
							System.out.println("Thread "+Thread.currentThread().getName()+": Adding sid 9 protocol");
							s.addSensor("<Sensor sid=\"9\"><parameters><Param name=\"ProtocolName\" value=\"osData\" /><Param name=\"Address\" value=\"NiceTime\" /></parameters></Sensor>");
							System.out.println("Thread "+Thread.currentThread().getName()+": Adding sid 10 protocol");
							s.addSensor("<Sensor sid=\"10\"><parameters><Param name=\"ProtocolName\" value=\"osData\" /><Param name=\"Address\" value=\"SystemTime\" /></parameters></Sensor>");
							System.out.println("Thread "+Thread.currentThread().getName()+": Adding sid 11 protocol");
							s.addSensor("<Sensor sid=\"11\"><parameters><Param name=\"ProtocolName\" value=\"osData\" /><Param name=\"Address\" value=\"IdleTime\" /></parameters></Sensor>");
						} catch (ConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						Thread.sleep(5000);
						
						try {
							System.out.println("Thread "+Thread.currentThread().getName()+": Removing 1-wire protocol");
							s.removeProtocol("1wtree", true);
							System.out.println("Thread "+Thread.currentThread().getName()+": 1-wire protocol removed");
						} catch (ConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						Thread.sleep(5000);
						
						try {
							System.out.println("Thread "+Thread.currentThread().getName()+": Removing osdata protocol");
							s.removeProtocol("osData", true);
							System.out.println("Thread "+Thread.currentThread().getName()+": osData protocol removed");
						} catch (ConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (InterruptedException e1) {	}
				
			}
		}
	}

	
	public static void main(String [] args) throws ConfigurationException, InterruptedException{
		s = new SALAgent();
		logger.setAdditivity(false);
		logger.setLevel(Level.ALL);
		logger.addAppender(new ConsoleAppender(new PatternLayout("%c{1}.%M(%F:%L) %r - %m%n")));
		s.start(args[0], args[1]);
		
		System.out.println("Starting !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
		StressTest2 st = new StressTest2();
		st.start();
		Thread.sleep(40000);
		st.stop();
		s.stop();
	}
}
