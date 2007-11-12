/**
 * 
 */
package jcu.sal.Agent;

import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Identifiers.ProtocolID;
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
	
	public SALAgent() {
		Slog.setupLogger(logger);
		
		Sensor s = null;
		ConfigService conf = ConfigService.getService();
		ProtocolManager pm = ProtocolManager.getProcotolManager();
		SensorManager sm = SensorManager.getSensorManager();
		
		try {
			conf.init("/home/gilles/workspace/SALv1/src/platformConfig-osdata.xml", "/home/gilles/workspace/SALv1/src/sensors.xml");
			Iterator<Node> iter = conf.getProtocolIterator();
			while(iter.hasNext()) {
				pm.createComponent(iter.next());
			}
		} catch (ConfigurationException e) {
			logger.error("Could not read the configuration files.");
		} 
		
		Iterator<Node> iter = conf.getSensorIterator();
		while(iter.hasNext()) {
			s = sm.createComponent(iter.next());
			try {
				pm.getComponent(new ProtocolID(s.getConfig(Sensor.PROTOCOLATTRIBUTE_TAG))).addSensor(s);
			} catch (BadAttributeValueExpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		
		pm.dumpTable();
		pm.destroyAllComponents();
	}
	
	public static void main(String[] args) {
		new SALAgent();
	}

}
