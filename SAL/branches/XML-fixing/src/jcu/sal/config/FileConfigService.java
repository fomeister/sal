/**
 * 
 */
package jcu.sal.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.SensorID;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * @author gilles
 * Makes configuration documents available to SAL components
 */
public class FileConfigService{
	private static Logger logger = Logger.getLogger(FileConfigService.class);
	static {Slog.setupLogger(logger);}
	private static FileConfigService c =  new FileConfigService();
	private static long sensorConfigWriteInterval = 5*1000;
	private static long platformConfigWriteInterval = 10*1000;
	
	
	private Set<ProtocolConfiguration> platformConfig;
	private Set<SMLDescription> sensorConfig;
	private File platformConfigFile, sensorConfigFile;
	private AtomicBoolean pcChanged, scChanged;
	private WatchSCThread scThread;
	private WatchPCThread pcThread;
	
	
	
	/**
	 * This method returns an instance of the File configuration service
	 * @return an instance of the File configuration service
	 */
	public static FileConfigService getService() { return c; }

	/**
	 * Private constructor
	 */
	private FileConfigService() {
		pcChanged = new AtomicBoolean(false);
		scChanged = new AtomicBoolean(false);
		scThread = new WatchSCThread();
		pcThread = new WatchPCThread();
		platformConfig = new HashSet<ProtocolConfiguration>();
		sensorConfig = new HashSet<SMLDescription>();
		scThread.start();
		pcThread.start();
	}
	
	/**
	 * This method initialises the file configuration service. it checks that both sensor and platform
	 * configuration files exist and are readable/writeable. It then loads their content in memory.
	 * @param pc the full path to the platform configuration file
	 * @param sc the full path to the sensor configuration file
	 * @throws ConfigurationException if either file is unreadable, unwritable  or both
	 */
	public synchronized void init(String pc, String sc) throws ConfigurationException{
		File p = new File(pc);
		File s = new File(sc);
		if (p.exists()) {
			if (!p.isFile())
				throw new ConfigurationException("Should not be a directory: " + p);
			
		    if (!p.canRead())
		    	throw new ConfigurationException("File cannot be read: " + p);

		    if (!p.canWrite())
		    	throw new ConfigurationException("File cannot be written: " + p);
		} else {
			logger.debug("Platform config file " + p.getName() +" does not exist - creating one");
			try {writeDocumentToFile(p, ProtocolConfigurations.createEmptyXML());}
			catch (IOException e) {
				logger.error("Cant write an empty platform config file - " +e.getMessage());
				throw new ConfigurationException("Cant create platform config file");
			}
		}


		if (s.exists()) {
			if (!s.isFile())
				throw new ConfigurationException("Should not be a directory: " + s);
		
		    if (!s.canRead())
		    	throw new ConfigurationException("File cannot be read: " + s);
			
		    if (!s.canWrite())
		    	throw new ConfigurationException("File cannot be written: " + s);
			
		} else {
			logger.debug("Sensor config file " + s.getName() +" does not exist - creating one");
			try {writeDocumentToFile(s, SMLDescriptions.createEmptySML());}
			catch (Exception e) {
				logger.error("Cant write an empty sensor config file - " +e.getMessage());
				e.printStackTrace();
				throw new ConfigurationException("Cant create sensor config file");
			}
		}

		platformConfigFile = p;
		sensorConfigFile = s;
		reloadConfig();
	}
	
	private void reloadConfig() throws ConfigurationException{
		String tmp;
		StringBuffer s = new StringBuffer();
		BufferedReader b;
		try {
			b = new BufferedReader(new FileReader(platformConfigFile));
			while((tmp = b.readLine()) != null) s.append(tmp);
			synchronized(platformConfig){
				platformConfig = new ProtocolConfigurations(s.toString()).getConfigurations();
			}
		} catch (ParserConfigurationException e) {
			logger.error("Could not parse the XML platform configuration file");
			e.printStackTrace();
			throw new ConfigurationException("Platform configuration file malformed");
		} catch (IOException e) {
			logger.error("Could not find platform configuration file: " + e.getMessage());
			e.printStackTrace();
			throw new ConfigurationException("Platform configuration file not found");
		}
		
		try {			
			s.delete(0, s.length());
			b = new BufferedReader(new FileReader(sensorConfigFile));
			while((tmp = b.readLine()) != null) s.append(tmp);
			synchronized(sensorConfig){
				sensorConfig = new SMLDescriptions(s.toString()).getDescriptions();
			}
		} catch (ParserConfigurationException e) {
			logger.error("Could not parse the XML sensor configuration file");
			e.printStackTrace();
			throw new ConfigurationException("Sensor configuration file malformed");
		} catch (IOException e) {
			logger.error("Could not find sensor configuration file: " + e.getMessage());
			e.printStackTrace();
			throw new ConfigurationException("Sensor configuration file not found");
		}
		
	}
	
