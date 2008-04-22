/**
 * 
 */
package jcu.sal.utils;

import java.util.Hashtable;

import jcu.sal.Components.EndPoints.EthernetEndPoint;
import jcu.sal.Components.EndPoints.FSEndPoint;
import jcu.sal.Components.EndPoints.PCIEndPoint;
import jcu.sal.Components.EndPoints.SerialEndPoint;
import jcu.sal.Components.EndPoints.UsbEndPoint;

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
		table.put(UsbEndPoint.USBENDPOINT_TYPE, SALcomponentPackage + "UsbEndPoint");
		table.put(SerialEndPoint.SERIALENDPOINT_TYPE, SALcomponentPackage + "SerialEndPoint");
		table.put(EthernetEndPoint.ETHERNETENDPOINT_TYPE , SALcomponentPackage + "EthernetEndPoint");
		table.put(FSEndPoint.FSENDPOINT_TYPE, SALcomponentPackage + "FSEndPoint");
		table.put(PCIEndPoint.PCIENDPOINT_TYPE, SALcomponentPackage + "PCIEndPoint");
	}
	
	public static String getClassName(String type) throws ClassNotFoundException
	{	
		String c = e.table.get(type);
		if (c==null) {
			e.logger.error("Cant find the class name from EndPoint type: " + type);
			throw new ClassNotFoundException("Cant find the class name from EndPoint type: " + type);
		}
		else e.logger.debug("Found class " + c + " for type " + type);
		
		return c;
	}
}
