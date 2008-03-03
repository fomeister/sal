/**
 * 
 */
package jcu.sal.Agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Command;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.Managers.ProtocolManager;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class SALAgent implements Runnable{
	
	private Logger logger = Logger.getLogger(SALAgent.class);
	private ProtocolManager pm;
	
	public SALAgent(String pc, String sc) throws ConfigurationException {
		Slog.setupLogger(logger);
		Thread t = new Thread(this);
		pm = ProtocolManager.getProcotolManager();
		pm.init(sc, pc);
		pm.dumpTable();
		pm.dumpSensors();
		
		int i=0;
		SensorID sid=null;
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		t.start();
		while(i!=-1) {
			System.out.println("Enter a sensor id (-1 to quit or -2 to see a list of sensors)");
			try {
				i=Integer.parseInt(b.readLine());
				if(i>=0) {
					sid=new SensorID(String.valueOf(i));
					System.out.println("command returned : "+pm.execute(new Command(100, "", ""), sid));
				} else if(i==-2)
					pm.dumpSensors();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberFormatException e) {
				System.out.println("Not a number !");
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadAttributeValueExpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		pm.destroyAllComponents();
		t.interrupt();
	}
	
	public static void main(String[] args) throws ConfigurationException {
		//new SALAgent("/home/gilles/workspace/SALv1/src/platformConfig-osdata.xml", "/home/gilles/workspace/SALv1/src/sensors.xml");
		new SALAgent(args[0], args[1]);
	}
	public void run() {
		logger.debug("NASTY THREAD STARTING");

		while(!Thread.interrupted()) {
			try {
			pm.execute(new Command(100, "", ""), new SensorID("1"));
			pm.execute(new Command(100, "", ""), new SensorID("2"));
			pm.execute(new Command(100, "", ""), new SensorID("3"));
			pm.execute(new Command(100, "", ""), new SensorID("4"));
			pm.execute(new Command(100, "", ""), new SensorID("5"));
			pm.execute(new Command(100, "", ""), new SensorID("6"));
			pm.execute(new Command(100, "", ""), new SensorID("1"));
			pm.execute(new Command(100, "", ""), new SensorID("2"));
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				logger.debug("config excp");
			} catch (BadAttributeValueExpException e) {
				// TODO Auto-generated catch block
				logger.debug("bad att value excp");
			}
		}
	}
}
