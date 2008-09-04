/**
 * 
 */
package jcu.sal.common.cml;

import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class encapsulates a CML Command Description document. This document describes a single command.
 * @author gilles
 *
 */
public class CMLDescription {
	private static Logger logger = Logger.getLogger(CMLDescription.class);
	static {
		Slog.setupLogger(logger);
	}
	
	private static String XPATH_CMD_DESC = "//"+CMLConstants.CMD_DESCRIPTION_TAG;
	private static String XPATH_CMD_DESC_SHORT_DESC = XPATH_CMD_DESC+"/"+CMLConstants.SHORT_DESCRIPTION_TAG;
	private static String XPATH_CMD_DESC_NAME = XPATH_CMD_DESC+"/"+CMLConstants.NAME_TAG;
	private static String XPATH_CMD_DESC_ARGUMENTS = XPATH_CMD_DESC+"/"+CMLConstants.ARGUMENTS_TAG;
	private static String XPATH_CMD_DESC_ARGUMENT = XPATH_CMD_DESC_ARGUMENTS + "/" + CMLConstants.ARGUMENT_TAG;
	private static String XPATH_CMD_DESC_RETURN_TYPE = XPATH_CMD_DESC+"/"+CMLConstants.RETURN_TYPE_TAG;

	private Integer cid;
	private String name;
	private String methodName;
	private String desc;
	private List<ArgumentType> argTypes;
	private List<String> argNames;
	private ReturnType returnType;
	
	
	/**
	 * This method constructs a CML descpriton object
	 * @param mName the name of the method which should be called when a command instance matching this description is received
	 * @param id the command id
	 * @param name the name of the command
	 * @param desc the description of the command
	 * @param argTypes an array containing the argument types
	 * @param argNames an array containing the argNames of the arguments
	 * @param returnType the type of the return result
	 * @throws ConfigurationException if the three arrays have different lengths,   
	 */
	public CMLDescription(String methodName, Integer id, String name, String desc, List<ArgumentType> argTypes,  List<String> argNames, ReturnType returnType) throws ConfigurationException{
		cid=id;
		this.name = name;
		this.desc = desc;
		this.argTypes = new Vector<ArgumentType>(argTypes);
		this.argNames = new Vector<String>(argNames);
		this.returnType = returnType;
		this.methodName = methodName!=null ? methodName : "";
		
		if(argTypes.size()!=argNames.size()) {
			logger.error("Error creating the CML doc: arguments number unequals somewhere");
			throw new ConfigurationException("number of argument types different from number of argument names");
		}

	}
	
	/**
	 * This method creates a new a CML descpritor object re-using the same description, argument types & argNames and return type
	 * as an existing description.
	 * @param id the command id of the new description
	 * @param name the name of the new command
	 * @param existing the existing description whose description, argument types & argNames and return type will be reused
	 * @throws ConfigurationException 
	 */
	public CMLDescription(Integer id, String name, CMLDescription existing) throws ConfigurationException{
		this(existing.getMethodName(), id, name,existing.getDesc(), existing.getArgTypes(), existing.getArgNames(), existing.getReturnType() );
	}
	
	/**
	 * This method creates a new a CML descpriton object based on a given CML description document.
	 * @param existing the existing description
	 * @throws ConfigurationException if the existing document cant be parsed or contains more than one CML description, ie
	 * it is a CML descriptions document
	 */
	public CMLDescription(Document existing) throws ConfigurationException{
		
		checkDocument(existing);
		parseName(existing);
		parseCID(existing);
		parseShortDescription(existing);
		parseArguments(existing);
		parseReturnType(existing);
		
		methodName = "";
		
	}
	