	/**
	 * This method stops FileConfigService
	 */
	public synchronized void stop(){
		scThread.stop();
		pcThread.stop();
	}
	
	/**
	 * This method adds the given protocol configuration object to the platform config file
	 * @param c the protocol config to be added
	 * @throws ConfigurationException if the protocol config is already present in the platform config file
	 */
	public void addProtocol(ProtocolConfiguration c) throws ConfigurationException{
		synchronized(platformConfig){
			if(!platformConfig.add(c))
				throw new ConfigurationException("Platform config already has this protocol '"+c.getXMLString()+"'");
		}
		pcChanged.set(true);
	}
	
	/**
	 * This method goes through the platform config file and looks for a protocol with the matching configuration
	 * name and value. If found, the protocol configuration object is returned
	 * @param param the name of the configuration parameter
	 * @param value the value of the configuration parameter
	 * @return the matching protocol's configuration object 
	 * @throws ConfigurationException if not found 
	 */
	public ProtocolConfiguration findProtocol(String param, String value) throws ConfigurationException{
		ProtocolConfiguration p;
		synchronized(platformConfig){
			Iterator<ProtocolConfiguration> i = platformConfig.iterator();
			while(i.hasNext()){
				p = i.next();
				if(p.getParameters().hasValue(param, value))
					return p;
			}
		}
		throw new ConfigurationException("No protocol with matching configuration found");
	}
	
	/**
	 * This method removes the given protocol configuration from the protocol config file
	 * @param pid the Id of the protocol to be remvoed
	 * @throws ConfigurationException if the protocol doesnt exist in the current config file
	 */
	public void removeProtocol(ProtocolID pid) throws ConfigurationException{
		synchronized(platformConfig){
			Iterator<ProtocolConfiguration> i = platformConfig.iterator();
			while(i.hasNext())
				if(i.next().getID().equals(pid.getName())) {
					i.remove();
					pcChanged.set(true);
					return;
				}
		}
		throw new ConfigurationException("Protocol configuration for ID '"+pid.getName()+"' not found");
	}
	
	/**
	 * This method adds an SML description to the sensor config
	 * @param s the SMl description to be added
	 * @throws ConfigurationException if the given description is already present in the sensor config
	 */
	public void addSensor(SMLDescription s) throws ConfigurationException{
		synchronized(sensorConfig){
			if(!sensorConfig.add(s))
				throw new ConfigurationException("Sensor config already has this sensor '"+s.getSMLString()+"'");
		}
		scChanged.set(true);
	}
	
	
	/**
	 * This method removes the SML description from the sensor config file
	 * @param sid the ID of the sensor to be removed
	 * @throws ConfigurationException if the sensor doesnt exist in the current config file
	 */
	public void removeSensor(SensorID sid) throws ConfigurationException{
		SMLDescription s;
		synchronized(sensorConfig){
			Iterator<SMLDescription> i = sensorConfig.iterator();
			while(i.hasNext()){
				s = i.next();
				if(s.getID().equals(sid.getName())) {
					i.remove();
					scChanged.set(true);
					return;
				}
			}
		}
		throw new ConfigurationException("Sensor configuration for ID '"+sid.getName()+"'not found");
	}
	
	/**
	 * This method returns a set of sensors' SML description for all sensors belonging to the specified protocol 
	 * @param pid the ProtocolID of the protocol whose sensors are to be listed
	 * @return a set of SML description for sensors associated with the protocol with the given PID
	 */
	public Set<SMLDescription> listSensors(ProtocolID pid){
		SMLDescription s;
		HashSet<SMLDescription> v = new HashSet<SMLDescription>();
		synchronized(sensorConfig){
			Iterator<SMLDescription> i = sensorConfig.iterator();
			while(i.hasNext()){
				s = i.next();
				if(s.getProtocolName().equals(pid.getName()))
					v.add(s);
			}
		}
	    return v;
	}
	
	/**
	 * This method removes sensors' SML description for all sensors belonging to a specified protocol 
	 * @param pid the ProtocolID of the protocol whose sensors are to be removed
	 */
	public void removeSensors(ProtocolID pid){
		synchronized(sensorConfig){
			Iterator<SMLDescription> i = sensorConfig.iterator();
			while(i.hasNext())
				if(i.next().getProtocolName().equals(pid.getName())) 
					i.remove();
		}
		scChanged.set(true);
	}	
	
