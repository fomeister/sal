package jcu.sal.config.plugins;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.XmlException;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.config.plugins.xml.NameClassType;
import jcu.sal.config.plugins.xml.ProtocolType;
import jcu.sal.config.plugins.xml.SalPlugins;
import jcu.sal.utils.JaxbHelper;

import org.apache.log4j.Logger;

/**
 * This class maintains a list of all known {@link AbstractProtocol}s and their device detection
 * filters.  
 * FIXME: This class needs rework.   
 * @author gilles
 */
public class PluginList {
	private static Logger logger = Logger.getLogger(PluginList.class);
	static {
		Slog.setupLogger(logger);
	}
	
	private static PluginList e;
	static{
		try {
			e = new PluginList();
		} catch (XmlException e) {}
	}
	
	
	/**
	 * Map of all existing protocol names and their associated class 
	 */
	private Map<String,String> protocolTable = new Hashtable<String,String>();

	/**
	 * List of all Device Detection Filter classes
	 */
	private Map<String, List<String>> filterTable = new Hashtable<String,List<String>>();
	
	private Map<String, String> endPointTable = new Hashtable<String,String>();
	
	private Map<String, String> configHelperTable = new Hashtable<String,String>();
	
	private SalPlugins type;
	


	private PluginList() throws XmlException {
		if(System.getProperty("jcu.sal.plugin.configFile")!=null) {
			try {
				type = JaxbHelper.fromFile(SalPlugins.class , new File(System.getProperty("jcu.sal.plugin.configFile")));
			} catch (XmlException e) {
				logger.error("Cant parse plugin XML config");
				e.printStackTrace();
				throw e;
			}
			parseXML();
		}
	}
	
	private void parseXML(){
		parseProtocols();
		parseEndPoints();
		parseConfigPlugins();
	}
	
	private void parseProtocols(){
		for(ProtocolType p: type.getProtocolPlugins().getProtocol()){
			protocolTable.put(p.getName(), p.getClazz());
			for(NameClassType n : p.getConfigPlugin()){
				if(!filterTable.containsKey(n.getName()))
					filterTable.put(n.getName(), new Vector<String>());
				
				filterTable.get(n.getName()).add(n.getClazz());
			}
		}
	}	
	private void parseEndPoints(){
		for(NameClassType n : type.getEndPointPlugins().getEndPoint())
			endPointTable.put(n.getName(), n.getClazz());
	}
	
	private void parseConfigPlugins(){
		for(NameClassType n : type.getConfigPlugins().getConfig())
			configHelperTable.put(n.getName(), n.getClazz());
	}
	
	public static String getProtocolClassName(String type) throws ClassNotFoundException {	
		String c = e.protocolTable.get(type);
		if (c==null) {
			logger.error("Cant find the protocol class name from protocol type: " + type);
			throw new ClassNotFoundException("Cant find the protocol class name from protocol type: " + type);
		}
		//else e.logger.debug("Found protocol class " + c + " for type " + type);
		
		return c;
	}
	
	/**
	 * This method returns a list of class names representing hardware filters for a given
	 * hardware detection probe. For instance, all HAL filters register with this object so
	 * that the {@link HalHelper} can retrieve a list of all its HAL filters. 
	 * @param fitlerName the name of the hardware detection probe, for instance {@link HalHelper#NAME}.
	 * @return a list of class names representing filters for the given hardware detection probe.
	 */
	public static List<String> getFilter(String fitlerName) {
		if(e.filterTable.get(fitlerName)!=null)
			return new LinkedList<String>(e.filterTable.get(fitlerName));
		else 
			return new LinkedList<String>();
	}
	
	public static String getEndPointClassName(String type) throws ClassNotFoundException
	{	
		String c = e.endPointTable.get(type);
		if (c==null) {
			logger.error("Cant find the class name from EndPoint type: " + type);
			throw new ClassNotFoundException("Cant find the class name from EndPoint type: " + type);
		}
		
		return c;
	}
	
	public static List<String> getHelperClassNames(){	
		return new LinkedList<String>(e.configHelperTable.values());
	}
}
