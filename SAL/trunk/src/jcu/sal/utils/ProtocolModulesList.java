/**
 * 
 */
package jcu.sal.utils;

import java.util.Hashtable;

import jcu.sal.Components.Protocols.OSDataProtocol;
import jcu.sal.Components.Protocols.OwfsProtocol;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class ProtocolModulesList {
	private static ProtocolModulesList e = new ProtocolModulesList();
	
	public final static String SALcomponentPackage = "jcu.sal.Components.Protocols.";
	
	private Hashtable<String,String> table = new Hashtable<String,String>();
	private Logger logger = Logger.getLogger(ProtocolModulesList.class);


	private ProtocolModulesList()
	{
		Slog.setupLogger(this.logger);
		table.put(OwfsProtocol.OWFSPROTOCOL_TYPE , ProtocolModulesList.SALcomponentPackage + "OwfsProtocol");
		/*table.put("PL40", ProtocolModulesList.SALcomponentPackage + "PLIProtocol");*/
		table.put("SSNMP", ProtocolModulesList.SALcomponentPackage + "SimpleSNMPProtocol");
		table.put(OSDataProtocol.OSDATAPROTOCOL_TYPE, ProtocolModulesList.SALcomponentPackage + "OSDataProtocol");
	}
	
	public static String getClassName(String type) throws ClassNotFoundException
	{	
		String c = e.table.get(type);
		if (c==null) {
			e.logger.error("Cant find the class name from Protocol type: " + type);
			throw new ClassNotFoundException("Cant find the class name from Protocol type: " + type);
		}
		else e.logger.debug("Found class " + c + " for type " + type);
		
		return c;
	}
}
