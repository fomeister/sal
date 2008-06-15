package jcu.sal.common.cml;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This class encapsulate a CML descriptions document. This type of docuement contains of one or more CML descriptions. 
 * @author gilles
 *
 */
public class CMLDescriptions {
	private static Logger logger = Logger.getLogger(CMLDescriptions.class);
	static {
		Slog.setupLogger(logger);
	}
	private Map<Integer,CMLDescription> cmls;
	
	/**
	 * This constructor creates a deep-copy of the existing CML descriptions object <code>c</code>.
	 * @param c the CML despcriptions document to copy
	 */
	public CMLDescriptions(CMLDescriptions c){
		cmls = c.getDescriptions();
	}
	
	/**
	 * This constructor builds a new CML descriptions object from the given map
	 * @param c the CML despcription objects to be groupped in a CML descriptions object
	 */
	public CMLDescriptions(Map<Integer,CMLDescription> m){
		cmls = new Hashtable<Integer,CMLDescription>(m);
	}
	
	/**
	 * This constructor builds a new CML descriptions object from an XML document
	 * @param cmls the XML CML descriptions document
	 * @throws ConfigurationException if the document isnt properly formatted
	 */
	public CMLDescriptions(Document cmls) throws ConfigurationException{
		this.cmls = new Hashtable<Integer,CMLDescription>();
		NodeList n;
		CMLDescription d;
		try {
			n = XMLhelper.getNodeList(CMLConstants.XPATH_CMD_DESC, cmls);
		} catch (XPathExpressionException e) {
			logger.error("Unable to parse the CML descriptions document:");
			logger.error(XMLhelper.toString(cmls));
			throw new ConfigurationException();
		}
		for (int i = 0; i < n.getLength(); i++) {
			try {
				d = new CMLDescription(XMLhelper.createDocument(n.item(i)));
				this.cmls.put(d.getCID(), d);
			} catch (ParserConfigurationException e) {
				logger.error("error creating a document from node "+n.item(i));
				throw new ConfigurationException();
			}
		}
	}
		
	/**
	 * This method returns a copy of the map of cids and associated CML description documents
	 * @return a copy of the map of cids and associated CML description documents
	 */
	public synchronized Map<Integer,CMLDescription> getDescriptions(){
		return new Hashtable<Integer,CMLDescription>(cmls);
	}
	
	/**
	 * This method return a CMLDescription object for a given CID
	 * @param cid the command ID
	 * @return the CML description associated with the given CID
	 * @throws ConfigurationException the the CID is not found
	 */
	public synchronized  CMLDescription getDescription(int cid) throws ConfigurationException{
		if(cmls.containsKey(new Integer(cid)))
			return cmls.get(new Integer(cid));
		throw new ConfigurationException();
	}

	/**
	 * This method returns the CML descriptions document as a string
	 * @return the CML descriptions document as a string
	 */
	public synchronized String getCMLString(){
		StringBuilder cmlString = new StringBuilder("<commandDescriptions>\n");
		Iterator<CMLDescription> i = cmls.values().iterator();
		while(i.hasNext())
			cmlString.append(i.next().getCMLString());
		cmlString.append("</commandDescriptions>\n");
		return cmlString.toString();
	}
	
	/**
	 * This method returns the CML descriptions document as a DOM document
	 * @return the CML descriptions document as a DOM document
	 * @throws ConfigurationException if the document cant be created
	 */
	public synchronized Document getCML() throws ConfigurationException{
		try {
			return XMLhelper.createDocument(getCMLString());
		} catch (ParserConfigurationException e) {
			logger.error("cant create CML descriptions document");
			throw new ConfigurationException();
		}		
	}
}
