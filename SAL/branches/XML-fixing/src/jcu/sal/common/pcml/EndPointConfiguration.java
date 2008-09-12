package jcu.sal.common.pcml;

import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.common.exceptions.ParserException;
import jcu.sal.components.HWComponentConfiguration;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class encapsulate an EndPoint's configuration
 * @author gilles
 *
 */
public class EndPointConfiguration implements HWComponentConfiguration{
	private static Logger logger = Logger.getLogger(EndPointConfiguration.class);
	static { Slog.setupLogger(logger); }
	
	private static String XPATH_ENDPOINT = "/" + PCMLConstants.ENDPOINT_NODE;
	
	private String name;
	private String type;
	private Parameters params;
	
	/**
	 * This constructor creates a configuration object for an EndPoint with an empty parameter list
	 * @param n the name of the EndPoint
	 * @param t the type of the EndPoint
	 */
	public EndPointConfiguration(String n, String t){
		name = n;
		type = t;
		params = new Parameters();
	}

	/**
	 * This constructor creates a configuration object for an EndPoint.
	 * @param n the name of the EndPoint
	 * @param t the type of the EndPoint
	 * @param p the parameters object associated with this EndPoint
	 */
	public EndPointConfiguration(String n, String t, Parameters p){
		name = n;
		type = t;
		params = p;
	}
	
	/**
	 * This constructor creates a new configuration object for an EndPoint from an XML document.<br>
	 * Example of valid XML configuration:<br>
	 * <pre>
	 * <EndPoint name="eth0" type="ethernet">
	 *   <parameters>
	 *    <Param name="EthernetDevice" value="eth0" />
	 * 	</parameters>
	 * </EndPoint>
	 * </pre>
	 * @param d the XML document representing the EndPoint configuration
	 * @throws ConfigurationException if the XML document isnt a valid EndPoint configuration document
	 */
	public EndPointConfiguration(Document d) throws ConfigurationException{
		checkDocument(d);
		parseDocumentNameType(d);
		parseParameters(d);
	}
	
	/**
	 * This method is identical to <code>EndPointConfiguration(Document d)</code> except that the document
	 * is passed as a string.
	 * @param xml the EndPoint configuration document
	 * @throws ConfigurationException if the EndPoint configuration document isnt valid
	 * @throws ParserException if the supplied document isnt a valid XML document
	 */
	public EndPointConfiguration(String xml) throws ConfigurationException, ParserException{
		this(XMLhelper.createDocument(xml));
	}
	
	/**
	 * This method checks the supplied XML document to see if it contains only one EndPoint configuration section
	 * @param d the XML document to be checked
	 * @throws ConfigurationException if the document isnt a valid EndPoint configuration document
	 */
	private void checkDocument(Document d) throws ConfigurationException{
		int nb;
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_ENDPOINT+")", d));
			if(nb!=1){
				logger.error("There is more than one EndPoint configuration section (found "+nb+") in this document");
				logger.error(XMLhelper.toString(d));
				throw new ConfigurationException();
			}
		} catch (Throwable t) {
			logger.error("Cant check how many EndPoint configuration sections are in this document");
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method parses the supplied EndPoint configuration document and exctracts the name and type
	 * @param d the XML document
	 * @throws ConfigurationException if either the name or the type of the EndPoint cant be found
	 */
	private void parseDocumentNameType(Document d) throws ConfigurationException{
		try {
			name = XMLhelper.getAttributeFromName(XPATH_ENDPOINT, PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE, d);
		} catch (Exception e) {
			logger.error("Cant find attr '"+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"' in EP config XML doc");
			throw new ConfigurationException();
		}
		
		try {
			type = XMLhelper.getAttributeFromName(XPATH_ENDPOINT, PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE, d);
		} catch (Exception e) {
			logger.error("Cant find attr '"+PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"' in EP config XML doc");
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method parses the supplied EndPoint configuartion document and extracts the parameters
	 * @param d the XML document
	 * @throws ConfigurationException if the parameters cant be found
	 */
	private void parseParameters(Document d) throws ConfigurationException{
		params = new Parameters(d);
	}
	
	/**
	 * This method returns the name of the EndPoint
	 * @return the name of the EndPoint
	 */
	public String getID(){
		return name;
	}
	
	/**
	 * This method returns the type of the EndPoint
	 * @return the type of the EndPoint
	 */
	public String getType(){
		return type;
	}
	
	/**
	 * This method returns the parameters object of the EndPoint
	 * @return the parameters object of the EndPoint
	 */
	public Parameters getParameters(){
		return params;
	}
	
	public String getXMLString(){
		StringBuffer sb = new StringBuffer();
		sb.append("<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=");
		sb.append("\""+name+"\" "+PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\""+type+"\">\n");
		sb.append(params.getXMLString());
		sb.append("</"+PCMLConstants.ENDPOINT_NODE+">\n");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((params == null) ? 0 : params.hashCode());
		result = PRIME * result + ((type == null) ? 0 : type.hashCode());
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
		final EndPointConfiguration other = (EndPointConfiguration) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}	
}
