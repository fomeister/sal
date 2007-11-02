/**
 * 
 */
package jcu.sal.Managers;

import java.text.ParseException;

import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * @author gilles
 * 
 */
public class EndPointManager extends ManagerFactory {
	
	public static final String ENPOINT_TAG="EndPoint";
	public static final String ENDPOINTNAME_TAG = "name";
	
	static Logger logger = Logger.getLogger(EndPointManager.class);
	
	/**
	 * 
	 */
	public EndPointManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected Object build(Document doc) {

		return null;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#componentIdentifier(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier componentIdentifier(Document doc) throws ParseException{
		Identifier id = null;
		try {
			id = new EndPointID(XMLhelper.getNode("//" + ENPOINT_TAG, doc).getAttributes()
					.getNamedItem(ENDPOINTNAME_TAG).getNodeValue());
		} catch (XPathExpressionException e) {
			logger.error("Couldnt find the component identifier");
			e.printStackTrace();
			throw new ParseException("Couldnt find the component identifier", 0);
		}
		return id;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(Object component) {


	}

}