	/**
	 * This method checks that there is only one CML description in the provided document
	 * @param d the document to be checked
	 * @throws ConfigurationException if the number of CML descriptions is not 1
	 */
	private void checkDocument(Document d) throws ConfigurationException{
		try {
			int nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_CMD_DESC+")", d));
			if(nb!=1){
				logger.error("There are too many CML descriptions ("+nb+") in this document");
				logger.error(XMLhelper.toString(d));
				throw new ConfigurationException("Too many CML descriptions ("+nb+") in this document, expected 1 only");
			}
		} catch (Throwable t) {
			logger.error("Cant check how many CML descriptions are in this document");
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method parses the given CML descripton document and extracts the CID
	 * @param d the CML description document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseCID(Document d) throws ConfigurationException {
		try {
			cid = Integer.parseInt(XMLhelper.getAttributeFromName(XPATH_CMD_DESC, CMLConstants.CID_ATTRIBUTE, d));
		} catch (NumberFormatException e) {
			logger.error("CID is not a number");
			throw new ConfigurationException("CID is not a number");
		} catch (Exception e) {
			logger.error("Cant find CID in CMLdescription XML doc");
			throw new ConfigurationException("Cant find CID in  XML doc");
		}
		
	}
	
	/**
	 * This method parses the given CML description document and extracts the description
	 * @param d the CML description document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseShortDescription(Document d) throws ConfigurationException {
		try {
			desc = XMLhelper.getTextValue(XPATH_CMD_DESC_SHORT_DESC, d);
		} catch (Exception e) {
			logger.error("Cant parse the descrption in CMLdescription XML doc");
			throw new ConfigurationException("Cant parse the descrption in XML doc");
		}
		
	}
	
	/**
	 * This method parses the given CML description document and extracts the name
	 * @param d the CML description document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseName(Document d) throws ConfigurationException {
		try {
			name = XMLhelper.getTextValue(XPATH_CMD_DESC_NAME, d);
		} catch (Exception e) {
			logger.error("Cant parse the name in CMLdescription XML doc");
			throw new ConfigurationException("Cant parse the name in XML doc");
		}
	}
	
	/**
	 * This method parses the given CML description document and extracts the return type
	 * @param d the CML description document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseReturnType(Document d) throws ConfigurationException {
		try {
			returnType = new ReturnType(XMLhelper.getAttributeFromName(XPATH_CMD_DESC_RETURN_TYPE,CMLConstants.TYPE_ATTRIBUTE,  d));
		} catch (Exception e) {
			logger.error("Cant parse the return type in CMLdescription XML doc");
			throw new ConfigurationException("Cant parse the return type in XML doc");
		}
	}
	
	/**
	 * This method parses the given CML description document and extracts the argument types and argNames
	 * @param d the CML description document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseArguments(Document d) throws ConfigurationException {
		int nbArgs;
		
		try {
			nbArgs = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_CMD_DESC_ARGUMENT+")", d));
		} catch (NumberFormatException e) {
			logger.error("Cant count how many arguments are needed for this command");
			throw new ConfigurationException("Arguments section malformed");
		} catch (XPathExpressionException e) {
			/*
			 * The <arguments> section is not mandatory if there are no argument
			 */
			logger.debug("Cant find the arguments in CMLdescription XML doc");
			nbArgs = 0;
		}
		
		argTypes = new Vector<ArgumentType>(nbArgs);
		argNames = new Vector<String>(nbArgs);
		for(int i=0; i<nbArgs; i++) {
			try {
				argNames.add(XMLhelper.getAttributeFromName(XPATH_CMD_DESC_ARGUMENT+"["+(i+1)+"]", CMLConstants.NAME_ATTRIBUTE, d));
			} catch (Exception e) {
//				logger.error("Cant parse the argument name for argument "+i+" in CMLdescription XML doc");
//				logger.error("XPATH: "+XPATH_CMD_DESC_ARGUMENT+"["+(i+1)+"]");
//				logger.error("Document: "+XMLhelper.toString(d));
//				e.printStackTrace();
				throw new ConfigurationException("Cant parse the argument name for argument '"+i+"'");
			}
			
			try {
				argTypes.add(new ArgumentType(XMLhelper.getAttributeFromName(XPATH_CMD_DESC_ARGUMENT+"["+(i+1)+"]", CMLConstants.TYPE_ATTRIBUTE, d)));
			} catch (Exception e) {
//				logger.error("Cant parse the argument type for argument "+i+" in CMLdescription XML doc");
//				logger.error("XPATH: "+XPATH_CMD_DESC_ARGUMENT+"["+(i+1)+"]");
//				logger.error("Document: "+XMLhelper.toString(d));
//				e.printStackTrace();
				throw new ConfigurationException("Cant parse the argument type (or invaid argument type) for argument '"+i+"'");
			}
		}
	}

	
	/**
	 * This method returns this CML description's document as a String
	 * @return the CML descripor document as a String
	 */
	public String getCMLString(){
		String cml = "<"+CMLConstants.CMD_DESCRIPTION_TAG+" "+CMLConstants.CID_ATTRIBUTE+"=\""+cid.toString()+"\">\n"
				+"\t<"+CMLConstants.NAME_TAG+">"+name+"</"+CMLConstants.NAME_TAG+">\n"
				+"\t<"+CMLConstants.SHORT_DESCRIPTION_TAG+">"+desc+"</"+CMLConstants.SHORT_DESCRIPTION_TAG+">\n"
				+"\t<"+CMLConstants.ARGUMENTS_TAG+">\n";
		for (int i = 0; i < argTypes.size(); i++)
			cml += "\t\t<"+CMLConstants.ARGUMENT_TAG+" "+CMLConstants.TYPE_ATTRIBUTE+"=\""+argTypes.get(i).getArgType()+
					"\" "+CMLConstants.NAME_ATTRIBUTE+"=\""+argNames.get(i)+"\" />\n";

		cml +=	"\t</"+CMLConstants.ARGUMENTS_TAG+">\n"
				+"\t<"+CMLConstants.RETURN_TYPE_TAG+" "+CMLConstants.TYPE_ATTRIBUTE+"=\""+returnType.getReturnType()+"\" />\n"
				+"</"+CMLConstants.CMD_DESCRIPTION_TAG+">\n";
		return cml;
	}
	
	/**
	 * This method returns this CML description's document
	 * @return the CML descripor document
	 */
	public Document getCML(){
		try {
			return XMLhelper.createDocument(getCMLString());
		} catch (ParserConfigurationException e) {
			logger.error("error creating XML CML doc");
		}
		return null;
	}
	
	/**
	 * This method returns this CML description's CID
	 * @return this CML description's CID
	 */
	public Integer getCID(){
		return cid;
	}
	
	/**
	 * This method returns this CML description's name
	 * @return this CML description's name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * This method returns the name of the method associated with this CML descpriptor
	 * @return the method associated with this CML description
	 */
	public String getMethodName(){
		return methodName;
	}

	/**
	 * This method returns an array of argument types for this  CML description
	 * @return an array of argument types for this  CML description
	 */
	public List<ArgumentType> getArgTypes() {
		return new Vector<ArgumentType>(argTypes);
	}
	
	/**
	 * This method returns the number of arguments for this  CML description
	 * @return the number of arguments for this  CML description
	 */
	public int getArgCount() {
		return argTypes.size();
	}

	/**
	 * This method returns this CML description's description
	 * @return an this CML description's description
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * This method returns an array of argument argNames for this  CML description
	 * @return an array of argument argNames for this  CML description
	 */
	public List<String> getArgNames() {
		return new Vector<String>(argNames);
	}

	/**
	 * This method returns the return type for this  CML description
	 * @return the return type for this  CML description
	 */
	public ReturnType getReturnType() {
		return returnType;
	}
	
	/**
	 * This method returns the argument type for a given argument.
	 * @param name the name of the argument whose the type will be returned 
	 * @return the argument type for a given argument.
	 * @throws ConfigurationException if the argument <code>"name"</code> does not exist
	 */
	public ArgumentType getArgType(String name) throws ConfigurationException{
		int pos = argNames.indexOf(name);
		if(pos>=0)
			return argTypes.get(pos);
		throw new ConfigurationException("Argument '"+name+"' not found");
	}
}
