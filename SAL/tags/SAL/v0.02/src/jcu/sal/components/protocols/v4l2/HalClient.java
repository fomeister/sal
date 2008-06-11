package jcu.sal.components.protocols.v4l2;


import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.components.Identifier;
import jcu.sal.components.protocols.AbstractHalClient;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.GenericMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.VectorMatch;


public class HalClient extends AbstractHalClient {
	private static Logger logger = Logger.getLogger(HalClient.class);

	private final String defaultDoc = "<Protocol name=\"%NAME%\" type=\""+V4L2Protocol.PROTOCOL_TYPE+"\">"
								+"<EndPoint name=\"%SUBSYSNAME%\" type=\"%SUBSYS%\"/>"
								+"<parameters>"
								+"<Param name=\"deviceFile\" value=\"%DEVICE%\"/>"
                    			+"</parameters></Protocol>";
                    			

	
	public HalClient() throws InvalidConstructorArgs, AddRemoveElemException{
		Slog.setupLogger(logger);
		
		addMatch("1-capability", new VectorMatch<String>("info.capabilities", "video4linux"));
		addMatch("2-category", new GenericMatch<String>("info.category", "video4linux"));
		addMatch("3-category.capture", new VectorMatch<String>("info.capabilities", "video4linux.video_capture"));
		addMatch("4-video4linux.device", new GenericMatch<String>("video4linux.device", "video", true,true));
		addMatch("5-deviceFile", new AlwaysMatch("linux.device_file"));
		addMatch("7-linux.subsystem", new NextMatch("@info.parent", new AlwaysMatch("linux.subsystem")));
		addMatch("8-info.product", new NextMatch("@info.parent", new AlwaysMatch("info.product")));
		addMatch("9-info.vendor", new NextMatch("@info.parent", new AlwaysMatch("info.vendor")));
		
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		logger.debug("Found "+l.get("8-info.product")+" - "+l.get("9-info.vendor")+ " on "+l.get("5-deviceFile"));
		Document d = null;
		String doc;

		try {
			//check if a running protocol already uses our device file (can happen during the initial run if for instance a protocol is 
			//instanciated with its config taken from the platform config file, and the device is detected again during the initial run)
			if(findRunningProtocolNameFromConfig(V4L2Protocol.PROTOCOL_TYPE, V4L2Protocol.DEVICE_ATTRIBUTE_TAG, l.get("5-deviceFile")).size()!=0){
				logger.debug("An existing protocol already uses "+l.get("5-deviceFile"));
				return;
			}
			
			//the device is available. check if there is an existing config for it in the  PlatformConfig file
			d = findProtocolConfigFromFile(V4L2Protocol.DEVICE_ATTRIBUTE_TAG, l.get("5-deviceFile"));
			logger.debug("Found config for "+l.get("5-deviceFile")+" in platformConfig file - reusing it");
		} catch (ConfigurationException e) {
			//if we re here, there is no pre exiting config for this device file, so we create a generic one
			logger.debug("No existing configuration for V4L protocol with device "+l.get("5-deviceFile"));
			//Add the name
			doc = defaultDoc.replaceFirst("%NAME%", "v4l-"+l.get("5-deviceFile"));
			//add the subsytem name
			doc = doc.replaceFirst("%SUBSYSNAME%", l.get("7-linux.subsystem")+"-"+l.get("5-deviceFile"));
			//add the subsytem name
			doc = doc.replaceFirst("%SUBSYS%", l.get("7-linux.subsystem"));
			//Add the device file
			doc = doc.replaceFirst("%DEVICE%", l.get("5-deviceFile"));
			try {
				d = XMLhelper.createDocument(doc);
				logger.debug(doc);
			} catch (ParserConfigurationException e1) {
				// shouldnt be here if defaultDoc is properly formed
				e1.printStackTrace();
				return;
			}
		}
		try {
			createProtocol(d);
		} catch (ConfigurationException e) {
			logger.error("Instancation failed");
		}
	}
	
	@Override
	public void deviceRemoved(Map<String, String> l) {			
		List<Identifier> ids = findRunningProtocolNameFromConfig(V4L2Protocol.PROTOCOL_TYPE, V4L2Protocol.DEVICE_ATTRIBUTE_TAG, l.get("5-deviceFile"));
		if(ids.size()==0)
			return;

		if(ids.size()>1)
			logger.debug("More than one protocol was handling device "+l.get("5-deviceFile")+" it seems. Removing  1st one" + ids.get(0).getName());
		try {
			removeProtocol(ids.get(0));
		} catch (ConfigurationException e) {
			logger.error("Couldnt remove protocol "+ids.get(0).getName());
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "V4L HAL DBus client";
	}

}
