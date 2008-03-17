/**
 * 
 */
package jcu.sal.Agent;

import java.io.NotActiveException;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.Command;
import jcu.sal.Components.Protocols.ProtocolID;
import jcu.sal.Components.Sensors.SensorID;
import jcu.sal.Managers.ProtocolManager;
import jcu.sal.Managers.SensorManager;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class SALAgent implements SALAgentInterface{
	
	private Logger logger = Logger.getLogger(SALAgent.class);
	private ProtocolManager pm;
	private SensorManager sm;
	
	public SALAgent(){
		Slog.setupLogger(logger);
		pm = ProtocolManager.getProcotolManager();
		sm = SensorManager.getSensorManager();

	}
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#init(java.lang.String, java.lang.String)
	 */
	public void init(String pc, String sc) throws ConfigurationException {
		pm.init(sc, pc);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#listSensors()
	 */
	public String listSensors() {
		return sm.listSensors();		
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#execute(jcu.sal.Components.Command, java.lang.String)
	 */
	public String execute(Command c, String sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException {
		return pm.execute(c,new SensorID(sid));
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#getCML(java.lang.String)
	 */
	public String getCML(String sid) throws ConfigurationException, NotActiveException {
		return pm.getCML(new SensorID(sid));
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#stop()
	 */
	public void stop(){
		pm.destroyAllComponents();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#addProtocol(java.lang.String)
	 */
	public void addProtocol(String xml) throws ConfigurationException, ParserConfigurationException {
		pm.createComponent(XMLhelper.createDocument(xml)).start();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#removeProtocol(java.lang.String)
	 */
	public void removeProtocol(String pid) throws ConfigurationException {
		pm.destroyComponent(new ProtocolID(pid));
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#removeProtocols()
	 */
	public void removeProtocols(){
		pm.removeAll();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#addSensor(java.lang.String)
	 */
	public void addSensor(String xml) throws ConfigurationException, ParserConfigurationException {
		sm.createComponent(XMLhelper.createDocument(xml));
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#removeSensor(java.lang.String)
	 */
	public void removeSensor(String sid) throws ConfigurationException {
		sm.destroyComponent(new SensorID(sid));
	}		
}
