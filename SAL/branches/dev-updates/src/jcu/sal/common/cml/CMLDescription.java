/**
 * 
 */
package jcu.sal.common.cml;

import java.util.List;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.common.cml.xml.Argument;
import jcu.sal.common.cml.xml.CommandDescription;
import jcu.sal.common.cml.xml.ObjectFactory;
import jcu.sal.common.cml.xml.CommandDescription.Arguments;
import jcu.sal.common.exceptions.ArgumentNotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.utils.JaxbHelper;
import jcu.sal.common.utils.XMLhelper;

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
	
	private CommandDescription commandDescription;
	private static final ObjectFactory factory = new ObjectFactory();
	private String methodName;
	
	private CMLDescription(String m){
		methodName = m!=null ? m : "";
		commandDescription = factory.createCommandDescription();
	}
	
	/**
	 * This package-private constructor is meant to be used only by 
	 * {@link CMLDescriptions} objects
	 * @param c
	 */
	CMLDescription(CommandDescription c) {
		commandDescription = c;
		methodName="";
	}
	/**
	 * This method constructs a CML description object
	 * @param mName the name of the method which should be called when a command instance matching this description is received
	 * @param id the command id
	 * @param name the name of the command
	 * @param desc the short description of the command
	 * @param args a list of {@link CMLArgument}s representing the arguments to this command
	 * @param returnType the type of the return result
	 * @throws SALRunTimeException if any of the argument is invalid   
	 */
	public CMLDescription(String methodName, Integer id, String name, String desc, List<CMLArgument> args, ResponseType returnType){
		this(methodName);
		commandDescription.setCid(id.toString());
		commandDescription.setName(name);
		commandDescription.setLongDescription("");
		commandDescription.setShortDescription(desc);
		commandDescription.setResponse(returnType.getResponse());
		commandDescription.setArguments(factory.createCommandDescriptionArguments());
		Arguments a = commandDescription.getArguments();

		if(args!=null)
			for(CMLArgument c: args)
				a.getArgument().add(c.getArgument());
		

	}
	
	/**
	 * This method creates a new a CML description object re-using the same description, argument types & argNames and return type
	 * as an existing description.
	 * @param id the command id of the new description
	 * @param name the name of the new command
	 * @param existing the existing description whose description, argument types & argNames and return type will be reused
	 */
	public CMLDescription(Integer id, String name, CMLDescription existing){
		this(existing.methodName, id, name,existing.getShortDesc(), existing.getArguments(), existing.getReturnType() );
	}
	
	/**
	 * This method returns this CML description's document as a String
	 * @return the CML description document as a String
	 */
	public String getXMLString(){
		return JaxbHelper.toXmlString(commandDescription);
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
		return new Integer(commandDescription.getCid());
	}
	
	/**
	 * This method returns this CML description's name, ie the name of the command
	 * @return this CML description's name
	 */
	public String getName(){
		return commandDescription.getName();
	}
	
	/**
	 * This method returns the name of the method associated with this CML description
	 * @return the method associated with this CML description
	 */
	public String getMethodName(){
		return methodName;
	}
	
	/**
	 * This method returns this CML description's short description
	 * @return an this CML description's short description
	 */
	public String getShortDesc() {
		return commandDescription.getShortDescription();
	}
	
	/**
	 * This method returns this CML description's long description
	 * @return an this CML description's long description
	 */
	public String getLongDesc() {
		return commandDescription.getLongDescription();
	}

	/**
	 * This method returns a list of {@link CMLArgument} for all
	 * arguments in this description. 
	 * @return a list of {@link CMLArgument}.
	 */
	public List<CMLArgument> getArguments(){
		Vector<CMLArgument> v = new Vector<CMLArgument>();
		if(commandDescription.getArguments()!=null)
			for(Argument a: commandDescription.getArguments().getArgument())
				v.add(new CMLArgument(a));
		return v;
	}
	
	/**
	 * This method returns a list of argument names in this object.
	 * @return a list of argument names in this object.
	 */
	public List<String> getArgNames(){
		Vector<String> n = new Vector<String>();
		if(commandDescription.getArguments().getArgument()!=null)
			for(Argument a: commandDescription.getArguments().getArgument())
				n.add(a.getName());
		return n;
	}
	
	/**
	 * This method returns a list of {@link ArgumentType}s for all
	 * argument in this object.
	 * @return a list of {@link ArgumentType}s in this object.
	 */
	public List<ArgumentType> getArgTypes(){
		Vector<ArgumentType> n = new Vector<ArgumentType>();
		if(commandDescription.getArguments()!=null)
			for(Argument a: commandDescription.getArguments().getArgument())
				n.add(new ArgumentType(a.getType()));
		return n;
	}
	
	/**
	 * This method returns the {@link ArgumentType} of an argument given its name
	 * @param name the name of the argument whose {@link ArgumentType} is to
	 * be returned
	 * @return the {@link ArgumentType} of an argument.
	 * @throws ArgumentNotFoundException if no argument matches the given name.
	 */
	public ArgumentType getArgType(String name){
		if(commandDescription.getArguments()!=null)
			for(Argument a: commandDescription.getArguments().getArgument())
				if(a.getName().equals(name))
					return new ArgumentType(a.getType());
		
		throw new ArgumentNotFoundException("Cant find argument named "+name);
	}
	
	/**
	 * This method returns the {@link CMLArgument} of an argument given its name
	 * @param name the name of the argument whose {@link CMLArgument} is
	 * to be returned
	 * @return the {@link CMLArgument} of the argument.
	 * @throws ArgumentNotFoundException if no argument matches the given name.
	 */
	public CMLArgument getArgument(String name){
		if(commandDescription.getArguments()!=null)
			for(Argument a: commandDescription.getArguments().getArgument())
				if(a.getName().equals(name))
					return new CMLArgument(a);
		
		throw new ArgumentNotFoundException("Cant find argument named "+name);
	}
	
	/**
	 * This method returns the return type for this  CML description
	 * @return the return type for this  CML description
	 */
	public ResponseType getReturnType() {
		return new ResponseType(commandDescription.getResponse());
	}
	
	/**
	 * This package-private methods is meant to be used
	 * only internally.
	 * @return
	 */
	CommandDescription getCommandDescription(){
		return commandDescription;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((commandDescription == null) ? 0 : commandDescription
						.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CMLDescription other = (CMLDescription) obj;
		if (commandDescription == null) {
			if (other.commandDescription != null)
				return false;
		} else if (!commandDescription.equals(other.commandDescription))
			return false;
		return true;
	}
	
}
