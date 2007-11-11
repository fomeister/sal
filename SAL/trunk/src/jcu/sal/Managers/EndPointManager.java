/**
 * 
 */
package jcu.sal.Managers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.utils.EndPointModulesList;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
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
	protected EndPoint build(Node config) throws InstantiationException {
		EndPoint endPoint = null;
		this.logger.debug("building EndPoint");
		try {
			String type = this.getComponentType(config);
			EndPointID i = (EndPointID) this.getComponentID(config);
			this.logger.debug("EndPoint type: " +type);
			String className = EndPointModulesList.getClassName(type);
			
			Class<?>[] p = new Class<?>[3];
			p[0] = EndPointID.class;
			p[1] = String.class;
			p[2] = Hashtable.class;
			Constructor<?> c = Class.forName(className).getConstructor(p);
			Object[] o = new Object[3];
			o[0] = i;
			o[1] = type;
			o[2] = getComponentConfig(config);
			endPoint = (EndPoint) c.newInstance(o);

			this.logger.debug("Done building EndPoint " + endPoint.toString());
			
		} catch (RuntimeException e) {
			this.logger.error("Error in new Endpoint configuration. XML doc:");
			this.logger.error(XMLhelper.toString(config));
			e.printStackTrace();
			throw new InstantiationException();
		} catch (ParseException e) {
			this.logger.error("Error while parsing the DOM document. XML doc:");
			this.logger.error(XMLhelper.toString(config));
			e.printStackTrace();
			throw new InstantiationException();
		}catch (Exception e) {
			this.logger.error("Error in new Endpoint instanciation. XML doc:");
			 this.logger.error(XMLhelper.toString(config));
			e.printStackTrace();
			throw new InstantiationException();
		}
		return endPoint;
	}

	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentType(org.w3c.dom.Document)
	 */
	@Override
	protected String getComponentType(Node n) throws ParseException{
		String type = null;
		try {
			type = XMLhelper.getAttributeFromName("//" + EndPoint.ENPOINT_TAG, EndPoint.ENDPOINTTYPE_TAG, n);
		} catch (Exception e) {
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
	protected Identifier getComponentID(Node n) throws ParseException {
		Identifier id = null;
		try {
			id = new EndPointID(XMLhelper.getAttributeFromName("//" + EndPoint.ENPOINT_TAG, EndPoint.ENDPOINTNAME_TAG, n));
		} catch (Exception e) {
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
