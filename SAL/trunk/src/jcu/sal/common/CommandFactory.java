package jcu.sal.common;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.ConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.components.protocols.CMLDescription.ArgTypes;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class CommandFactory {
	private static Logger logger = Logger.getLogger(CommandFactory.class);
	static {
		Slog.setupLogger(logger);
	}

	private int cid;
	private Hashtable<String,ArgTypes> args;
	private Hashtable<String, String> argValues;
	private Vector<String> missingArgs;
	private StreamCallback callback;
	
	/**
	 * Create a command template based on the given Command's CML descriptor 
	 * using values in the command instance
	 * @param desc the command description document
	 * @param inst the command instance document
	 * @return whether or not some arguments are missing
	 */
	public CommandFactory(Document desc, Document inst)  throws ConfigurationException{
		int cid;
		try {
			cid = Integer.parseInt(XMLhelper.getAttributeFromName(CMLConstants.XPATH_CMD_INST, CMLConstants.CID_ATTRIBUTE, inst));
		} catch (Exception e) {
			logger.error("Cant parse the cid in CML instance XML doc");
			throw new ConfigurationException();
		}
		args = new Hashtable<String, ArgTypes>();
		argValues = new Hashtable<String, String>();
		missingArgs = new Vector<String>();
		parseArguments(desc, cid);
		parseArgumentValues(inst);
	}
	
	/**
	 * Create a empty command template based on the Command description document 
	 * and the givencommand id
	 * @param desc the command description document
	 * @param cid the command id
	 */
	public CommandFactory(Document desc, int cid) throws ConfigurationException{
		argValues = new Hashtable<String, String>();
		args = new Hashtable<String, ArgTypes>();
		missingArgs = new Vector<String>();
		parseArguments(desc, cid);
	}
	
	/**
	 * This method returns the type of a given argument
	 * @param name the name of the argument
	 * @return the type of the argument
	 * @throws ConfigurationException if the argument cant be found
	 */
	public ArgTypes getArgType(String name) throws ConfigurationException {
		ArgTypes t = args.get(name);
		if(t==null)
			throw new ConfigurationException();
		return t;
			
	}	
	
	/**
	 * This method returns an Enumeration<String> of the argument names for which a value is missing 
	 * @return an Enumeration<String> of the argument names for which a value is missing
	 */
	public Enumeration<String> listMissingArgNames() {
		return missingArgs.elements();
	}
	
	
	/**
	 * This method adds a callback argument and overwrites any previous values.
	 * @param val the callback
	 * @param name the name of the argument for which the value is to be added
	 * @throws ConfigurationException if the given callback object is null or the argument
	 * <code>name</code> isnt of type callback
	 */
	public void addArgumentCallback(String name, StreamCallback val) throws ConfigurationException{
		if(!args.get(name).getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
			logger.error("Type of argument '"+name+"' is '"+args.get(name).getArgType()+"' not 'callback'");
			throw new ConfigurationException();
		}
		if(val==null)
			throw new ConfigurationException("Callback object null");
		callback = val;
		missingArgs.remove(name);
	}
	
	/**
	 * This method adds the value for a float argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ConfigurationException if the argument <code>name</code> isnt of type float
	 */
	public void addArgumentValueFloat(String name, float val) throws ConfigurationException{
		if(!args.get(name).getArgType().equals(CMLConstants.ARG_TYPE_FLOAT)) {
			logger.error("Type of argument '"+name+"' is '"+args.get(name).getArgType()+"' not 'float'");
			throw new ConfigurationException();
		}		
		addValue(name, String.valueOf(val));
	}
	
	/**
	 * This method adds the value for an integer argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ConfigurationException if the argument <code>name</code> isnt of type int
	 */
	public void addArgumentValueInt(String name, int val) throws ConfigurationException{
		if(!args.get(name).getArgType().equals(CMLConstants.ARG_TYPE_INT)) {
			logger.error("Type of argument '"+name+"' is '"+args.get(name).getArgType()+"' not 'int'");
			throw new ConfigurationException();
		}
		addValue(name, String.valueOf(val));
	}
	
	/**
	 * This method adds the value for an string argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ConfigurationException if the argument <code>name</code> isnt of type string
	 */
	public void addArgumentValueString(String name, String val) throws ConfigurationException{
		if(!args.get(name).getArgType().equals(CMLConstants.ARG_TYPE_STRING)) {
			logger.error("Type of argument '"+name+"' is '"+args.get(name).getArgType()+"' not 'string'");
			throw new ConfigurationException();
		}		
		addValue(name, String.valueOf(val));
	}
	
	/**
	 * This method adds the value for an argument and overwrites any previous values.
	 * The value is passed as a string and converted to the given type for checking
	 * before being added. CallBack types are not accepted
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ConfigurationException if the value cant be converted, the argument cant be found or is of type callback
	 */
	public void addArgumentValue(String name, String val) throws ConfigurationException{
		ArgTypes t;
		if((t= args.get(name))==null){
			logger.debug("Cant find argument '"+name+"'");
			throw new ConfigurationException();
		} else if(t.getArgType().equals(CMLConstants.ARG_TYPE_FLOAT))
			addArgumentValueFloat(name, Float.parseFloat(val));				
		else if(t.getArgType().equals(CMLConstants.ARG_TYPE_INT))
			addArgumentValueInt(name, Integer.parseInt(val));	
		else if(t.getArgType().equals(CMLConstants.ARG_TYPE_STRING))
			addArgumentValueString(name, val);
		else if(t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
			logger.error("Given an argument of type CALLBACK");
			throw new ConfigurationException();
		} else {
			logger.error("Unknown argument type '"+t.getArgType()+"'");
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method creates a new Command object from the command template.
	 * The new object is instanciated only if all the arguments have been assigned a value.
	 * @return a new Command object
	 * @throws ConfigurationException if some of the arguments dont have a value
	 */
	public Command getCommand() throws ConfigurationException{
		//Make sure we have all the args and their values
		String name;
		ArgTypes t;
		Enumeration<String> e = args.keys();
		while(e.hasMoreElements()) {
			name = e.nextElement();
			t = args.get(name); 
			if(t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK) && callback==null) {
				logger.error("We are missing the callback object to create the command");
				throw new ConfigurationException("CallBack object missing");
			} else if(!t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK) && argValues.get(name)==null) {
				logger.error("We are missing the value for argument '"+name+"' to create the command");
				throw new ConfigurationException("value for argument '"+name+"' missing");
			}
		}
		return new Command(cid, argValues, callback);
	}
	
	/**
	 * this methods adds a value to an argument
	 * @param name the argument
	 * @param value the value
	 */
	private void addValue(String name, String val){
		argValues.put(name, val);
		missingArgs.remove(name);
	}
	
	/**
	 * This method parse the given command description document and extracts the argument types and argNames
	 * @param d the CML command description document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseArguments(Document d, int cid) throws ConfigurationException {
		int nbArgs;
		ArgTypes t;
		String n;
		String xpath_base=CMLConstants.XPATH_CMD_DESC+"[@"+CMLConstants.CID_ATTRIBUTE+"=\""+cid+"\"]/"+CMLConstants.ARGUMENTS_TAG+"/"+CMLConstants.ARGUMENT_TAG;
		//Check that we have a description for this CID in the command description doc
		try {
			XMLhelper.getTextValue(CMLConstants.XPATH_CMD_DESC+"[@"+CMLConstants.CID_ATTRIBUTE+"=\""+cid+"\"]", d);
		} catch (XPathExpressionException e) {
			logger.error("Cant find the CID '"+cid+"' in the command description");
			throw new ConfigurationException();
		}
		
		//Check how many arguments this command needs
		String xpath="count("+xpath_base+")";
		try {
			nbArgs = Integer.parseInt(XMLhelper.getTextValue(xpath, d));
		} catch (NumberFormatException e) {
			logger.error("Cant count how many arguments are needed for this command");
			throw new ConfigurationException();
		} catch (XPathExpressionException e) {
			logger.error("Cant find the arguments in CMLdescriptor XML doc");
			throw new ConfigurationException();
		}
		
		//Fetch the arguments and put them together with the arg name in  the hashtable
		for(int i=0; i<nbArgs; i++) {
			try {
				t = new ArgTypes(XMLhelper.getAttributeFromName(xpath_base+"["+(i+1)+"]", CMLConstants.TYPE_ATTRIBUTE, d));
			} catch (Exception e) {
				logger.error("Cant parse the argument type for argument "+i+" in CMLdescriptor XML doc");
				throw new ConfigurationException();
			}
			
			try {
				n = XMLhelper.getAttributeFromName(xpath_base+"["+(i+1)+"]", CMLConstants.NAME_ATTRIBUTE, d);
			} catch (Exception e) {
				logger.error("Cant parse the argument name for argument "+i+" in CMLdescriptor XML doc");
				throw new ConfigurationException();
			}
			args.put(n,t);
			missingArgs.add(n);
		}
		this.cid = cid;
	}
	
	/**
	 * This method parse the given command instance document and extracts the argument values. If the instance document
	 * contains a value for a callback-type argument, it is ignored.
	 * @param d the CML command description document
	 * @throws ConfigurationException if the document cant be parsed
	 */
	private void parseArgumentValues(Document d) throws ConfigurationException {
		String val = null, name;
		ArgTypes t;
		Enumeration<String> e = args.keys();
		while(e.hasMoreElements()) {
			name = e.nextElement();
			t = args.get(name);
			if(!t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
				try {
					val = XMLhelper.getTextValue(CMLConstants.XPATH_CMD_INST_ARGUMENT+"[@"+CMLConstants.NAME_ATTRIBUTE+"=\""+name+"\"]", d);
					addArgumentValue(name, val);
				} catch (XPathExpressionException e1) {
					//logger.debug("Cant find the value for argument '"+name+"' in the command instance CML doc");
				} catch (NumberFormatException e2) {
					logger.error("The string '"+val+"'for argument '"+name+"' cant be parsed to a "+t.getArgType());
				}
			}
		}
	}
}
