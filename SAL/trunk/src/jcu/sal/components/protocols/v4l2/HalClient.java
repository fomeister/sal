package jcu.sal.components.protocols.v4l2;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import jcu.sal.components.Identifier;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.managers.ProtocolManager;
import jcu.sal.utils.Slog;

import au.edu.jcu.haldbus.AbstractDeviceDetection;
import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.GenericMatch;
import au.edu.jcu.haldbus.match.VectorMatch;


public class HalClient extends AbstractDeviceDetection {
	private static Logger logger = Logger.getLogger(HalClient.class);
	private ProtocolManager pm;
	
	public HalClient() throws InvalidConstructorArgs, AddRemoveElemException{
		Slog.setupLogger(logger);
		addMatch("1-capability", new VectorMatch<String>("info.capabilities", "video4linux"));
		addMatch("2-category", new GenericMatch<String>("info.category", "video4linux"));
		addMatch("3-category.capture", new VectorMatch<String>("info.capabilities", "video4linux.video_capture"));
		addMatch("4-video4linux.device", new GenericMatch<String>("video4linux.device", "video", true,true));
		addMatch("5-deviceFile", new AlwaysMatch("linux.device_file"));
		addMatch("6-info.product", new NextMatch("@info.parent", new AlwaysMatch("info.product")));
		addMatch("7-info.vendor", new NextMatch("@info.parent", new AlwaysMatch("info.vendor")));
		pm = ProtocolManager.getProcotolManager();
	}

	@Override
	public void doAction(Map<String,String> l) {
		AbstractProtocol p;
		System.out.println("Found "+l.get("6-info.product")+" - "+l.get("7-info.vendor")+ " on "+l.get("5-deviceFile"));
		try {
			List<Identifier> i = pm.getComponentsOfType(V4L2Protocol.V4L2PROTOCOL_TYPE);
			Iterator<Identifier> iter = i.iterator();
			while(iter.hasNext()){
				try {
					p =pm.getComponent(iter.next()); 
					if(p.getConfig(V4L2Protocol.V4L2D_DEVICE_ATTRIBUTE_TAG).equals(l.get("5-deviceFile")))
						logger.debug("Device file "+l.get("5-deviceFile")+" seems to be already used by "+ p.toString());
				} catch (BadAttributeValueExpException e) {}
			}
		} catch (ConfigurationException e) {
			logger.debug("No protocol of type "+V4L2Protocol.V4L2PROTOCOL_TYPE);
		}
		logger.debug("noone seems to be using "+l.get("5-deviceFile"));
		
		
		
	}

	@Override
	public String getName() {
		return "V4L HAL DBus client";
	}
}
