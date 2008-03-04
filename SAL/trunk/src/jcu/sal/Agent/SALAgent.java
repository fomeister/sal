/**
 * 
 */
package jcu.sal.Agent;

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
public class SALAgent implements SALAgentInterface{
	
	private Logger logger = Logger.getLogger(SALAgent.class);
	private ProtocolManager pm;
	
	public SALAgent(){
		Slog.setupLogger(logger);
		pm = ProtocolManager.getProcotolManager();

	}
	
	public void init(String pc, String sc) throws ConfigurationException {
		pm.init(sc, pc);		
	}

	public void dumpSensors() {
		pm.dumpSensors();		
	}

	public String execute(Command c, int sid) throws ConfigurationException, BadAttributeValueExpException {
		return pm.execute(c,sid);
	}
	
	public String getCML(int sid) throws ConfigurationException {
		return pm.getProtocol(sid).getCML(new SensorID(String.valueOf(sid)));
	}
	
	public void stop(){
		pm.destroyAllComponents();
	}
	
		
}
