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
public class SALAgent {
	
	private Logger logger = Logger.getLogger(SALAgent.class);
	
	public SALAgent(String pc, String sc) throws ConfigurationException {
		Slog.setupLogger(logger);
		ProtocolManager pm = ProtocolManager.getProcotolManager();
		pm.init(sc, pc);
		pm.dumpTable();
		pm.dumpSensors();
		
		int i=0;
		SensorID sid=null;
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		while(i!=-1) {
			System.out.println("Enter a sensor id (-1 to quit)");
			try {
				i=Integer.parseInt(b.readLine());
				if(i>=0) {
					sid=new SensorID(String.valueOf(i));
					System.out.println("command returned : "+pm.execute(new Command(100, "", ""), sid));
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
	
	public static void main(String[] args) throws ConfigurationException {
		//new SALAgent("/home/gilles/workspace/SALv1/src/platformConfig-osdata.xml", "/home/gilles/workspace/SALv1/src/sensors.xml");
		new SALAgent(args[0], args[1]);
	}

}
