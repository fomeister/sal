package jcu.sal.common.sml;

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

public class SMLDescriptions {
	private static Logger logger = Logger.getLogger(SMLDescriptions.class);
	static {
		Slog.setupLogger(logger);
	}
	
	private Map<Integer, SMLDescription> smls;
	
	private static String XPATH_SENSORS_DESC = "/"+SMLConstants.SENSOR_CONF_NODE+"/"+SMLConstants.SENSOR_TAG;
	
	/**
	 * This constructor create an SMLDescriptions object from multiple SML description objects
	 * @param sml a map of sensor IDs and associated SML description objects
	 */
	public SMLDescriptions(Map<Integer, SMLDescription> m){
		smls = new Hashtable<Integer, SMLDescription>(m);
	}
	
	/**
	 * This constructor create an SMLDescriptions object from an SML descriptions XML document given as a string.
	 * @param sml an SML descriptions XML document
	 * @throws ConfigurationException if the XML document is not a valid SML document
	 * @throws ParserConfigurationException if the string is not a valid XML document
	 */
	public SMLDescriptions(String sml) throws ConfigurationException, ParserConfigurationException{
		this(XMLhelper.createDocument(sml));
	}
	
	/**
	 * This constructor creates an SMLDescriptions object from an SML descriptions XML document.
	 * @param d the SML descriptions XML document
	 * @throws ConfigurationException if the document cant be parsed / is malformed
	 */
	public SMLDescriptions(Document d) throws ConfigurationException {
		smls = new Hashtable<Integer,SMLDescription>();
		NodeList n;
		SMLDescription s;
		try {
			n = XMLhelper.getNodeList(XPATH_SENSORS_DESC, d);
		} catch (XPathExpressionException e) {
			logger.error("Unable to parse the SML descriptions document:");
			e.printStackTrace();
			logger.error(XMLhelper.toString(d));
			throw new ConfigurationException("Malformed SML descriptions document");
		}
		for (int i = 0; i < n.getLength(); i++) {
			try {
				s = new SMLDescription(XMLhelper.createDocument(n.item(i)));
				smls.put(s.getSID(), s);
			} catch (ParserConfigurationException e) {
				logger.error("error creating a document from node");
				e.printStackTrace();
				logger.error(XMLhelper.toString(n.item(i)));
				throw new ConfigurationException("individual SML description malformed");
			}
		}
	}
	
	/**
	 * This methods returns a list of sensor IDs present in this SML descriptions object
	 * @return a list of sensor IDs present in this SML descriptions object
	 */
	public List<Integer> getSIDs(){
		return new Vector<Integer>(smls.keySet());
	}
	
	/**
	 * This method returns a list of individual SML description objects
	 * @return a list of SML description objects
	 */
	public List<SMLDescription> getDescriptions(){
		return new Vector<SMLDescription>(smls.values());
	}
	
	/**
	 * This method returns an SMLDescription object for a single sensor, given it sensor ID
	 * @param sid the sensor ID
	 * @return an SMLDescription object
	 * @throws ConfigurationException if the sensor ID cant be found
	 */
	public SMLDescription getDescription(int sid) throws ConfigurationException {
		if(!smls.containsKey(sid))
			throw new ConfigurationException("no such sensor ID");
		return smls.get(sid);
	}
	
	/**
	 * This method returns the XML document associated with this SMLDescriptions object as a string
	 * @return the XML document associated with this object
	 */
	public String getSMLString() {
		StringBuffer sb = new StringBuffer();
		Iterator<SMLDescription> i = smls.values().iterator();
		
		sb.append("<"+SMLConstants.SENSOR_CONF_NODE+">");
		while(i.hasNext())
			sb.append(i.next().getSMLString());
		sb.append("</"+SMLConstants.SENSOR_CONF_NODE+">");
		
		return sb.toString();
	}
	
	/**
	 * This method returns the XML document associated with this SMLDescriptions object as a DOM document
	 * @return the XML document associated with this SMLDescriptions object 
	 */
	public Document getSML() {
		try {
			return XMLhelper.createDocument(getSMLString());
		} catch (ParserConfigurationException e) {
			logger.error("error creating XML SML doc");
		}
		return null;
	}
	
	
	/**
	 * This method returns an empty SML descriptions document
	 * @return an empty SML descriptions document
	 */
	public static Document createEmptySML(){
	try {
			return XMLhelper.createDocument("<"+SMLConstants.SENSOR_CONF_NODE+" />\n");
		} catch (ParserConfigurationException e) {
			logger.error("We shouldnt be here - cant create CML descriptions document");
		}
		return null;
	}
}
