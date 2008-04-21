/**
 * 
 */
package jcu.sal.Agent;

import java.io.NotActiveException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.Command;
import jcu.sal.Components.Protocols.Protocol;
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
	public void start(String pc, String sc) throws ConfigurationException {
		stop();
		pm.init(sc, pc);		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#stop()
	 */
	public void stop(){
		pm.destroyAllComponents();
	}
	
	/*
	 * Sensor-related-methods
	 */
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#addSensor(java.lang.String)
	 */
	public synchronized String  addSensor(String xml) throws ConfigurationException, ParserConfigurationException {
		return sm.createComponent(XMLhelper.createDocument(xml)).getID().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#removeSensor(java.lang.String)
	 */
	public synchronized void removeSensor(String sid) throws ConfigurationException {
		SensorID s = new SensorID(sid);
		try {sm.destroyComponent(s);}
		catch (ConfigurationException e) {logger.debug("Looks like this sensor was not active");}
		sm.removeSensorConfig(s);
	}		
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#listActiveSensors()
	 */
	public String listActiveSensors() {
		return sm.listActiveSensors();		
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
	 * Protocols-related mthods 
	 */

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#addProtocol(java.lang.String, boolean)
	 */
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, ParserConfigurationException {
		synchronized (this) {
			Protocol p = pm.createComponent(XMLhelper.createDocument(xml));
			if(loadSensors) sm.loadSensorsFromConfig(p.getID());
			p.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Agent.SALAgentInterface#removeProtocol(java.lang.String, boolean)
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws ConfigurationException {
		ProtocolID p = new ProtocolID(pid);
		synchronized (this) {
			pm.destroyComponent(p);
			pm.removeProtocolConfig(p, removeSensors);
		}
	}

	public static void main(String[] args) {
		SALAgent s = new SALAgent();
		try {
			SALAgentInterface stub = (SALAgentInterface) UnicastRemoteObject.exportObject(s, 0);
			Registry registry = LocateRegistry.getRegistry("137.219.45.43");
            registry.rebind("SALAgent", stub);
            System.out.println("RMI SALAgentInterface ready");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}