/**
 * 
 */
package jcu.sal.Agent;

import javax.naming.ConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Config.ConfigService;
import jcu.sal.Managers.ProtocolManager;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author gilles
 *
 */
public class SALAgent {
	
	private Logger logger = Logger.getLogger(SALAgent.class);
	
	public SALAgent() {
		Slog.setupLogger(logger);
		
		Document d = null;
		NodeList nl = null;
		Node n = null;
		ConfigService conf = ConfigService.getService();
		ProtocolManager pm = ProtocolManager.getProcotolManager();
		
		try {
			conf.init("/home/gilles/workspace/SALv1/src/sensors1.xml", "/home/gilles/workspace/SALv1/src/sensors.xml");
			d = conf.getPlatformConfig();
			nl = XMLhelper.getNodeList("//" + Protocol.PROTOCOL_TAG, d);
			for(int i=0; i<nl.getLength(); i++) {
				n = nl.item(i);
				logger.debug("about to create a protocol: ");
				logger.debug(XMLhelper.toString(n));
				pm.createComponent(n);
			}
			
		} catch (ConfigurationException e) {
			logger.error("Could not read the configuration files.");
		} catch (XPathExpressionException e) {
			logger.error("Cannot parse the XML document");
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		SALAgent s = new SALAgent();
	}

}
