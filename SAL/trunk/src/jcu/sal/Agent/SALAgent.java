/**
 * 
 */
package jcu.sal.Agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Command;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.Config.ConfigService;
import jcu.sal.Managers.ProtocolManager;
import jcu.sal.Managers.SensorManager;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @author gilles
 *
 */
public class SALAgent {
	
	private Logger logger = Logger.getLogger(SALAgent.class);
	
	public SALAgent(String pc, String sc) {
		Slog.setupLogger(logger);
		
		Sensor s = null;
		ConfigService conf = ConfigService.getService();
		ProtocolManager pm = ProtocolManager.getProcotolManager();
		SensorManager sm = SensorManager.getSensorManager();
		
		try {
			//conf.init("/home/gilles/workspace/SALv1/src/platformConfig-osdata.xml", "/home/gilles/workspace/SALv1/src/sensors.xml");
			conf.init(pc,sc);
			Iterator<Node> iter = conf.getProtocolIterator();
			while(iter.hasNext()) {
				pm.createComponent(iter.next());
			}
		} catch (ConfigurationException e) {
			logger.error("Could not read the configuration files.");
			return;
		} 
		
		Iterator<Node> iter = conf.getSensorIterator();
		while(iter.hasNext()) {
			try {
				s = sm.createComponent(iter.next());
				pm.addSensor(s);
			} catch (ConfigurationException e) {
				logger.error("Could not add the sensor to any protocols");
				if(s!=null) sm.destroyComponent(s);
			}
		} 
		
		pm.dumpTable();
		pm.startAll();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Protocol p = pm.getComponent(new ProtocolID("osData"));
		for (int id = 0; id < 1; id++) {
			Iterator<Sensor> i = sm.getIterator();
			while(i.hasNext())
				try {
					s=i.next();
					System.out.println( s.toString() + " Return value: " +p.execute(new Command(100, "", ""), s.getID()));
				} catch (BadAttributeValueExpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}
		
		sm.dumpTable();
		int i=0;
		SensorID sid=null;
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		while(i!=-1) {
			System.out.println("Enter a sensor id (-1 to quit)");
			try {
				i=Integer.parseInt(b.readLine());
				if(i>=0) {
					sid=new SensorID(String.valueOf(i));
					System.out.println("command returned : "+sm.getProtocol(sid).execute(new Command(100, "", ""), sid));
				}
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
	}
	
	public static void main(String[] args) {
		//new SALAgent("/home/gilles/workspace/SALv1/src/platformConfig-osdata.xml", "/home/gilles/workspace/SALv1/src/sensors.xml");
		new SALAgent(args[0], args[1]);
	}

}
