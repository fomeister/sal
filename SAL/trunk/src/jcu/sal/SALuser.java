package jcu.sal;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.naming.ConfigurationException;

import jcu.sal.Agent.SALAgent;
import jcu.sal.Components.Command;

import org.apache.log4j.Logger;

public class SALuser {
	private Logger logger = Logger.getLogger(SALuser.class);
	static SALAgent s;
	
	public static void main(String [] args) throws ConfigurationException {
		int i=0,j=0;
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		s = new SALAgent();
		s.init(args[0], args[1]);
		
		while(i!=-1) {
			System.out.println("Enter a sensor id (-1 to quit or -2 to see a list of sensors)");
			try {
				i=Integer.parseInt(b.readLine());
				if(i>=0) {
					System.out.println("\n\nHere is the CML document for this sensor:");
					System.out.println(s.getCML(i));
					System.out.println("Enter a command id:");
					j=Integer.parseInt(b.readLine());
					System.out.println("command "+j+" returned : "+s.execute(new Command(j, "", ""), i));
				} else if(i==-2)
					s.dumpSensors();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		s.stop();
	}
	
	public void run() {
		logger.debug("NASTY THREAD STARTING");

		while(!Thread.interrupted()) {
			try {
			s.execute(new Command(100, "", ""),1);
			s.execute(new Command(100, "", ""),2);
			s.execute(new Command(100, "", ""), 3);
			s.execute(new Command(100, "", ""), 4);
			s.execute(new Command(100, "", ""), 5);
			s.execute(new Command(100, "", ""), 6);
			s.execute(new Command(100, "", ""), 1);
			s.execute(new Command(100, "", ""), 2);
			} catch (Exception e) {
				logger.debug("Excp");
			}
		}
	}
}
