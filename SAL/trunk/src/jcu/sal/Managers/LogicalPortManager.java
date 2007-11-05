/**
 * 
 */
package jcu.sal.Managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.Components.Identifiers.LogicalPortID;
import jcu.sal.Components.LogicalPorts.LogicalPort;
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
		Document d = null;

		this.logger.debug("building LogicalPort");
		try {
			Identifier i = this.getComponentID(doc);
			this.logger.debug("LogicalPort name " + i.getName());
			//Create the Endpoint
			Node n = XMLhelper.getNode("/" + LOGICALPORT_TAG + "/" + EndPoint.ENPOINT_TAG, doc);
			d = XMLhelper.createDocument(n);
			XMLhelper.toString(d);
			EndPoint e = EndPointManager.getEndPointManager().createComponent(d);
			
			//Create the Protocol here and pass it to lp
			//TODO 
			
			if(e!=null) { // TODO && Protocol != null
				lport = new LogicalPort(e);
				lport.setID(i);
				lport.setType(getComponentType(d));
			} else
				throw new InstantiationException("Couldnt create the Endpoint/Protocol and logical Port");
			
		} catch (ParseException e) {
			this.logger.error("Cant get the LogicalPort's name");
			e.printStackTrace();
			throw new InstantiationException("Cant create the logical port");
		} catch (XPathExpressionException e) {
			this.logger.error("Error in new LogicalPort's EndPoint/Protocol XML configuration");
			e.printStackTrace();
		} catch (TransformerException e) {
			this.logger.error("Error in new LogicalPort's EndPoint/Protocol XML configuration");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			this.logger.error("Cant create an empty DOM document");
			e.printStackTrace();
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
			id = new EndPointID(XMLhelper.getAttributeFromName("//" + LOGICALPORT_TAG, LOGICALPORTNAME_TAG, doc));
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
		l.destroyComponent(new LogicalPortID("IDU"));
	}
}
