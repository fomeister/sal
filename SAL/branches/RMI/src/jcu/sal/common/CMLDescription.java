/**
 * 
 */
package jcu.sal.common;

import javax.naming.ConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class encapsulates a CML Command Description document. 
 * @author gilles
 *
 */
public class CMLDescription {
	private static Logger logger = Logger.getLogger(CMLDescription.class);
	static {
		Slog.setupLogger(logger);
	}

	private Integer cid;
	private String cml;
	private String name;
	private String methodName;
	private String desc;
	private ArgTypes[] argTypes;
	private String[] argNames;
	private ReturnType returnType;
	
	
	/**
	 * This method constructs a CML descpritor object
	 * @param mName the name of the method which should be called when a command instance matching this descriptor is received
	 * @param id the command id
	 * @param name the name of the command
	 * @param desc the description of the command
	 * @param argTypes an array containing the argument types
	 * @param argNames an array containing the argNames of the arguments
	 * @param returnType the type of the return result
	 * @throws ConfigurationException if the three arrays have different lengths,   
	 */
	public CMLDescription(String mName, Integer id, String name, String desc, ArgTypes[] argTypes,  String[] argNames, ReturnType returnType) throws ConfigurationException{
		cid=id;
		this.name = name;
		this.desc = desc;
		this.argTypes = argTypes;
		this.argNames = argNames;
		this.returnType = returnType;
		
		if(argTypes.length!=argNames.length) {
			logger.error("Error creating the CML doc: arguments number unequals somewhere");
			throw new ConfigurationException();
		}

		cml = "<CommandDescription cid=\""+id.toString()+"\">\n"
				+"\t<Name>"+name+"</Name>\n"
				+"\t<ShortDescription>"+desc+"</ShortDescription>\n"
				+"\t<arguments count=\""+argTypes.length+"\">\n";
		for (int i = 0; i < argTypes.length; i++)
			cml += "\t\t<Argument type=\""+argTypes[i].getArgType()+"\" name=\""+argNames[i]+"\" />\n";

		cml +=	"\t</arguments>\n"
				+"\t<ReturnType type=\""+returnType.getReturnType()+"\" />\n"
				+"</CommandDescription>\n";
		
		methodName = mName;
	}
	
	/**
	 * This method creates a new a CML descpritor object re-using the same description, argument types & argNames and return type
	 * as an existing descriptor.
	 * @param id the command id of the new descriptor
	 * @param name the name of the new command
	 * @param existing the existing descriptor whose description, argument types & argNames and return type will be reused
	 */
	public CMLDescription(Integer id, String name, CMLDescription existing){
		cid=id;
		this.name = name;
		cml = existing.getCML();
		cml = cml.replaceFirst("<CommandDescription.*\n.*/Name>", "");

		cml = "<CommandDescription cid=\""+id.toString()+"\">\n"
				+"\t<Name>"+name+"</Name>\n"
				+cml;
		desc = existing.getDesc();
		argTypes = existing.getArgTypes();
		argNames = existing.getArgNames();
		returnType = existing.getReturnType();
		methodName = existing.getMethodName();
	}
	
	/**
	 * This method creates a new a CML descpritor object based on a given CML document
	 * @param existing the existing descriptor
	 * @throws ConfigurationException if the existing document cant be parsed
	 */
	public CMLDescription(Document existing) throws ConfigurationException{
		cml = XMLhelper.toString(existing);

		parseName(existing);
		parseCID(existing);
		parseShortDescription(existing);
		parseArguments(existing);
		parseReturnType(existing);
		
		methodName = "";
		
	}
	
