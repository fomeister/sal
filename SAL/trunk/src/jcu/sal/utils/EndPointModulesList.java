/**
 * 
 */
package jcu.sal.utils;

import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class EndPointModulesList {
	private static EndPointModulesList e = new EndPointModulesList();
	
	public final static String SALcomponentPackage = "jcu.sal.Components.EndPoints.";
	
	private Hashtable<String,String> table = new Hashtable<String,String>();
	private Logger logger = Logger.getLogger(EndPointModulesList.class);


	private EndPointModulesList()
	{
		Slog.setupLogger(this.logger);
		table.put("usb", EndPointModulesList.SALcomponentPackage + "UsbEndPoint");
		table.put("serial", EndPointModulesList.SALcomponentPackage + "SerialEndPoint");
	}
	
	public static String getClassName(String type)
	{	
		String c = e.table.get(type);
		if (c==null)
			e.logger.error("Cant find the class name from EndPointID: " + type);
		
		e.logger.debug("Found class " + c + " for type " + type);
		return c;
	}
}
