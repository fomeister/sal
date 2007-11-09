/**
 * 
 */
package jcu.sal.Managers;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

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
import org.w3c.dom.Node;

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
	protected Protocol build(Node config) throws InstantiationException {
		Protocol p = null;
		Node n = null;
		this.logger.debug("building Protocol");
		try {
			String type = this.getComponentType(config);
			ProtocolID i = (ProtocolID) this.getComponentID(config);
			this.logger.debug("Protocol type: " +type);
			String className = ProtocolModulesList.getClassName(type);

			Class<?>[] params = new Class<?>[4];
			params[0] = ProtocolID.class;
			params[1] = String.class;
			params[2] = Hashtable.class;
			params[3] = Document.class;
			Constructor<?> c = Class.forName(className).getConstructor(params);
			Object[] o = new Object[4];
			o[0] = i;
			o[1] = type;
			o[2] = getComponentConfig(config);
			logger.debug("Protocol config: " + XMLhelper.toString(config));
			n = XMLhelper.getNode("/" + Protocol.PROTOCOL_TAG + "/" + EndPoint.ENPOINT_TAG, config);
			o[3] = XMLhelper.createDocument(n);
			logger.debug("EndPoint config: " + XMLhelper.toString((Document) o[3])); 
			p = (Protocol) c.newInstance(o);
			
			this.logger.debug("done building protocol "+p.toString());
			
		} catch (ParseException e) {
			this.logger.error("Error while parsing the DOM document. XML doc:");
			this.logger.error(XMLhelper.toString(config));
			//e.printStackTrace();
			throw new InstantiationException();
		}catch (Exception e) {
			this.logger.error("Error in new Protocol instanciation. XML doc:");
			 this.logger.error(XMLhelper.toString(config));
			e.printStackTrace();
			throw new InstantiationException();
		}
		return p;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentConfig(org.w3c.dom.Document)
	 */
	@Override
	protected Hashtable<String, String> getComponentConfig(Node n){
		ArrayList<String> xml = null;
		Hashtable<String, String> config = new Hashtable<String, String>();
		String name = null, value = null;
		
		try {
			xml = XMLhelper.getAttributeListFromElements("//" + Protocol.PROTOCOLPARAM_TAG, n);
			Iterator<String> iter = xml.iterator();
			while(iter.hasNext()) {
				iter.next();
				name = iter.next();
				iter.next();
				value = iter.next();
				config.put(name,value);
			}
		} catch (XPathExpressionException e) {
			this.logger.debug("Did not find any parameters for this Protocol");
		}
		
		
		return config;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentType(org.w3c.dom.Document)
	 */
	@Override
	protected String getComponentType(Node n) throws ParseException{
		String type = null;
		try {
			type = XMLhelper.getAttributeFromName("//" + Protocol.PROTOCOL_TAG, Protocol.PROTOCOLTYPE_TAG, n);
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
	protected Identifier getComponentID(Node n) throws ParseException {
		Identifier id = null;
		try {
			id = new ProtocolID(XMLhelper.getAttributeFromName("//" + Protocol.PROTOCOL_TAG, Protocol.PROTOCOLNAME_TAG, n));
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
		component.remove();
	}

	
	public static void main(String[] args)  {
		ProtocolManager e = getProcotolManager();
		Protocol p = null;
		try {
			p = e.createComponent(XMLhelper.createDocument("<Protocol name='1wirefs' type='owfs'><EndPoint name='usb' type='usb' /><parameters><Param name='Location' value='/opt/owfs/bin/owfs' /><Param name='MountPoint' value='/mnt/w1' /></parameters></Protocol>"));
		} catch (ParserConfigurationException ex) {
			System.out.println("Cannot parse Endpoint / PRotocol configuration");
		}
		
		if(p!=null)
			e.destroyComponent(p.getID());
	}
}
