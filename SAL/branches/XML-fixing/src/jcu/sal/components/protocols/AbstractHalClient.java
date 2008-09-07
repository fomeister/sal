package jcu.sal.components.protocols;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.components.Identifier;
import jcu.sal.config.FileConfigService;
import jcu.sal.managers.ProtocolManager;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import au.edu.jcu.haldbus.AbstractDeviceDetection;

public abstract class AbstractHalClient extends AbstractDeviceDetection {
	private ProtocolManager pm;
	private static Logger logger = Logger.getLogger(AbstractHalClient.class);
	
	protected AbstractHalClient(int when){
		super(when);
		Slog.setupLogger(logger);
		pm = ProtocolManager.getProcotolManager();
	}
	
	protected AbstractHalClient(){
		this(ALWAYS_RUN_FLAG);
	}
	
	/**
	 * This method returns whether a protocol of a given type is already isntanciated
	 * @param type the type of protocols
	 * @return whether a protocol of a given type is already isntanciated
	 */
	protected boolean isProtocolRunning(String type){
		try {
			return pm.getComponentsOfType(type).size()!=0;
		} catch (ConfigurationException e) {
			return false;
		}
	}
	
	/**
	 * This method returns a Map of configuration directives for each protocol of a given type. If there are no protocols of
	 * the given type, an empty list is returned.
	 * @param type the type of protocols
	 * @return a Map of their configuration
	 */
	protected Map<Identifier, Parameters> findRunningProtocolConfigFromType(String type){
		Map<Identifier, Parameters> ret = new Hashtable<Identifier, Parameters>();
		try {
			Iterator<Identifier> i = pm.getComponentsOfType(type).iterator();
			Identifier id;
			while(i.hasNext()) {
				id = i.next();
				ret.put(id, pm.getComponent(id).getConfig());
			}
		} catch (ConfigurationException e){}
			
		return ret;
	}
	
	/**
	 * This method gathers the configuration directives of running protocols of a given type. It then searches these directives
	 * for a specified name/value pair given in second and third argument. The returned list is empty if there are no matches.
	 * @param type the type of running protocols to search  
	 * @param param the name of the directive to search for
	 * @param value the expected value of the searched directive
	 * @return a list of protocol identifiers matching the criteria
	 */
	protected List<Identifier> findRunningProtocolNameFromConfig(String type, String param, String value){
		List<Identifier> l = new Vector<Identifier>();
		Map<Identifier, Parameters> m = findRunningProtocolConfigFromType(type);
		Identifier id;
		Iterator<Identifier> i = m.keySet().iterator();

		while(i.hasNext()){
			id = i.next();
			try {
				if(m.get(id).hasValue(param,value))
					l.add(id);
			} catch (Exception e){}				
		}
		return l;
	}
	
	/**
	 * this method checks the File config service to retreive the whole configuration document of a protocol
	 * given part of it. Given a parameter name (ex: "device_file"), and the expected value (ex: "/dev/video0"),
	 * the File config service will look for a protocol configuration containing this parameter and its value, and if
	 * found, the entire protocl configuration will be returned.  
	 * @param param the parameter name
	 * @param value the expected parameter value
	 * @return the entire protocol configuration, if found 
	 * @throws ConfigurationException if not found
	 */
	protected Document findProtocolConfigFromFile(String param, String value) throws ConfigurationException {
		return FileConfigService.getService().findProtocol(param, value);
	}
	
	protected void createProtocol(Document d) throws ConfigurationException{
		logger.debug("Creating new protocol with document: \n"+XMLhelper.toString(d));
		try {pm.createComponent(d).start();}
		catch (Throwable t){
			logger.error("Cant instanciate protocol");
			t.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	protected void removeProtocol(Identifier i) throws ConfigurationException{
		logger.debug("Removing existing protocol "+i.toString());
		pm.destroyComponent(i);
		pm.removeProtocolConfig((ProtocolID) i, false);
	}
	
}
