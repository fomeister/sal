package jcu.sal.common.cml;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.naming.ConfigurationException;

import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.ParserException;
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
	static {Slog.setupLogger(logger);}
	
	private Set<CMLDescription> cmls;
	
	private CMLDescriptions(){
		cmls = new HashSet<CMLDescription>();	
	}
	
	/**
	 * This constructor creates a new CML descriptions document from a CML descriptions XML document given as a string.
	 * @param cml the CML descriptions XML document 
	 * @throws ConfigurationException if the XML document is not a valid CML document
	 * @throws ParserException if the string is not a valid XML document
	 */
	public CMLDescriptions(String cml) throws ConfigurationException, ParserException {
		this(XMLhelper.createDocument(cml));
	}
	
	/**
	 * This constructor builds a new CML descriptions object from an XML document
	 * @param c the XML CML descriptions document
	 * @throws ConfigurationException if the XML document is not a valid CML document
	 */
	public CMLDescriptions(Document c) throws ConfigurationException{
		this();
		NodeList n;
		try {
			n = XMLhelper.getNodeList(CMLConstants.XPATH_CMD_DESC, c);
		} catch (NotFoundException e) {
			logger.error("No commands were found in the CML descriptions document:");
			logger.error(XMLhelper.toString(c));
			e.printStackTrace();
			throw new ConfigurationException("Malformed CML descriptions document");
		}
		for (int i = 0; i < n.getLength(); i++)
			addCMLDescription(new CMLDescription(XMLhelper.createDocument(n.item(i))));
	}
	
	/**
	 * This constructor builds a new CML descriptions object from the given set
	 * @param c a set of CML despcription objects to be groupped in a CML descriptions object
	 */
	public CMLDescriptions(Set<CMLDescription> c){
		cmls = new HashSet<CMLDescription>(c);
	}
	
	/**
	 * This constructor builds a new CML descriptions object from the given set
	 * @param c a set of CML despcription objects to be groupped in a CML descriptions object
	 * @throws ConfigurationException if the collection contains duplicate CML descriptions
	 */
	public CMLDescriptions(Collection<CMLDescription> c) throws ConfigurationException{
		this();
		for(CMLDescription cd: c)
			addCMLDescription(cd);		
	}
	
	/**
	 * This method adds a CMLdescription object to this object
	 * @param c the CML description to be added
	 * @throws ConfigurationException if this object already contains the supplied CML description
	 */
	private void addCMLDescription(CMLDescription c) throws ConfigurationException {
		if(!cmls.add(c)) {
			logger.error("The CML descriptions document contains command with the same name");
			throw new ConfigurationException("2 or more individual CML descriptions share the same name '"+c.getCID()+"'");
		}
	}
	
	/**
	 * This method returns a set of the command identifier present in this CML descriptions document
	 * @return a set of the command identifier present in this CML descriptions document
	 */
	public Set<Integer> getCIDs(){
		HashSet<Integer> h = new HashSet<Integer>();
		
		for(CMLDescription c: cmls)
			h.add(c.getCID());
		
		return h;
	}
	
	/**
	 * This method returns a set of individual CML description objects
	 * @return a set of CML description objects
	 */
	public Set<CMLDescription> getDescriptions(){
		return new HashSet<CMLDescription>(cmls);
	}
	
	/**
	 * This method return a CMLDescription object for a given CID
	 * @param cid the command ID
	 * @return the CML description associated with the given CID
	 * @throws ConfigurationException the the CID is not found
	 */
	public CMLDescription getDescription(int cid) throws ConfigurationException{
		for(CMLDescription c: cmls)
			if(c.getCID().intValue()==cid)
				return c;
		
		throw new ConfigurationException("no such CID "+cid);
	}

	/**
	 * This method returns the CML descriptions document as a string
	 * @return the CML descriptions document as a string
	 */
	public String getXMLString(){
		StringBuilder cmlString = new StringBuilder("<"+CMLConstants.CMD_DESCRIPTIONS_TAG+">\n");
		
		for(CMLDescription c: cmls)
			cmlString.append(c.getXMLString());
		
		cmlString.append("</"+CMLConstants.CMD_DESCRIPTIONS_TAG+">\n");
		return cmlString.toString();
	}
	
	/**
	 * This method returns the CML descriptions document as a DOM document
	 * @return the CML descriptions document as a DOM document
	 * @throws ConfigurationException if the document cant be created
	 */
	public Document getXML() throws ConfigurationException{
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (ParserException e) {
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
