/**
 * 
 */
package jcu.sal.agent;

import jcu.sal.common.Response;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.events.EventHandler;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.SensorID;
import jcu.sal.config.HwProbeService;
import jcu.sal.events.EventDispatcher;
import jcu.sal.managers.ProtocolManager;
import jcu.sal.managers.SensorManager;
import jcu.sal.utils.Slog;

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
	
	/**
	 * This method initialises the SAL agent. It parses the platform & sensor configuration files
	 * and creates the required components as per configuration files. 
	 * @param pc the platform config file
	 * @param sc the sensor config file
	 * @throws ConfigurationException if the files can not be written to, parsed, or the configuration is incorrect
	 */
	public void start(String pc, String sc) throws ConfigurationException {
		pm.init(sc, pc);
		pm.startAll();
		if(System.getProperty("jcu.sal.disableHwDetection")==null)
			hp.loadAll();
		else
			logger.debug("Disabling HW autodetection");
		
	}
	
	
	/**
	 * This method stops the SAL agent. It must be called if a previous call to <code>start()</code> was successful.
	 *
	 */
	public void stop(){
		hp.stopAll();
		pm.destroyAllComponents();
		pm.stop();
		ev.stop();
	}
	
	/*
	 * Sensor-related-methods
	 */
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgent#addSensor(java.lang.String)
	 */
	public synchronized String addSensor(String xml) throws SALDocumentException, ConfigurationException {
		return sm.createComponent(new SMLDescription(xml)).getID().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#removeSensor(java.lang.String)
	 */
	public synchronized void removeSensor(String sid) throws NotFoundException {
		SensorID s = new SensorID(sid);
		sm.destroyComponent(s);
		try {
			sm.removeSensorConfig(s);
		} catch (ConfigurationException e) {
			logger.error("We shoudlnt be here - sensor still active");
			e.printStackTrace();
		}
	}		
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#listActiveSensors()
	 */
	public String listActiveSensors() {
		return sm.listSensors(true).getXMLString();		
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#listSensors()
	 */
	public String listSensors() {
		return sm.listSensors(false).getXMLString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#listSensor(java.lang.String)
	 */
	public String listSensor(String sid) throws NotFoundException {
		return sm.listSensor(new SensorID(sid)).getXMLString();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#execute(jcu.sal.components.Command, java.lang.String)
	 */
	public Response execute(Command c, String sid) throws NotFoundException, SensorControlException{
		return pm.execute(c,new SensorID(sid));
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#getCML(java.lang.String)
	 */
	public String  getCML(String sid) throws NotFoundException{
		return pm.getCML(new SensorID(sid)).getXMLString();
	}

	/*
	 * Protocols-related mthods 
	 */

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#addProtocol(java.lang.String, boolean)
	 */
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, SALDocumentException {
		synchronized (this) {
			AbstractProtocol p = pm.createComponent(new ProtocolConfiguration(xml));
			if(loadSensors) sm.loadSensorsFromConfig(p.getID());
			p.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#removeProtocol(java.lang.String, boolean)
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws NotFoundException {
		ProtocolID p = new ProtocolID(pid);
		synchronized (this) {
			pm.destroyComponent(p);
			try {
				pm.removeProtocolConfig(p, removeSensors);
			} catch (ConfigurationException e) {
				logger.error("We shoudlnt be here - protocol still active");
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.agent.SALAgentInterface#listProtocol()
	 */
	public String listProtocols() {
		return pm.listProtocols().getXMLString();
	}
	
	/*
	 * Event-related methods
	 */
	
	public void registerEventHandler(EventHandler eh, String producerID) throws NotFoundException {
		ev.registerEventHandler(eh, producerID);
	}

	public void unregisterEventHandler(EventHandler eh, String producerID) throws NotFoundException {
		ev.unregisterEventHandler(eh, producerID);
	}
}
