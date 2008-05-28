/**
 * 
 */
package jcu.sal.utils;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jcu.sal.components.protocols.osData.OSDataConstants;
import jcu.sal.components.protocols.owfs.OWFSProtocol;
import jcu.sal.components.protocols.simpleSNMP.SSNMPProtocol;
import jcu.sal.components.protocols.v4l2.V4L2Protocol;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class ProtocolModulesList {
	private static ProtocolModulesList e = new ProtocolModulesList();
	
	public final static String SALcomponentPackage = "jcu.sal.components.protocols.";
	
	private Map<String,String> protocolTable = new Hashtable<String,String>();
	private List<String> probeTable = new LinkedList<String>();
	private Logger logger = Logger.getLogger(ProtocolModulesList.class);


	private ProtocolModulesList()
	{
		Slog.setupLogger(this.logger);
		protocolTable.put(OWFSProtocol.OWFSPROTOCOL_TYPE , SALcomponentPackage + "owfs.OWFSProtocol");
		
		/*table.put("PL40", ProtocolModulesList.SALcomponentPackage + "PLIProtocol");*/
		protocolTable.put(SSNMPProtocol.SIMPLESNMPPROTOCOL_TYPE, SALcomponentPackage + "simpleSNMP.SSNMPProtocol");
		protocolTable.put(OSDataConstants.OSDATAPROTOCOL_TYPE, SALcomponentPackage + "osData.OSDataProtocol");
		protocolTable.put(V4L2Protocol.V4L2PROTOCOL_TYPE, SALcomponentPackage + "v4l2.V4L2Protocol");
		
		probeTable.add(SALcomponentPackage + "v4l2.HalClient");
	}
	
	public static String getProtocolClassName(String type) throws ClassNotFoundException
	{	
		String c = e.protocolTable.get(type);
		if (c==null) {
			e.logger.error("Cant find the protocol class name from protocol type: " + type);
			throw new ClassNotFoundException("Cant find the protocol class name from protocol type: " + type);
		}
		else e.logger.debug("Found protocol class " + c + " for type " + type);
		
		return c;
	}
	
	public static List<String> getProbeClassName() throws ClassNotFoundException
	{	
		return e.probeTable;
	}
}
