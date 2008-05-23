/**
 * 
 */
package jcu.sal.utils;

import java.util.Hashtable;

import jcu.sal.components.protocols.osData.OSDataConstants;
import jcu.sal.components.protocols.owfs.OWFSProtocol;
import jcu.sal.components.protocols.v4l2.V4L2Protocol;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class ProtocolModulesList {
	private static ProtocolModulesList e = new ProtocolModulesList();
	
	public final static String SALcomponentPackage = "jcu.sal.components.protocols.";
	
	private Hashtable<String,String> protocolTable = new Hashtable<String,String>();
	private Hashtable<String,String> sensorProbeTable = new Hashtable<String,String>();
	private Logger logger = Logger.getLogger(ProtocolModulesList.class);


	private ProtocolModulesList()
	{
		Slog.setupLogger(this.logger);
		protocolTable.put(OWFSProtocol.OWFSPROTOCOL_TYPE , ProtocolModulesList.SALcomponentPackage + "owfs.OWFSProtocol");
		
		/*table.put("PL40", ProtocolModulesList.SALcomponentPackage + "PLIProtocol");*/
		protocolTable.put("SSNMP", ProtocolModulesList.SALcomponentPackage + "simpleSNMP.SSNMPProtocol");
		protocolTable.put(OSDataConstants.OSDATAPROTOCOL_TYPE, ProtocolModulesList.SALcomponentPackage + "osData.OSDataProtocol");
		protocolTable.put(V4L2Protocol.V4L2PROTOCOL_TYPE, ProtocolModulesList.SALcomponentPackage + "v4l2.V4L2Protocol");
	}
	
	public static String getClassName(String type) throws ClassNotFoundException
	{	
		String c = e.protocolTable.get(type);
		if (c==null) {
			e.logger.error("Cant find the class name from AbstractProtocol type: " + type);
			throw new ClassNotFoundException("Cant find the class name from AbstractProtocol type: " + type);
		}
		else e.logger.debug("Found class " + c + " for type " + type);
		
		return c;
	}
}
