package jcu.sal;

import java.util.ArrayList;
import java.util.Random;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Agent.SALAgent;
import jcu.sal.Components.Command;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class StressTest {

	private static Logger logger = Logger.getLogger(StressTest.class);
	static SALAgent s;
	private int NB_THREADS = 5;
	private ArrayList<StressUser> threads;
	int[] sensors;
	static {
	}
	//int[] sensors = {1,2,3,4,5,13,14,15,16,17,18,19,20,21,22,23};
	//int[] sensors = {1};
	
	public StressTest(int n){
		NB_THREADS=n;
		threads = new ArrayList<StressUser>();
		for (int i = 0; i < NB_THREADS; i++) {
			threads.add(new StressUser(String.valueOf(i)));
		}
		sensors = new int[38];
		for(int i=1; i<=38; i++)
			sensors[(i-1)]=i;
	}
	
	public StressTest(){
		threads = new ArrayList<StressUser>();
		for (int i = 0; i < NB_THREADS; i++) {
			threads.add(new StressUser(String.valueOf(i)));
		}
		sensors = new int[38];
		for(int i=1; i<=38; i++)
			sensors[(i-1)]=i;
	}
	
	public void start(){
		for (int i = 0; i < NB_THREADS; i++) {
			threads.get(i).start();
		}
	}
	
	public void interrupt(){
		for (int i = 0; i < NB_THREADS; i++) {
			if(threads.get(i).isAlive()) threads.get(i).interrupt();
		}
	}
	
	public void join(){
		for (int i = 0; i < NB_THREADS; i++) {
			if(threads.get(i).isAlive())
				try {
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
			t= new Thread(this, "Stress User thread_"+n);
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
		 */
		public void run() {
			Random r = new Random();
			int ns, count=0, id;
			int[] failure = new int[sensors.length];
			int[] success= new int[sensors.length];
			try {
				while(!Thread.interrupted()) {
					id = r.nextInt(sensors.length);
					ns = sensors[id];
					logger.debug("Thread "+Thread.currentThread().getName()+": Sending command to "+ns);
					try {
						count++;
						logger.debug("Result: " +s.execute(new Command(100, "", ""), ns));
						success[id]++;
					} catch (ConfigurationException e) {failure[id]++;}
					catch (BadAttributeValueExpException e) {failure[id]++;}
					
					Thread.sleep(0);
				}
			} catch (InterruptedException e) {}
			synchronized(sensors){
				System.out.println("Thread "+Thread.currentThread().getName()+": "+count+" commands sent");
				System.out.print("Sensor\t");
				for(int i = 0; i<failure.length; i++)
					System.out.print(sensors[i]+"\t");
				System.out.println("Total");
				count=0;
				System.out.print("Failure\t");
				for(int i = 0; i<failure.length; i++)
					{ System.out.print(failure[i]+"\t"); count += failure[i]; }
				System.out.print(count);
				System.out.println("");
				count = 0;
				System.out.print("Success\t");
				for(int i = 0; i<failure.length; i++)
					{ System.out.print(success[i]+"\t"); count +=  success[i]; }
				System.out.print(count);
				System.out.println("");
			}
		}
	}

	
	public static void main(String [] args) throws ConfigurationException, InterruptedException{
		s = new SALAgent();
		logger.setAdditivity(false);
		logger.setLevel(Level.ERROR);
		logger.addAppender(new ConsoleAppender(new PatternLayout()));
		s.init(args[0], args[1]);
		
		System.out.println("Starting !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
		StressTest st = new StressTest();
		st.start();
		Thread.sleep(20000);
		st.stop();
		s.stop();
	}
}
