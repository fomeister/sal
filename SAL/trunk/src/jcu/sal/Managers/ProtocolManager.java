/**
 * 
 */
package jcu.sal.Managers;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.utils.ProtocolModulesList;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * @author gilles
 * 
 */
public class ProtocolManager extends ManagerFactory<Protocol> {
	
	private static ProtocolManager p = new ProtocolManager();
	private Logger logger = Logger.getLogger(ProtocolManager.class);
	
	
	/**
	 * Private constructor
	 */
	private ProtocolManager() {
		super();
		Slog.setupLogger(this.logger);
	}
	
	/**
	 * Returns the instance of the EndPointManager 
	 * @return
	 */
	public static ProtocolManager getProcotolManager() {
		return p;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected Protocol build(Document doc) throws InstantiationException {
		Protocol p = null;
		this.logger.debug("building Protocol");
		try {
			String type = this.getComponentType(doc);
			ProtocolID i = (ProtocolID) this.getComponentID(doc);
			this.logger.debug("Protocol type: " +type);
			String className = ProtocolModulesList.getClassName(type);

			Class<?>[] params = new Class<?>[3];
			params[0] = ProtocolID.class;
			params[1] = String.class;
			params[2] = Hashtable.class;
			Constructor<?> c = Class.forName(className).getConstructor(params);
			Object[] o = new Object[3];
			o[0] = i;
			o[1] = type;
			o[2] = getComponentConfig(doc);
			p = (Protocol) c.newInstance(o);
			
			this.logger.debug("done building protocol"+p.toString());
			
		} catch (RuntimeException e) {
			this.logger.error("Error in new Protocol configuration. XML doc:");
			this.logger.error(XMLhelper.toString(doc));
			e.printStackTrace();
			throw new InstantiationException();
		} catch (ParseException e) {
			this.logger.error("Error while parsing the DOM document. XML doc:");
			this.logger.error(XMLhelper.toString(doc));
			e.printStackTrace();
			throw new InstantiationException();
		}catch (Exception e) {
			this.logger.error("Error in new Protocol instanciation. XML doc:");
			 this.logger.error(XMLhelper.toString(doc));
			e.printStackTrace();
			throw new InstantiationException();
		}
		return p;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentConfig(org.w3c.dom.Document)
	 */
	@Override
	protected Hashtable<String, String> getComponentConfig(Document doc) throws ParseException {
		ArrayList<String> xml = null;
		Hashtable<String, String> config = new Hashtable<String, String>();
		String name = null, value = null;
		
		try {
			xml = XMLhelper.getAttributeListFromElements("//" + Protocol.PROTOCOLPARAM_TAG, doc);
		} catch (XPathExpressionException e) {
			this.logger.error("Cannot find parameters for this Protocol");
			throw new ParseException("Cannot find parameters for this Protocol", 0);
		}
		
		Iterator<String> iter = xml.iterator();
		
		while(iter.hasNext()) {
			iter.next();
			name = iter.next();
			iter.next();
			value = iter.next();
			config.put(name,value);
		}
		
		return config;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentType(org.w3c.dom.Document)
	 */
	@Override
	protected String getComponentType(Document doc) throws ParseException{
		String type = null;
		try {
			type = XMLhelper.getAttributeFromName("//" + Protocol.PROTOCOL_TAG, Protocol.PROTOCOLTYPE_TAG, doc);
		} catch (XPathExpressionException e) {
			this.logger.error("Couldnt find the protocol type");
			e.printStackTrace();
			throw new ParseException("Couldnt find the protocol type", 0);
		}
		return type;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(Document doc) throws ParseException {
		Identifier id = null;
		try {
			id = new ProtocolID(XMLhelper.getAttributeFromName("//" + Protocol.PROTOCOL_TAG, Protocol.PROTOCOLNAME_TAG, doc));
		} catch (XPathExpressionException e) {
			this.logger.error("Couldnt find the Protocol name");
			e.printStackTrace();
			throw new ParseException("Couldnt create the Protocol identifier", 0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(Protocol component) {
		component.stop();
		component.remove();
	}

	
	public static void main(String[] args)  {
		ProtocolManager e = getProcotolManager();
		Protocol p = null;
		EndPointManager epm = EndPointManager.getEndPointManager();
		EndPoint ep = null;
		try {
			ep = epm.createComponent(XMLhelper.createDocument("<EndPoint name='usb1' type='usb' />"));

			if(ep!=null) {
				p = e.createComponent(XMLhelper.createDocument("<Protocol name='1wirefs' type='owfs'><parameters><Param name='Location' value='/opt/owfs/bin/owfs' /><Param name='MountPoint' value='/mnt/w1' /></parameters></Protocol>"));
				if(p!=null)
					p.setEp(ep);
				else 
					e.destroyComponent(p.getID());
			}
		} catch (ParserConfigurationException ex) {
			System.out.println("Cannot parse Endpoint / PRotocol configuration");
		}  catch (ConfigurationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			System.out.println("Cannot configure PRotocol");
		}
		if(p!=null)
			e.destroyComponent(p.getID());
		if(ep!=null)
			epm.destroyComponent(ep.getID());

		/*e.createComponent(XMLhelper.createDocument("<EndPoint name='usb2' type='usb' />"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='serial0' type='serial'><parameters><Param name='PortSpeed' value='9600' /><Param name='DataBits' value='8' /><Param name='Parity' value='0' /><Param name='StopBit' value='1' /><Param name='PortDeviceFile' value='/dev/ttyS0' /></parameters></EndPoint>"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='serial0' type='serial'><parameters><Param name='PortSpeed' value='9600' /><Param name='DataBits' value='8' /><Param name='Parity' value='0' /><Param name='StopBit' value='1' /><Param name='PortDeviceFile' value='/dev/ttyS0' /></parameters></EndPoint>"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='eth0' type='ethernet'><parameters><Param name='EthernetDevice' value='eth0' /><Param name='IPAddress' value='' /></parameters></EndPoint>"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='files' type='fs' />"));
		
		e.destroyComponent(new ProtocolID("eth01"));
		e.destroyComponent(new ProtocolID("usb1"));
		e.destroyComponent(new EndPointID("usb2"));
		e.destroyComponent(new EndPointID("serial0"));
		e.destroyComponent(new EndPointID("eth0"));
		e.destroyComponent(new EndPointID("files"));*/
	}
}
