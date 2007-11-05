/**
 * 
 */
package jcu.sal.Managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.utils.EndPointModulesList;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author gilles
 * 
 */
public class EndPointManager extends ManagerFactory<EndPoint> {
	
	private static EndPointManager e = new EndPointManager();
	private Logger logger = Logger.getLogger(EndPointManager.class);
	
	
	/**
	 * Private constructor
	 */
	private EndPointManager() {
		super();
		Slog.setupLogger(this.logger);
	}
	
	/**
	 * Returns the instance of the EndPointManager 
	 * @return
	 */
	public static EndPointManager getEndPointManager() {
		return e;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected EndPoint build(Document doc) throws InstantiationException {
		EndPoint endPoint = null;
		this.logger.debug("building EndPoint");
		try {
			String type = this.getComponentType(doc);
			Identifier i = this.getComponentID(doc);
			this.logger.debug("Component type: " +type);
			String className = EndPointModulesList.getClassName(type);

			endPoint = (EndPoint) Class.forName(className).newInstance();
			endPoint.setConfig(getComponentConfig(doc));
			endPoint.setID(i);
			endPoint.setType(type);
			
		} catch (ParseException e) {
			this.logger.error("Error while parsing the DOM document");
			e.printStackTrace();
		} catch (InstantiationException e) {
			this.logger.error("Error in new Endpoint instanciation");
			e.printStackTrace();
			throw e;
		} catch (IllegalAccessException e) {
			this.logger.error("Error in new Endpoint instanciation");
			e.printStackTrace();
			throw new InstantiationException();
		} catch (ClassNotFoundException e) {
			this.logger.error("Error in new Endpoint instanciation");
			e.printStackTrace();
			throw new InstantiationException();
		} catch (RuntimeException e) {
			this.logger.error("Error in new Endpoint configuration");
			e.printStackTrace();
			throw new InstantiationException();
		}
	
		return endPoint;
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
			xml = XMLhelper.getAttributeListFromElements("//" + EndPoint.ENDPOINTPARAM_TAG, doc);
		} catch (XPathExpressionException e) {
			this.logger.error("Cannot find parameters for this EndPoint");
			throw new ParseException("Cannot find parameters for this EndPoint", 0);
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
			type = XMLhelper.getAttributeFromName("//" + EndPoint.ENPOINT_TAG, EndPoint.ENDPOINTTYPE_TAG, doc);
		} catch (XPathExpressionException e) {
			this.logger.error("Couldnt find the EndPoint type");
			e.printStackTrace();
			throw new ParseException("Couldnt find the EndPoint type", 0);
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
			id = new EndPointID(XMLhelper.getAttributeFromName("//" + EndPoint.ENPOINT_TAG, EndPoint.ENDPOINTNAME_TAG, doc));
		} catch (XPathExpressionException e) {
			this.logger.error("Couldnt find the EndPoint name");
			e.printStackTrace();
			throw new ParseException("Couldnt create the EndPoint identifier", 0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(EndPoint component) {
		component.stop();
		component.remove();
	}

	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, BadAttributeValueExpException {
		EndPointManager e = getEndPointManager();
		e.createComponent(XMLhelper.createDocument("<EndPoint name='usb1' type='usb'><parameters><Param name='portNumber' value='1' /></parameters></EndPoint>"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='usb2' type='usb' />"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='serial0' type='serial'><parameters><Param name='PortSpeed' value='9600' /><Param name='DataBits' value='8' /><Param name='Parity' value='0' /><Param name='StopBit' value='1' /><Param name='PortDeviceFile' value='/dev/ttyS0' /></parameters></EndPoint>"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='serial0' type='serial'><parameters><Param name='PortSpeed' value='9600' /><Param name='DataBits' value='8' /><Param name='Parity' value='0' /><Param name='StopBit' value='1' /><Param name='PortDeviceFile' value='/dev/ttyS0' /></parameters></EndPoint>"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='eth0' type='ethernet'><parameters><Param name='EthernetDevice' value='eth0' /><Param name='IPAddress' value='' /></parameters></EndPoint>"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='files' type='fs' />"));
		e.destroyComponent(new EndPointID("eth01"));
		e.destroyComponent(new EndPointID("usb1"));
		e.destroyComponent(new EndPointID("usb2"));
		e.destroyComponent(new EndPointID("serial0"));
		e.destroyComponent(new EndPointID("eth0"));
		e.destroyComponent(new EndPointID("files"));
	}
}
