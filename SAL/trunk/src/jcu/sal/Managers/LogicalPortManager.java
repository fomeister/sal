/**
 * 
 */
package jcu.sal.Managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.Components.Identifiers.LogicalPortID;
import jcu.sal.Components.LogicalPorts.LogicalPort;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author gilles
 * 
 */
public class LogicalPortManager extends ManagerFactory<LogicalPort> {
	
	public static final String LOGICALPORT_TAG="LogicalPort";
	public static final String LOGICALPORTNAME_TAG = "name";
	private Logger logger = Logger.getLogger(LogicalPortManager.class);
	private static LogicalPortManager l = new LogicalPortManager();
	
	/**
	 * Private constructor
	 */
	private LogicalPortManager() {
		super();
		Slog.setupLogger(this.logger);
	}

	/**
	 * Returns the instance of the LogicalPortManager 
	 * @return
	 */
	public static LogicalPortManager getLogicalPortManager() {
		return l;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected LogicalPort build(Document doc) throws InstantiationException {
		LogicalPort lport = null;
		EndPoint ep = null;
		Protocol p = null;

		this.logger.debug("building LogicalPort");
		try {
			LogicalPortID i = (LogicalPortID) this.getComponentID(doc);
			String type = getComponentType(doc);
			logger.debug("LogicalPort name " + i.getName());
			
			//Create the Endpoint
			Node n = XMLhelper.getNode("/" + LOGICALPORT_TAG + "/" + EndPoint.ENPOINT_TAG, doc);
			ep = EndPointManager.getEndPointManager().createComponent(XMLhelper.createDocument(n));
						
			//Create the Protocol and pass it to lp
			if(ep != null) {
				n = XMLhelper.getNode("/" + LOGICALPORT_TAG + "/" + Protocol.PROTOCOL_TAG, doc);
				p = ProtocolManager.getProcotolManager().createComponent(XMLhelper.createDocument(n)); 
			
				if(p!=null) {
					p.setEp(ep);
					lport = new LogicalPort(i, type, p);
				} else {
					EndPointManager.getEndPointManager().destroyComponent(ep.getID());
					logger.error("Couldnt create the Protocol / logical Port");
					throw new InstantiationException("Couldnt create the Protocol / logical Port");
				}
			} else {
				logger.error("Couldnt create the Endpoint / logical Port");
				throw new InstantiationException("Couldnt create the Endpoint / logical Port");
			}
			logger.debug("done building Logical Port "+lport.toString());

			
		} catch (ParseException e) {
			logger.error("Cant get the LogicalPort's ID / type. XML doc:");
			logger.error(XMLhelper.toString(doc));
			e.printStackTrace();
			throw new InstantiationException("Cant create the logical port");
		} catch (XPathExpressionException e) {
			logger.error("Error in XPATH expression for new LogicalPort's EndPoint/Protocol XML configuration. XML doc:");
			logger.error(XMLhelper.toString(doc));
			e.printStackTrace();
			throw new InstantiationException("Cant create the logical port");
		} catch (ParserConfigurationException e) {
			logger.error("Error in transforming new LogicalPort's EndPoint/Protocol XML configuration. XML doc:");
			logger.error(XMLhelper.toString(doc));
			e.printStackTrace();
			throw new InstantiationException("Cant create the logical port");
		} catch (ConfigurationException e) {
			logger.error("Error in Protocol's XML configuration.");
			e.printStackTrace();
			throw new InstantiationException("Cant configure the protocol");
		}
	
		return lport;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentConfig(org.w3c.dom.Document)
	 */
	@Override
	protected Hashtable<String, String> getComponentConfig(Document doc) throws ParseException {
		return new Hashtable<String,String>();
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentType(org.w3c.dom.Document)
	 */
	@Override
	protected String getComponentType(Document doc) throws ParseException{
		return new String(LogicalPort.LOGICALPORT_TYPE);
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(Document doc) throws ParseException {
		Identifier id = null;
		try {
			id = new LogicalPortID(XMLhelper.getAttributeFromName("//" + LOGICALPORT_TAG, LOGICALPORTNAME_TAG, doc));
		} catch (XPathExpressionException e) {
			this.logger.error("Couldnt find the logical port name");
			e.printStackTrace();
			throw new ParseException("Couldnt create the logical port identifier", 0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(LogicalPort component) {
		component.stop();
		component.remove();
	}

	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, BadAttributeValueExpException {
		LogicalPortManager l = getLogicalPortManager();
		l.createComponent(XMLhelper.createDocument("<LogicalPort name='IDU'><EndPoint name='eth0' type='ethernet'><parameters><Param name='EthernetDevice' value='ath0' /><Param name='IPAddress' value='192.168.3.11' /></parameters></EndPoint><Protocol name='EMS_SNMP' type='EMS_IDU_SNMP'><parameters><Param name='AgentIP' value='192.168.0.2' /><Param name='CommunityString' value='EMSOLUTIONS' /><Param name='SNMPVersion' value='1' /></parameters></Protocol></LogicalPort>"));
		//l.createComponent(XMLhelper.createDocument("<LogicalPort name='PL40'><EndPoint name='serial0' type='serial'><parameters><Param name='PortSpeed' value='9600' /><Param name='DataBits' value='8' /><Param name='Parity' value='0' /><Param name='StopBit' value='1' /><Param name='PortDeviceFile' value='/dev/ttyS0' /></parameters></EndPoint></LogicalPort>"));
		l.createComponent(XMLhelper.createDocument("<LogicalPort name='owfs'><EndPoint name='usb2' type='usb' /></LogicalPort>"));
		l.createComponent(XMLhelper.createDocument("<LogicalPort name='owfs1'><EndPoint name='usb2' type='usb' /></LogicalPort>"));
		//l.createComponent(XMLhelper.createDocument("<LogicalPort name='PL401'><EndPoint name='serial0' type='serial'><parameters><Param name='PortSpeed' value='9600' /><Param name='DataBits' value='8' /><Param name='Parity' value='0' /><Param name='StopBit' value='1' /><Param name='PortDeviceFile' value='/dev/ttyS0' /></parameters></EndPoint></LogicalPort>"));
		l.createComponent(XMLhelper.createDocument("<LogicalPort name='IDU2'><EndPoint name='eth0' type='ethernet'><parameters><Param name='EthernetDevice' value='eth0' /><Param name='IPAddress' value='' /></parameters></EndPoint></LogicalPort>"));
		l.createComponent(XMLhelper.createDocument("<LogicalPort name='osData'><EndPoint name='files' type='fs' /></LogicalPort>"));
		l.destroyComponent(new LogicalPortID("osData"));
		l.destroyComponent(new LogicalPortID("usb1"));
		l.destroyComponent(new LogicalPortID("usb2"));
		l.destroyComponent(new LogicalPortID("serial0"));
		l.destroyComponent(new LogicalPortID("eth0"));
		l.destroyComponent(new LogicalPortID("IDU2"));
	}
}
