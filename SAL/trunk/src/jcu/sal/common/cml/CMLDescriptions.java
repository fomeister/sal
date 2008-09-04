package jcu.sal.common.cml;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This class encapsulate multiple CML description objects (jcu.sal.common.cml.CMLDescription)
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
	 * This constructor creates a new CML descriptions document from a CML descriptions XML document given as a string.
	 * @param cml the CML descriptions XML document 
	 * @throws ConfigurationException if the XML document is not a valid CML document
	 * @throws ParserConfigurationException if the string is not a valid XML document
	 */
	public CMLDescriptions(String cml) throws ConfigurationException, ParserConfigurationException {
		this(XMLhelper.createDocument(cml));
	}
	
	/**
	 * This constructor builds a new CML descriptions object from an XML document
	 * @param cmls the XML CML descriptions document
	 * @throws ConfigurationException if the XML document is not a valid CML document
	 */
	public CMLDescriptions(Document cmls) throws ConfigurationException{
		this.cmls = new Hashtable<Integer,CMLDescription>();
		NodeList n;
		CMLDescription d;
		try {
			n = XMLhelper.getNodeList(CMLConstants.XPATH_CMD_DESC, cmls);
		} catch (XPathExpressionException e) {
			logger.error("Unable to parse the CML descriptions document:");
			e.printStackTrace();
			logger.error(XMLhelper.toString(cmls));
			throw new ConfigurationException("Malformed CML descriptions document");
		}
		for (int i = 0; i < n.getLength(); i++) {
			try {
				d = new CMLDescription(XMLhelper.createDocument(n.item(i)));
				this.cmls.put(d.getCID(), d);
			} catch (ParserConfigurationException e) {
				logger.error("error creating a document from node");
				e.printStackTrace();
				logger.error(XMLhelper.toString(n.item(i)));
				throw new ConfigurationException("individual CML description malformed");
			}
		}
	}
	
	/**
	 * This constructor builds a new CML descriptions object from the given map
	 * @param c the CML despcription objects to be groupped in a CML descriptions object
	 */
	public CMLDescriptions(Map<Integer,CMLDescription> m){
		cmls = new Hashtable<Integer,CMLDescription>(m);
	}
	
	/**
	 * This method returns a list of the command identifier present in this CML descriptions document
	 * @return a list of the command identifier present in this CML descriptions document
	 */
	public List<Integer> getCIDs(){
		return new Vector<Integer>(cmls.keySet());
	}
	
	/**
	 * This method returns a list of individual CML description objects
	 * @return a list of CML description objects
	 */
	public List<CMLDescription> getDescriptions(){
		return new Vector<CMLDescription>(cmls.values());
	}
	
	/**
	 * This method return a CMLDescription object for a given CID
	 * @param cid the command ID
	 * @return the CML description associated with the given CID
	 * @throws ConfigurationException the the CID is not found
	 */
	public CMLDescription getDescription(int cid) throws ConfigurationException{
		if(cmls.containsKey(new Integer(cid)))
			return cmls.get(new Integer(cid));
		throw new ConfigurationException("no such CID");
	}

	/**
	 * This method returns the CML descriptions document as a string
	 * @return the CML descriptions document as a string
	 */
	public String getCMLString(){
		StringBuilder cmlString = new StringBuilder("<"+CMLConstants.CMD_DESCRIPTIONS_TAG+">\n");
		Iterator<CMLDescription> i = cmls.values().iterator();
		while(i.hasNext())
			cmlString.append(i.next().getCMLString());
		cmlString.append("</"+CMLConstants.CMD_DESCRIPTIONS_TAG+">\n");
		return cmlString.toString();
	}
	
	/**
	 * This method returns the CML descriptions document as a DOM document
	 * @return the CML descriptions document as a DOM document
	 * @throws ConfigurationException if the document cant be created
	 */
	public Document getCML() throws ConfigurationException{
		try {
			return XMLhelper.createDocument(getCMLString());
		} catch (ParserConfigurationException e) {
			logger.error("cant create CML descriptions document");
			throw new ConfigurationException();
		}		
	}
}
