package jcu.sal.common.cml;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
				if(this.cmls.put(d.getCID(), d)!=null) {
					logger.error("The CML descriptions document contains command with the same name");
					throw new ConfigurationException("2 or more individual CML descriptions share the same name '"+d.getCID()+"'");
				}
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
	 * This method returns a set of the command identifier present in this CML descriptions document
	 * @return a set of the command identifier present in this CML descriptions document
	 */
	public Set<Integer> getCIDs(){
		return new HashSet<Integer>(cmls.keySet());
	}
	
	/**
	 * This method returns a set of individual CML description objects
	 * @return a set of CML description objects
	 */
	public Set<CMLDescription> getDescriptions(){
		return new HashSet<CMLDescription>(cmls.values());
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

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((cmls == null) ? 0 : cmls.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CMLDescriptions other = (CMLDescriptions) obj;
		if (cmls == null) {
			if (other.cmls != null)
				return false;
		} else if (!cmls.equals(other.cmls))
			return false;
		return true;
	}
}
