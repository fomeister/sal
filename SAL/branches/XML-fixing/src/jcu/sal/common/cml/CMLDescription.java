/**
 * 
 */
package jcu.sal.common.cml;

import java.util.List;
import java.util.Vector;

import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
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
	static {Slog.setupLogger(logger);}
	
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
	 * This method constructs a CML description object
	 * @param mName the name of the method which should be called when a command instance matching this description is received
	 * @param id the command id
	 * @param name the name of the command
	 * @param desc the description of the command
	 * @param argTypes an array containing the argument types
	 * @param argNames an array containing the argNames of the arguments
	 * @param returnType the type of the return result
	 * @throws SALRunTimeException if the three arrays have different lengths,   
	 */
	public CMLDescription(String methodName, Integer id, String name, String desc, List<ArgumentType> argTypes,
			List<String> argNames, ReturnType returnType){
		cid=id;
		this.name = name;
		this.desc = desc;
		this.argTypes = new Vector<ArgumentType>(argTypes);
		this.argNames = new Vector<String>(argNames);
		this.returnType = returnType;
		this.methodName = methodName!=null ? methodName : "";
		
		if(argTypes.size()!=argNames.size()) {
			logger.error("Error creating the CML doc: arguments number unequals somewhere");
			throw new SALRunTimeException("number of argument types different from number of argument names");
		}

	}
	
	/**
	 * This method creates a new a CML description object re-using the same description, argument types & argNames and return type
	 * as an existing description.
	 * @param id the command id of the new description
	 * @param name the name of the new command
	 * @param existing the existing description whose description, argument types & argNames and return type will be reused
	 */
	public CMLDescription(Integer id, String name, CMLDescription existing){
		this(existing.getMethodName(), id, name,existing.getDesc(), existing.getArgTypes(), existing.getArgNames(), existing.getReturnType() );
	}
	
	/**
	 * This method creates a new a CML description object based on a given CML description document.
	 * @param existing the existing description
	 * @throws SALDocumentException if the existing document cant be parsed or contains more than one CML description, ie
	 * it is a CML descriptions document
	 */
	public CMLDescription(Document existing) throws SALDocumentException{
		
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
	 * @throws SALDocumentException if the number of CML descriptions is not 1
	 */
	private void checkDocument(Document d) throws SALDocumentException{
		try {
			int nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_CMD_DESC+")", d));
			if(nb!=1){
				logger.error("There are too many CML descriptions ("+nb+") in this document");
				logger.error(XMLhelper.toString(d));
				throw new SALDocumentException("Too many CML descriptions ("+nb+") in this document, expected 1 only");
			}
		} catch (Throwable t) {
			logger.error("Cant check how many CML descriptions are in this document");
			throw new SALDocumentException("Unable to check the CML description sections in this document", t);
		}
	}
	
	/**
	 * This method parses the given CML description document and extracts the CID
	 * @param d the CML description document
	 * @throws SALDocumentException if the CID cant be parsed
	 */
	private void parseCID(Document d) throws SALDocumentException {
		try {
			cid = Integer.parseInt(XMLhelper.getAttributeFromName(XPATH_CMD_DESC, CMLConstants.CID_ATTRIBUTE, d));
		} catch (NumberFormatException e) {
			logger.error("CID is not a number");
			throw new SALDocumentException("CID is not a number",e);
		} catch (Exception e) {
			logger.error("Cant find CID in CMLdescription XML doc");
			throw new SALDocumentException("Cant find CID in CML doc", e);
		}
		
	}
	
	/**
	 * This method parses the given CML description document and extracts the description
	 * @param d the CML description document
	 * @throws ConfigurationException if the description cant be parsed
	 */
	private void parseShortDescription(Document d) throws SALDocumentException {
		try {
			desc = XMLhelper.getTextValue(XPATH_CMD_DESC_SHORT_DESC, d);
		} catch (Exception e) {
			logger.error("Cant parse the descrption in CMLdescription XML doc");
			throw new SALDocumentException("Cant parse the descrption in CML doc", e);
		}
		
	}
	
	/**
	 * This method parses the given CML description document and extracts the name
	 * @param d the CML description document
	 * @throws SALDocumentException if the name cant be parsed
	 */
	private void parseName(Document d) throws SALDocumentException {
		try {
			name = XMLhelper.getTextValue(XPATH_CMD_DESC_NAME, d);
		} catch (Exception e) {
			logger.error("Cant parse the name in CMLdescription XML doc");
			throw new SALDocumentException("Cant parse the name in cML doc",e) ;
		}
	}
	
	/**
	 * This method parses the given CML description document and extracts the return type
	 * @param d the CML description document
	 * @throws SALDocumentException if the return type cant be parsed
	 */
	private void parseReturnType(Document d) throws SALDocumentException {
		try {
			returnType = new ReturnType(XMLhelper.getAttributeFromName(XPATH_CMD_DESC_RETURN_TYPE,CMLConstants.TYPE_ATTRIBUTE,  d));
		} catch (Exception e) {
			logger.error("Cant parse the return type in CMLdescription XML doc");
			throw new SALDocumentException("Cant parse the return type in CML doc",e);
		}
	}
	
	/**
	 * This method parses the given CML description document and extracts the argument types and argNames
	 * @param d the CML description document
	 * @throws SALDocumentException if the arguments cant be parsed
	 */
	private void parseArguments(Document d) throws SALDocumentException {
		int nbArgs;
		
		try {
			nbArgs = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_CMD_DESC_ARGUMENT+")", d));
		} catch (NumberFormatException e) {
			logger.error("Cant count how many arguments are needed for this command");
			throw new SALDocumentException("Arguments section malformed", e);
		} catch (NotFoundException e) {
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
				throw new SALDocumentException("Cant parse the argument name for argument '"+i+"'",e);
			}
			
			try {
				argTypes.add(new ArgumentType(XMLhelper.getAttributeFromName(XPATH_CMD_DESC_ARGUMENT+"["+(i+1)+"]", CMLConstants.TYPE_ATTRIBUTE, d)));
			} catch (Exception e) {
//				logger.error("Cant parse the argument type for argument "+i+" in CMLdescription XML doc");
//				logger.error("XPATH: "+XPATH_CMD_DESC_ARGUMENT+"["+(i+1)+"]");
//				logger.error("Document: "+XMLhelper.toString(d));
//				e.printStackTrace();
				throw new SALDocumentException("Cant parse the argument type (or invalid argument type) for argument '"+i+"'",e);
			}
		}
	}

	
	/**
	 * This method returns this CML description's document as a String
	 * @return the CML description document as a String
	 */
	public String getXMLString(){
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
	 * @return the CML description document
	 */
	public Document getXML(){
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (SALDocumentException e) {
			logger.error("error creating XML CML doc");
			throw new SALRunTimeException("Cant creat the CML document",e);
		}
	}
	
	/**
	 * This method returns this CML description's command identifier
	 * @return this CML description's CID
	 */
	public Integer getCID(){
		return cid;
	}
	
	/**
	 * This method returns this CML description's name, ie the name of the command
	 * @return this CML description's name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * This method returns the name of the method associated with this CML description
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
	public ArgumentType getArgType(String name) throws NotFoundException{
		int pos = argNames.indexOf(name);
		if(pos>=0)
			return argTypes.get(pos);
		throw new NotFoundException("Argument '"+name+"' not found");
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((argNames == null) ? 0 : argNames.hashCode());
		result = PRIME * result + ((argTypes == null) ? 0 : argTypes.hashCode());
		result = PRIME * result + ((cid == null) ? 0 : cid.hashCode());
		result = PRIME * result + ((desc == null) ? 0 : desc.hashCode());
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((returnType == null) ? 0 : returnType.hashCode());
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
		final CMLDescription other = (CMLDescription) obj;
		if (argNames == null) {
			if (other.argNames != null)
				return false;
		} else if (!argNames.equals(other.argNames))
			return false;
		if (argTypes == null) {
			if (other.argTypes != null)
				return false;
		} else if (!argTypes.equals(other.argTypes))
			return false;
		if (cid == null) {
			if (other.cid != null)
				return false;
		} else if (!cid.equals(other.cid))
			return false;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}
}
