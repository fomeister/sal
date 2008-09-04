package jcu.sal.common.sml;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class encapsulates an SML sensor description XML document. It provides information about a single SAL-managed sensor.
 * @author gilles
 *
 */
public class SMLDescription {
	private static Logger logger = Logger.getLogger(SMLDescription.class);
	static {
		Slog.setupLogger(logger);
	}

	private Integer sid;
	private Map<String,String> parameters;
	
	private static String XPATH_SENSOR_DESC="/"+SMLConstants.SENSOR_TAG;
	private static String XPATH_SENSOR_PARAMETER=XPATH_SENSOR_DESC+"/"+SMLConstants.PARAMETERS_NODE+"/"+SMLConstants.PARAMETER_NODE;
	
	/**
	 * This method constructs a new SML description object given a sensor ID and a table of key,value pairs representing
	 * the sensor parameters. These parameters will be checked to make sure all required parameters are there.
	 * @param s the sensor ID
	 * @param p the parameter list
	 * @throws ConfigurationException if either arguments are invalid
	 */
	public SMLDescription(Integer s, Map<String,String> p) throws ConfigurationException {
		Set<String> l;
		String tmp;
		
		//Check the args
		if(s.intValue()<0 || s.intValue()>SMLConstants.SENSOR_ID_MAX || p.size() != SMLConstants.NB_PARAMETERS)
			throw new ConfigurationException("Invalid sensor ID or parameter list");
		sid = new Integer(s);
		
		//check the param list
		Iterator<String> iter = SMLConstants.PARAM_NAMES.iterator();
		l = p.keySet();
		while(iter.hasNext()) {
			tmp = iter.next();
			if(!l.contains(tmp)) throw new ConfigurationException("Couldnt find required parameter '"+tmp+"'");
		}
		parameters = new Hashtable<String, String>(p);
	}
	
	public SMLDescription(Document doc) throws ConfigurationException {
		checkDocument(doc);
		parseSensorID(doc);
		parameters = new Hashtable<String, String>();
		parseParameters(doc);		
	}
	
	/**
	 * This method checks that the given document is a valid SML description document.
	 * It checks that there is only one SMLDescription instance, and that the required number of parameters are present.
	 * @param d the SML description document to be validated
	 * @throws ConfigurationException if the document is not a valid SMLdescription
	 */
	private void checkDocument(Document d) throws ConfigurationException {
		int nb;
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_SENSOR_DESC+")", d));
			if(nb!=1){
				logger.error("There is more than one SML description (found "+nb+") in this document");
				logger.error(XMLhelper.toString(d));
				throw new ConfigurationException();
			}
		} catch (Throwable t) {
			logger.error("Cant check how many SML descriptions are in this document");
			throw new ConfigurationException();
		}
		
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_SENSOR_PARAMETER+")", d));
			if(nb!=SMLConstants.NB_PARAMETERS){
				logger.error("The number of parameters found in this document ("+nb+") is different from the required number ("+
							SMLConstants.NB_PARAMETERS+")");
				logger.error(XMLhelper.toString(d));
				throw new ConfigurationException();
			}
		} catch (Throwable t) {
			logger.error("Cant check how many parameters there are in this document");
			throw new ConfigurationException();
		}		
	}
	
	/**
	 * This method parses the given SML description document and extracts the sensorID
	 * @param d the SML description document
	 * @throws ConfigurationException if the sensor ID cant be found
	 */
	private void parseSensorID(Document d) throws ConfigurationException {
		try {
			sid = Integer.parseInt(XMLhelper.getAttributeFromName(XPATH_SENSOR_DESC, SMLConstants.SENSOR_ID_ATTRIBUTE_NODE, d));
		} catch (NumberFormatException e) {
			logger.error("SID is not a number");
			throw new ConfigurationException();
		} catch (Exception e) {
			logger.error("Cant find attr '"+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"' in SMLdescriptor XML doc");
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method parses the parameter list is the SML description document and extracts each of them. It assumes the SML
	 * document contains all the required parameters and only those.
	 * @param d the SML description document
	 * @throws ConfigurationException if there is an error finding the required arguments or parsing their values
	 */
	private void parseParameters(Document d) throws ConfigurationException {
		String paramName, paramValue;
		Iterator<String> i = SMLConstants.PARAM_NAMES.iterator();
		
		while(i.hasNext()){
			paramName = i.next();
			try {
				paramValue = XMLhelper.getAttributeFromName(XPATH_SENSOR_PARAMETER+"[@"+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+
															"=\""+paramName+"\"]", SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE, d);
				parameters.put(paramName, paramValue);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Cant find value for parameter '"+paramName+"' in SMLdescriptor XML doc");
				throw new ConfigurationException();
			}
		}
	}
	
	/**
	 * This method returns the sensor ID
	 * @return the sensor ID
	 */
	public int getSID() {
		return sid.intValue();
	}
	
	/**
	 * This method returns the value of a parameter given its name.
	 * @param n the name of the parameter
	 * @return the value of the parameter, which can be null if no parameter with this name is found
	 */
	public String getParameter(String n){
		return parameters.get(n);
	}
	
	/**
	 * This method returns a set of parameter names associated with this sensor description document.
	 * @return a set of parameter names
	 */
	public Set<String> getParameterNames(){
		return new HashSet<String>(parameters.keySet());
	}
	
	/**
	 * This method returns the name of the protocol associated with the sensor listed in this SML description
	 * @return the name of the protocol associated with the sensor listed in this SML description
	 */
	public String getProtocolName(){
		return parameters.get(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
	}
	
	/**
	 * This method returns the address of the sensor listed in this SML description
	 * @return the address of the sensor listed in this SML description
	 */
	public String getSensorAddress(){
		return parameters.get(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE);
	}
	
	/**
	 * This method returns the XML version of this SMLDescription object as a string
	 * @return the XML version of this SMLDescription object
	 */
	public String getSMLString() {
		Iterator<String> i = parameters.keySet().iterator();
		String n;
		StringBuffer sb = new StringBuffer();
		sb.append("<"+SMLConstants.SENSOR_TAG+" "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\""+sid.toString()+"\">\n"
			+"\t<"+SMLConstants.PARAMETERS_NODE+">\n");

		while(i.hasNext()) {
			n = i.next();
			sb.append("\t\t<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+n+
					"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\""+parameters.get(n)+"\" />\n");
		}

		sb.append("\t</"+SMLConstants.PARAMETERS_NODE+">\n</"+SMLConstants.SENSOR_TAG+">\n");
		return sb.toString();		
	}
	
	/**
	 * This method returns the XML version of this SMLDescription object as a DOM document
	 * @return the XML version of this SMLDescription object 
	 */
	public Document getSML(){
		try {
			return XMLhelper.createDocument(getSMLString());
		} catch (ParserConfigurationException e) {
			logger.error("error creating XML SML doc");
		}
		return null;
	}
	
}
