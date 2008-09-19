/**
 * 
 */
package jcu.sal.utils;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jcu.sal.components.protocols.dummy.DummyProtocol;
import jcu.sal.components.protocols.osData.OSDataConstants;
import jcu.sal.components.protocols.owfs.OWFSProtocol;
import jcu.sal.components.protocols.simpleSNMP.SSNMPProtocol;
import jcu.sal.components.protocols.v4l2.V4L2Protocol;
import jcu.sal.config.deviceDetection.HalHelper;

import org.apache.log4j.Logger;

/**
 * FIXME: This class needs rework.   
 * @author gilles
 */
public class ProtocolModulesList {
	private static ProtocolModulesList e = new ProtocolModulesList();
	
	public final static String SALcomponentPackage = "jcu.sal.components.protocols.";	
	/**
	 * Map of all existing protocol names and their associated class 
	 */
	private Map<String,String> protocolTable = new Hashtable<String,String>();
	

	/**
	 * List of all Device Detection Filter classes
	 */
	private Map<String, List<String>> filterTable = new Hashtable<String,List<String>>();
	
	private Logger logger = Logger.getLogger(ProtocolModulesList.class);


	private ProtocolModulesList() {
		Slog.setupLogger(this.logger);
		protocolTable.put(OWFSProtocol.OWFSPROTOCOL_TYPE , SALcomponentPackage + "owfs.OWFSProtocol");
		
		/*table.put("PL40", ProtocolModulesList.SALcomponentPackage + "PLIProtocol");*/
		protocolTable.put(SSNMPProtocol.PROTOCOL_TYPE, SALcomponentPackage + "simpleSNMP.SSNMPProtocol");
		protocolTable.put(OSDataConstants.PROTOCOL_TYPE, SALcomponentPackage + "osData.OSDataProtocol");
		protocolTable.put(V4L2Protocol.PROTOCOL_TYPE, SALcomponentPackage + "v4l2.V4L2Protocol");
		protocolTable.put(DummyProtocol.PROTOCOL_TYPE, SALcomponentPackage + "dummy.DummyProtocol");

		if(filterTable.get(HalHelper.NAME)==null)
			filterTable.put(HalHelper.NAME, new LinkedList<String>());
		filterTable.get(HalHelper.NAME).add(SALcomponentPackage + "v4l2.HalClient");
		filterTable.get(HalHelper.NAME).add(SALcomponentPackage + "owfs.HalClient");
		filterTable.get(HalHelper.NAME).add(SALcomponentPackage + "osData.HalClient");
	}
	
	public static String getProtocolClassName(String type) throws ClassNotFoundException {	
		String c = e.protocolTable.get(type);
		if (c==null) {
			e.logger.error("Cant find the protocol class name from protocol type: " + type);
			throw new ClassNotFoundException("Cant find the protocol class name from protocol type: " + type);
		}
		//else e.logger.debug("Found protocol class " + c + " for type " + type);
		
		return c;
	}
	
	public static List<String> getFilter(String fitlerName) {
		if(e.filterTable.get(fitlerName)!=null)
			return new LinkedList<String>(e.filterTable.get(fitlerName));
		else 
			return new LinkedList<String>();
	}

}