	/**
	 * This method parse the given CML descriptor document and extracts the CID
	 * @param d the CML descriptor document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseCID(Document d) throws ConfigurationException {
		try {
			cid = Integer.parseInt(XMLhelper.getAttributeFromName(CMLConstants.XPATH_CMD_DESC, CMLConstants.CID_ATTRIBUTE, d));
		} catch (NumberFormatException e) {
			logger.error("CID is not a number");
			throw new ConfigurationException();
		} catch (Exception e) {
			logger.error("Cant find CID in CMLdescriptor XML doc");
			throw new ConfigurationException();
		}
		
	}
	
	/**
	 * This method parse the given CML descriptor document and extracts the description
	 * @param d the CML descriptor document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseShortDescription(Document d) throws ConfigurationException {
		try {
			desc = XMLhelper.getTextValue(CMLConstants.XPATH_CMD_DESC_SHORT_DESC, d);
		} catch (Exception e) {
			logger.error("Cant parse the descrption in CMLdescriptor XML doc");
			throw new ConfigurationException();
		}
		
	}
	
	/**
	 * This method parse the given CML descriptor document and extracts the name
	 * @param d the CML descriptor document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseName(Document d) throws ConfigurationException {
		try {
			name = XMLhelper.getTextValue(CMLConstants.XPATH_CMD_DESC_NAME, d);
		} catch (Exception e) {
			logger.error("Cant parse the name in CMLdescriptor XML doc");
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method parse the given CML descriptor document and extracts the return type
	 * @param d the CML descriptor document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseReturnType(Document d) throws ConfigurationException {
		try {
			returnType = new ReturnType(XMLhelper.getAttributeFromName(CMLConstants.XPATH_CMD_DESC_RETURN_TYPE,CMLConstants.TYPE_ATTRIBUTE,  d));
		} catch (Exception e) {
			logger.error("Cant parse the return type in CMLdescriptor XML doc");
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method parse the given CML descriptor document and extracts the argument types and argNames
	 * @param d the CML descriptor document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseArguments(Document d) throws ConfigurationException {
		int nbArgs;
		
		try {
			nbArgs = Integer.parseInt(XMLhelper.getTextValue("count("+CMLConstants.XPATH_CMD_DESC_ARGUMENTS+")", d));
		} catch (NumberFormatException e) {
			logger.error("Cant count how many arguments are needed for this command");
			throw new ConfigurationException();
		} catch (XPathExpressionException e) {
			/*
			 * The <arguments> section is not mandatory if there are no argument
			 */
			logger.debug("Cant find the arguments in CMLdescriptor XML doc");
			nbArgs = 0;
		}
		argTypes = new ArgTypes[nbArgs];
		argNames = new String[nbArgs];
		for(int i=0; i<nbArgs; i++) {
			try {
				argTypes[i] = new ArgTypes(XMLhelper.getAttributeFromName(CMLConstants.XPATH_CMD_DESC_ARGUMENT, CMLConstants.TYPE_ATTRIBUTE, d));
			} catch (Exception e) {
				logger.error("Cant parse the argument type for argument "+i+" in CMLdescriptor XML doc");
				throw new ConfigurationException();
			}
			
			try {
				argNames[i] = XMLhelper.getTextValue(CMLConstants.XPATH_CMD_DESC_NAME, d);
			} catch (Exception e) {
				logger.error("Cant parse the argument name for argument "+i+" in CMLdescriptor XML doc");
				throw new ConfigurationException();
			}
		}
	}

	
	/**
	 * This method returns this CML descriptor's document as a String
	 * @return the CML descripor document as a String
	 */
	public String getCML(){
		return cml;
	}
	
	/**
	 * This method returns this CML descriptor's CID
	 * @return this CML descriptor's CID
	 */
	public Integer getCID(){
		return cid;
	}
	
	/**
	 * This method returns this CML descriptor's name
	 * @return this CML descriptor's name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * This method returns the name of the method associated with this CML descpriptor
	 * @return the method associated with this CML descriptor
	 */
	public String getMethodName(){
		return methodName;
	}

	/**
	 * This method returns an array of argument types for this  CML descriptor
	 * @return an array of argument types for this  CML descriptor
	 */
	public ArgTypes[] getArgTypes() {
		return argTypes;
	}
	
	/**
	 * This method returns the number of arguments for this  CML descriptor
	 * @return the number of arguments for this  CML descriptor
	 */
	public int getArgCount() {
		return argTypes.length;
	}

	/**
	 * This method returns this CML descriptor's description
	 * @return an this CML descriptor's description
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * This method returns an array of argument argNames for this  CML descriptor
	 * @return an array of argument argNames for this  CML descriptor
	 */
	public String[] getArgNames() {
		return argNames;
	}

	/**
	 * This method returns the return type for this  CML descriptor
	 * @return the return type for this  CML descriptor
	 */
	public ReturnType getReturnType() {
		return returnType;
	}
}