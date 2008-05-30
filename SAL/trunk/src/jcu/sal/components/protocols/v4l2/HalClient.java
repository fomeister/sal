package jcu.sal.components.protocols.v4l2;


import java.util.Map;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.config.FileConfigService;
import jcu.sal.managers.ProtocolManager;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import au.edu.jcu.haldbus.AbstractDeviceDetection;
import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.GenericMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.VectorMatch;


public class HalClient extends AbstractDeviceDetection {
	private static Logger logger = Logger.getLogger(HalClient.class);
	private ProtocolManager pm;
	private String defaultDoc = "<Protocol name=\"%NAME%\" type=\""+V4L2Protocol.PROTOCOL_TYPE+"\">"
								+"<EndPoint name=\"%SUBSYSNAME%\" type=\"%SUBSYS%\"/>"
								+"<parameters>"
								+"<Param name=\"deviceFile\" value=\"/dev/video0\"/>"
								+"<Param name=\"width\" value=\"640\"/>"
                    			+"<Param name=\"height\" value=\"480\"/>"
                    			+"<Param name=\"standard\" value=\"0\"/>"
                    			+"<Param name=\"channel\" value=\"0\"/>"
                    			+"</parameters></Protocol>";
                    			

	
	public HalClient() throws InvalidConstructorArgs, AddRemoveElemException{
		Slog.setupLogger(logger);
		pm = ProtocolManager.getProcotolManager();
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
	public void doAction(Map<String,String> l) {
		logger.debug("Found "+l.get("8-info.product")+" - "+l.get("9-info.vendor")+ " on "+l.get("5-deviceFile"));
		Document d = null;
		try {
			d = FileConfigService.getService().findProtocol(V4L2Protocol.DEVICE_ATTRIBUTE_TAG, l.get("5-deviceFile"));
		} catch (ConfigurationException e) {
			logger.debug("No existing configuration for V4L protocol with device "+l.get("5-deviceFile"));
			//Add the name
			defaultDoc = defaultDoc.replaceFirst("%NAME%", l.get("5-deviceFile"));
			//add the subsytem name
			defaultDoc = defaultDoc.replaceFirst("%SUBSYSNAME%", l.get("7-linux.subsystem")+"-"+l.get("5-deviceFile"));
			//add the subsytem name
			defaultDoc = defaultDoc.replaceFirst("%SUBSYS%", l.get("7-linux.subsystem"));
			try {
				d = XMLhelper.createDocument(defaultDoc);
				logger.debug(defaultDoc);
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			pm.createComponent(d);
		} catch (ConfigurationException e) {
			logger.error("Instancation failed");
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "V4L HAL DBus client";
	}
}
