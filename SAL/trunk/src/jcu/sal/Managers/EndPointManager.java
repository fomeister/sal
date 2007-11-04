/**
 * 
 */
package jcu.sal.Managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
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
	
	public static final String ENPOINT_TAG="EndPoint";
	public static final String ENDPOINTTYPE_TAG = "type";
	public static final String ENDPOINTPARAM_TAG = "Param";	
	public static final String ENDPOINTPARAMNAME_TAG = "name";
	private Logger logger = Logger.getLogger(EndPointManager.class);
	
	/**
	 * 
	 */
	public EndPointManager() {
		super();
		Slog.setupLogger(this.logger);
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected EndPoint build(Document doc) throws InstantiationException {
		EndPoint endPoint = null;
		this.logger.debug("building EndPoint");
		try {
			Identifier id = this.getComponentIdentifier(doc);
			String n = id.getName();
			this.logger.debug("Component identifier: " +n);
			String className = EndPointModulesList.getClassName(n);
			
			endPoint = (EndPoint) Class.forName(className).newInstance();
			
			endPoint.setConfig(getComponentConfig(doc));
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
		Hashtable<String,String> xml = null, config = new Hashtable<String,String>();
		
		try {
			xml = XMLhelper.getAttributeListFromElements("//" + ENDPOINTPARAM_TAG, doc);
		} catch (XPathExpressionException e) {
			this.logger.error("Cannot find parameters for this EndPoint");
			throw new ParseException("Cannot find parameters for this EndPoint", 0);
		}
		
		Collection<String> values = xml.values();
		Iterator<String> iter = values.iterator();
		
		while(iter.hasNext())
			config.put(iter.next(), iter.next());
		
		return config;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentIdentifier(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentIdentifier(Document doc) throws ParseException{
		Identifier id = null;
		try {
			id = new EndPointID(XMLhelper.getAttributeFromName("//" + ENPOINT_TAG, ENDPOINTTYPE_TAG, doc));
		} catch (XPathExpressionException e) {
			this.logger.error("Couldnt find the component identifier");
			e.printStackTrace();
			throw new ParseException("Couldnt find the component identifier", 0);
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
		EndPointManager e = new EndPointManager();
		e.createComponent(XMLhelper.createDocument("<EndPoint name='usb' type='usb'><parameters><Param name='portNumber' value='1' /></parameters></EndPoint>"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='usb' type='usb' />"));
		e.createComponent(XMLhelper.createDocument("<EndPoint name='serial1' type='serial'><parameters><Param name='PortNumber' value='1' /></parameters></EndPoint>"));
		e.destroyComponent(new EndPointID("usb"));
	}
}
