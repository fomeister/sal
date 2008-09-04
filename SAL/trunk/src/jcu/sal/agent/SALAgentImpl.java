/**
 * 
 */
package jcu.sal.agent;

import java.io.NotActiveException;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.Response;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.events.EventHandler;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.SensorID;
import jcu.sal.config.HwProbeService;
import jcu.sal.events.EventDispatcher;
import jcu.sal.managers.ProtocolManager;
import jcu.sal.managers.SensorManager;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class SALAgentImpl implements SALAgent{
	
	private Logger logger = Logger.getLogger(SALAgentImpl.class);
	private ProtocolManager pm;
	private SensorManager sm;
	private EventDispatcher ev;
	private HwProbeService hp;
	
	public SALAgentImpl(){
		Slog.setupLogger(logger);
		ev = EventDispatcher.getInstance();
		pm = ProtocolManager.getProcotolManager();
		sm = SensorManager.getSensorManager();
		hp = HwProbeService.getService();

	}
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#init(java.lang.String, java.lang.String)
	 */
	public void start(String pc, String sc) throws ConfigurationException {
		pm.init(sc, pc);
		pm.startAll();
		hp.loadAll();
		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#stop()
	 */
	public void stop(){
		hp.stopAll();
		pm.destroyAllComponents();
		ev.stop();
	}
	
	/*
	 * Sensor-related-methods
	 */
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#addSensor(java.lang.String)
	 */
	public synchronized String addSensor(String xml) throws ConfigurationException, ParserConfigurationException {
		return sm.createComponent(XMLhelper.createDocument(xml)).getID().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#removeSensor(java.lang.String)
	 */
	public synchronized void removeSensor(String sid) throws ConfigurationException {
		SensorID s = new SensorID(sid);
		try {sm.destroyComponent(s);}
		catch (ConfigurationException e) {logger.debug("Looks like this sensor was not active");}
		sm.removeSensorConfig(s);
	}		
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#listActiveSensors()
	 */
	public String listActiveSensors() {
		return sm.listActiveSensors().getSMLString();		
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#listSensors()
	 */
	public String listSensors() {
		return sm.listSensors().getSMLString();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#execute(jcu.sal.components.Command, java.lang.String)
	 */
	public Response execute(Command c, String sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException {
		return pm.execute(c,new SensorID(sid));
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#getCML(java.lang.String)
	 */
	public String  getCML(String sid) throws ConfigurationException, NotActiveException {
		return pm.getCML(new SensorID(sid)).getCMLString();
	}

	/*
	 * Protocols-related mthods 
	 */

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#addProtocol(java.lang.String, boolean)
	 */
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, ParserConfigurationException {
		synchronized (this) {
			AbstractProtocol p = pm.createComponent(XMLhelper.createDocument(xml));
			if(loadSensors) sm.loadSensorsFromConfig(p.getID());
			p.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#removeProtocol(java.lang.String, boolean)
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws ConfigurationException {
		ProtocolID p = new ProtocolID(pid);
		synchronized (this) {
			pm.destroyComponent(p);
			pm.removeProtocolConfig(p, removeSensors);
		}
	}
	
	/*
	 * Event-related methods
	 */
	
	public void registerEventHandler(EventHandler eh, String producerID) throws ConfigurationException {
		ev.registerEventHandler(eh, producerID);
	}

	public void unregisterEventHandler(EventHandler eh, String producerID) throws ConfigurationException {
		ev.unregisterEventHandler(eh, producerID);
	}
	


}