	/**
	 * This method looks in the sensor configuration file for a sensor semantically identical to the
	 * one given in argument 's'. If one is found, a new SensorID object based on the sid found in the
	 * config file for that sensor is returned. 
	 * @param s the SML description whose parameters will be looked for in the sensor configuration file
	 * @return a new SensorID as found in the configuration document
	 * @throws ConfigurationException if the sensor Id cant be found
	 */
	public SensorID findSensor(SMLDescription s) throws ConfigurationException{
		SMLDescription t;
		synchronized(sensorConfig){
			Iterator<SMLDescription> i = sensorConfig.iterator();
			while(i.hasNext()) {
				t = i.next();
				if(t.isSame(s))
					return new SensorID(t.getID());
			}
		}
		throw new ConfigurationException("SML description not found");
	}
	
	/**
	 * This method returns a copy of all protocol configuration objects as found in the configuration file
	 * @return a set of all protocol configuration objects
	 */
	public Set<ProtocolConfiguration> getProtocols(){
		synchronized(platformConfig){
			return new HashSet<ProtocolConfiguration>(platformConfig);
		}	
	}
	
	/**
	 * This method returns a copy of all protocol configuration objects as found in the configuration file
	 * @return a set of all protocol configuration objects
	 */
	public Set<SMLDescription> getSensors(){
		synchronized(sensorConfig){
			return new HashSet<SMLDescription>(sensorConfig);
		}	
	}
	
	/**
	 * This returns a set of Sensor IDs currently found in the sensor config file
	 * @return a set of Sensor IDs currently found in the sensor config file
	 */
	public synchronized Set<String> listSensorID(){
		HashSet<String> s = new HashSet<String>();
		synchronized(sensorConfig){
			Iterator<SMLDescription> i = sensorConfig.iterator();
			while(i.hasNext())
				s.add(i.next().getID());			
		}
		return s;
	}

	private void writeDocumentToFile(File f, Document d) throws IOException{
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write(XMLhelper.toString(d));
        out.close();
	}
	
	private void writePCConfig() throws ConfigurationException, IOException {
		ProtocolConfigurations pc;
		synchronized(platformConfig){
			pc = new ProtocolConfigurations(platformConfig);
		}
		logger.debug("writing PC file");
		writeDocumentToFile(platformConfigFile, pc.getXML());		
	}
	
	private void writeSCConfig() throws ConfigurationException, IOException {
		SMLDescriptions sc;
		synchronized(sensorConfig){
			sc = new SMLDescriptions (sensorConfig);
		}
		logger.debug("writing SC file");
		writeDocumentToFile(sensorConfigFile, sc.getSML());		
	}
	
	private class WatchPCThread implements Runnable{
		private Thread t;
		
		/**
		 * Creates a watch thread kicking in every 'i' milliseconds
		 * @param i the check interval in milliseconds
		 */
		public WatchPCThread(){
			t = new Thread(this, "PCWatcher");
		}
		
		public void start(){
			t.start();
		}
		
		public void stop(){
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {}
			logger.debug("Watch thread joined");
		}
		
		public void run() {
			logger.debug("Watch thread started");
			try {
				while(!Thread.interrupted()){
					if(pcChanged.compareAndSet(true, false)) {
						try {
							writePCConfig();
						} catch (ConfigurationException e) {
							logger.error("Cant write platform config");
							e.printStackTrace();
						} catch (IOException e) {
							logger.error("Cant write platform config");
							e.printStackTrace();
						}
					}
					Thread.sleep(platformConfigWriteInterval);
				}				
			} catch (InterruptedException e) {}
			logger.debug("Watch thread exited");
		}
	}
	
	private class WatchSCThread implements Runnable{
		private Thread t;
		
		/**
		 * Creates a watch thread kicking in every 'i' milliseconds
		 * @param i the check interval in milliseconds
		 */
		public WatchSCThread(){
			t = new Thread(this, "SCWatcher");
		}
		
		public void start(){
			t.start();
		}
		
		public void stop(){
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {}
			logger.debug("Watch thread joined");
		}
		
		public void run() {
			logger.debug("Watch thread started");
			try {
				while(!Thread.interrupted()){
					if(scChanged.compareAndSet(true, false))
						try {
							writeSCConfig();
						} catch (ConfigurationException e) {
							logger.error("Cant write sensor config");
							e.printStackTrace();
						} catch (IOException e) {
							logger.error("Cant write sensor config");
							e.printStackTrace();
						}
					Thread.sleep(sensorConfigWriteInterval);
				}				
			} catch (InterruptedException e) {}
			logger.debug("Watch thread exited");
		}
	}
}
