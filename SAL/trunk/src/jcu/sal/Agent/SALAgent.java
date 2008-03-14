/**
 * 
 */
package jcu.sal.Agent;

import java.io.NotActiveException;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Command;
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
	
	public String listSensors() {
		return pm.listSensors();		
	}

	public String execute(Command c, int sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException {
		return pm.execute(c,sid);
	}
	
	public String getCML(int sid) throws ConfigurationException, NotActiveException {
		return pm.getCML(sid);
	}
	
	public void stop(){
		pm.destroyAllComponents();
	}
	
		
}
