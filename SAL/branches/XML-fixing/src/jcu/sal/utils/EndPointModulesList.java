/**
 * 
 */
package jcu.sal.utils;

import java.util.Hashtable;

import jcu.sal.components.EndPoints.EthernetEndPoint;
import jcu.sal.components.EndPoints.FSEndPoint;
import jcu.sal.components.EndPoints.PCIEndPoint;
import jcu.sal.components.EndPoints.SerialEndPoint;
import jcu.sal.components.EndPoints.UsbEndPoint;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class EndPointModulesList {
	private static EndPointModulesList e = new EndPointModulesList();
	
	public final static String SALcomponentPackage = "jcu.sal.components.EndPoints.";
	
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
