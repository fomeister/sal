package jcu.sal.components.protocols.osData;


import java.util.Map;

import jcu.sal.components.EndPoints.FSEndPoint;
import jcu.sal.components.protocols.AbstractHalClient;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.match.GenericMatch;


public class HalClient extends AbstractHalClient {
	private static Logger logger = Logger.getLogger(HalClient.class);

	private final String defaultDoc = "<Protocol name=\"osdata\" type=\""+OSDataConstants.PROTOCOL_TYPE+"\">"
								+"<EndPoint name=\""+FSEndPoint.FSENDPOINT_TYPE+"-osdata\" type=\""+FSEndPoint.FSENDPOINT_TYPE+"\"/>"
								+"<parameters/>"
								//+"<Param name=\"deviceFile\" value=\"%DEVICE%\"/>"
                    			//+"</parameters></Protocol>";
								+"</Protocol>";
                    			

	
	public HalClient() throws InvalidConstructorArgs, AddRemoveElemException{
		Slog.setupLogger(logger);		
		addMatch("1-volume.mount_point", new GenericMatch<String>("volume.mount_point", "/"));
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		logger.debug("Found root filesystem");
		if(!isProtocolRunning(OSDataConstants.PROTOCOL_TYPE)){
			try {
				createProtocol(XMLhelper.createDocument(defaultDoc));
			} catch (Exception e) {
				logger.error("Instancation failed");
			}
		} else
			logger.debug("OSdata already instanciated");
	}
	
	@Override
	public void deviceRemoved(Map<String, String> l) {}

	@Override
	public String getName() {
		return "OS Data HAL DBus client";
	}

}
