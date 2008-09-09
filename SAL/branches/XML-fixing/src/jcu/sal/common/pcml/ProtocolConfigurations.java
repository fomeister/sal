package jcu.sal.common.pcml;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
 * Objects of this class are used to group configuration information about a platform. They convey the same
 * information as PCML documents.
 * @author gilles
 *
 */
public class ProtocolConfigurations {
	private static Logger logger = Logger.getLogger(ProtocolConfigurations.class);
	static {
		Slog.setupLogger(logger);
	}
	
	private HashSet<ProtocolConfiguration> configs;
	
	private static String XPATH_CONFIG = "/"+PCMLConstants.PLATFORM_CONFIGURATION_NODE;
	private static String XPATH_PROTOCOL_DESC = XPATH_CONFIG+"/"+PCMLConstants.PROTOCOL_NODE;
	
	private ProtocolConfigurations(){
		configs = new HashSet<ProtocolConfiguration>();
	}
	
	/**
	 * This constructor creates a platform configuration object from individual protocol configuration objects
	 * @param c a collection of protocol configuration objects
	 * @throws ConfigurationException if two or more protocol configuration object have the same ID
	 */
	public ProtocolConfigurations(Collection<ProtocolConfiguration> c) throws ConfigurationException {
		this();
		Iterator<ProtocolConfiguration> i = c.iterator();
		while(i.hasNext())
			addProtocolConfiguration(i.next());
	}
	
	/**
	 * This constructor creates a platform configuration object from individual protocol configuration objects
	 * @param c a set of protocol configuration objects
	 */
	public ProtocolConfigurations(Set<ProtocolConfiguration> c) {
		configs = new HashSet<ProtocolConfiguration>(c);
	}
	
	/**
	 * This constructor creates a platform configuration object from a valid PCML document
	 * @param d a valid PCML document
	 * @throws ConfigurationException if the supplied document is not a valid PCML document
	 */
	public ProtocolConfigurations(Document d) throws ConfigurationException{
		this();
		checkDocument(d);
		parseDocument(d);
	}
	
	/**
	 * This method checks the given XML document to see if it is a valid PCML document
	 * @param d the document to be checked
	 * @throws ConfigurationException if the check fails
	 */
	private void checkDocument(Document d) throws ConfigurationException{
		int nb;
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_CONFIG+")", d));
		} catch (Throwable t) {
			logger.error("Cant check how many platform config section are in this document");
			throw new ConfigurationException("Malformed PCML document");
		}
		if(nb!=1){
			logger.error("Found "+nb+" platform config section in this document, there should be 1");
			logger.error(XMLhelper.toString(d));
			throw new ConfigurationException("Malformed PCML document");
		}
	}
	
	/**
	 * This method parses the given PCML document and exctracts the individual protocol configuration section
	 * and creates the matching protocol configuration objects
	 * @param d the PCML document
	 * @throws ConfigurationException if the PCML document is malformed
	 */
	private void parseDocument(Document d) throws ConfigurationException{
		NodeList n;
		try {
			n = XMLhelper.getNodeList(XPATH_PROTOCOL_DESC, d);
		} catch (XPathExpressionException e) {
			logger.error("Unable to parse the platform config document:");
			e.printStackTrace();
			logger.error(XMLhelper.toString(d));
			throw new ConfigurationException("Malformed platform config document");
		}
		for (int i = 0; i < n.getLength(); i++) {
			try {
				addProtocolConfiguration(new ProtocolConfiguration(XMLhelper.createDocument(n.item(i))));
			} catch (ParserConfigurationException e) {
				logger.error("error creating a document from node");
				e.printStackTrace();
				logger.error(XMLhelper.toString(n.item(i)));
				throw new ConfigurationException("individual protocol configuration section malformed");
			}
		}
	}
	
	/**
	 * This method adds a protocol configuration to this object 
	 * @param p the protocol configuration to be added
	 * @throw ConfigurationException if the element already exists
	 */
	private void addProtocolConfiguration(ProtocolConfiguration p) throws ConfigurationException {
		if(!configs.add(p)){
			logger.error("The platform configuration document contains protocols with the same name '"+p.getID()+"'");
			throw new ConfigurationException("2 or more individual protocols share the same name '"+p.getID()+"'");
		}
	}
	
	/**
	 * This constructor is identical to <code>ProtocolConfigurations(Document)</code> except that the XML document
	 * is passed as a String.
	 * @param xml a valid PCML platform configuration document
	 * @throws ConfigurationException if the XML document is not a valid PCML document
	 * @throws ParserConfigurationException if the supplied string isnt a valid XML document
	 */
	public ProtocolConfigurations(String xml) throws ConfigurationException, ParserConfigurationException{
		this(XMLhelper.createDocument(xml));
	}
	
	/**
	 * This methods returns a set of protocol IDs present in this platform configuration object
	 * @return a set of protocol IDs present in this platform configuration object
	 */
	public Set<String> getPIDs(){
		HashSet<String> h = new HashSet<String>();
		Iterator<ProtocolConfiguration> iter = configs.iterator();
		while(iter.hasNext())
			h.add(iter.next().getID());
		
		return h;
	}
	
	/**
	 * This method returns a set of individual protocol configuration objects
	 * @return a set of protocol configuration objects
	 */
	public Set<ProtocolConfiguration> getConfigurations(){
		return new HashSet<ProtocolConfiguration>(configs);
	}
	
	/**
	 * This method returns an protocol configuration object, given its protocol ID
	 * @param pid the protcol ID
	 * @return a ProtocolConfiguration object
	 * @throws ConfigurationException if the protocol ID cant be found
	 */
	public ProtocolConfiguration getDescription(String pid) throws ConfigurationException {
		ProtocolConfiguration s=null;
		Iterator<ProtocolConfiguration> iter = configs.iterator();
		while(iter.hasNext()) {
			s = iter.next();
			if(s.getID().equals(pid))
				return s;
		}
			
		throw new ConfigurationException("no such protocol ID");
	}
	
	/**
	 * This method returns the number of Protocol configuration objects in this objects
	 * @return number of Protocol configuration objects in this objects
	 */
	public int getSize(){
		return configs.size();
	}
	
	/**
	 * This method returns the PCML document associated with this platform config object as a string
	 * @return the PCML document associated with this object
	 */
	public String getXMLString() {
		StringBuffer sb = new StringBuffer();
		Iterator<ProtocolConfiguration> i = configs.iterator();
		
		sb.append("<"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">\n");
		while(i.hasNext())
			sb.append(i.next().getXMLString());
		sb.append("</"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">\n");
		
		return sb.toString();
	}
	
	/**
	 * This method returns the XML document associated with this platform config object as a DOM document
	 * @return the XML document associated with this platform config object 
	 */
	public Document getXML() {
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			logger.error("We shouldnt be here - error creating PCML doc");
		}
		return null;
	}
	
	
	/**
	 * This method returns an empty SML descriptions document
	 * @return an empty SML descriptions document
	 */
	public static Document createEmptyXML(){
	try {
			return XMLhelper.createDocument("<"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+" />\n");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			logger.error("We shouldnt be here - cant create PCML document");
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((configs == null) ? 0 : configs.hashCode());
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
		final ProtocolConfigurations other = (ProtocolConfigurations) obj;
		if (configs == null) {
			if (other.configs != null)
				return false;
		} else if (!configs.equals(other.configs))
			return false;
		return true;
	}
}
