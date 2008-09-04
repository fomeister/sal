package jcu.sal.components.protocols.owfs;


import java.util.Map;

import jcu.sal.components.protocols.AbstractHalClient;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.match.GenericMatch;


public class HalClient extends AbstractHalClient {
	private static Logger logger = Logger.getLogger(HalClient.class);

	private final String defaultDoc = "<Protocol name=\"1wirefs\" type=\""+OWFSProtocol.OWFSPROTOCOL_TYPE+"\">"
								+"<EndPoint name=\"usb-ds9490\" type=\"usb\"/>"
								+"<parameters>"
								+"<Param name=\"Location\" value=\"/opt/owfs/bin/owfs\" />"
								+"<Param name=\"MountPoint\" value=\"/mnt/w1\" />"
                    			+"</parameters></Protocol>";
                    			

	
	public HalClient() throws InvalidConstructorArgs, AddRemoveElemException{
		Slog.setupLogger(logger);
		
		//THe following rules have been commented out because on some machines, USB product and vendor descriptions
		//are not up-to-date and instead of "DS1490F 2-in-1 Fob, 1-Wire adapter" & "Dallas Semiconductor", HAL reports
		//'Unknown (0x2490)' & 'Unknown (0x04fa)'
		//addMatch("1-product", new GenericMatch<String>("info.product", "DS1490F 2-in-1 Fob, 1-Wire adapter"));
		//addMatch("2-vendor", new GenericMatch<String>("info.vendor", "Dallas Semiconductor"));
		addMatch("3-productId", new GenericMatch<Integer>("usb_device.product_id", new Integer(9360)));
		addMatch("4-vendorId", new GenericMatch<Integer>("usb_device.vendor_id", new Integer(1274)));		
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		logger.debug("Found DS9490");
		if(!isProtocolRunning(OWFSProtocol.OWFSPROTOCOL_TYPE)){
			try {
				createProtocol(XMLhelper.createDocument(defaultDoc));
			} catch (Exception e) {
				logger.error("Instancation failed");
			}
		}
	}
	
	@Override
	public void deviceRemoved(Map<String, String> l) {}

	@Override
	public String getName() {
		return "OWFS HAL DBus client";
	}

}
