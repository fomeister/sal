package jcu.sal.common;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.exceptions.ArgumentNotFoundException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * CommandFactory objects are used to create correct Command objects which can then be given to SAL for execution.
 * To construct a Command object, you create a CommandFactory object with a CML document and a command ID. (The CML
 * document for a sensor can be retrieved using the <code>SALAgent.getCML()</code> method.). In order to produce a properly
 * formatted command, all the required command arguments must be supplied (as specified in the CML document). For that,
 * you iterate over a list of names of arguments which are still missing a value (the iterator is returned by 
 * <code>listMissingArgNames()</code>). For each name, you find out about the type of the missing value using
 * <code>getArgType(name)</code>. You can then supply the value using one of the 
 * <code>addArgumentValue{Int,String,Callback,Float}()</code> methods. You can also use <code>addArgumentValue()</code> and let
 * the CommandFactory do the type conversion for you. If you do, make sure you handle any returned exception.<br>
 * Once all arguments have been given a value, you can get an instance of a Command object by invoking <code>getCommand()</code>
 * @author gilles
 *
 */
public class CommandFactory {
	private static Logger logger = Logger.getLogger(CommandFactory.class);
	static {
		Slog.setupLogger(logger);
	}

	private Map<String, String> argValues;
	private CMLDescription cml;
	private List<String> missingArgs;
	private Map<String,StreamCallback> callback;
	
	
	/**
	 * This constructor creates an empty command template based on the CML descriptions document 
	 * and the given command id
	 * @param desc the command description document
	 * @param cid the command id
	 * @throws SALDocumentException if the given CML description document is not a valid CML document
	 * @throws NotFoundException if the command id doesnt exist in the CML document
	 */
	public CommandFactory(Document desc, int cid) throws ConfigurationException, NotFoundException, SALDocumentException {
		argValues = new Hashtable<String, String>();
		missingArgs = new Vector<String>();
		callback = new Hashtable<String, StreamCallback>();
		cml = new CMLDescriptions(desc).getDescription(cid);
		parseArguments(desc, cid);
	}
	
	/**
	 * This constructor creates a empty command template based on the CML description document object
	 * @param desc the command description document
	 */
	public CommandFactory(CMLDescription desc){
		argValues = new Hashtable<String, String>();
		missingArgs = new Vector<String>();
		callback = new Hashtable<String, StreamCallback>();
		cml = desc;
		missingArgs = cml.getArgNames();
	}
	
	/**
	 * This method returns the type of a given argument
	 * @param name the name of the argument
	 * @return the type of the argument
	 * @throws ArgumentNotFoundException if the argument cant be found
	 */
	public ArgumentType getArgType(String name) {
		return cml.getArgType(name);
	}	
	
	/**
	 * This method returns an Enumeration<String> of the argument names for which a value is missing 
	 * @return an Enumeration<String> of the argument names for which a value is missing
	 */
	public List<String> listMissingArgNames() {
		return new Vector<String>(missingArgs);
	}
	
	
	/**
	 * This method adds a callback argument and overwrites any previous values.
	 * @param name the name of the argument for which the value is to be added
	 * @param val the callback
	 * @throws SALRunTimeException 
	 * @throws ArgumentNotFoundException if no argument matches the given name, 
	 * if the given callback object is null or the argument
	 * <code>name</code> isnt of type callback
	 */
	public void addArgumentCallback(String name, StreamCallback val){
		if(!cml.getArgType(name).equals(ArgumentType.CallbackArgument)) {
			logger.error("Type of argument '"+name+"' is '"+cml.getArgType(name).getArgType()+"' not 'callback'");
			throw new ArgumentNotFoundException("Type of argument '"+name+"' is '"+cml.getArgType(name).getArgType()+"' not 'callback'");
		}
		if(val==null)
			throw new ArgumentNotFoundException("Callback object null");
		if(callback.containsKey(name))
			logger.debug("A previous value exists for callback "+name+" - will be overwritten");
		callback.put(name, val);
		missingArgs.remove(name);
	}
	
	/**
	 * This method adds the value for a float argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name or
	 * if the argument <code>name</code> isnt of type float
	 */
	public void addArgumentValueFloat(String name, float val) {
		if(!cml.getArgType(name).equals(ArgumentType.FloatArgument)) {
			logger.error("Type of argument '"+name+"' is '"+cml.getArgType(name).getArgType()+"' not 'float'");
			throw new ArgumentNotFoundException("Type of argument '"+name+"' is '"+cml.getArgType(name).getArgType()+"' not 'float'");
		}		
		addValue(name, String.valueOf(val));
	}
	
	/**
	 * This method adds the value for an integer argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name or
	 * if the argument <code>name</code> isnt of type int
	 */
	public void addArgumentValueInt(String name, int val) {
		if(!cml.getArgType(name).equals(ArgumentType.IntegerArgument)) {
			logger.error("Type of argument '"+name+"' is '"+cml.getArgType(name).getArgType()+"' not 'int'");
			throw new ArgumentNotFoundException("Type of argument '"+name+"' is '"+cml.getArgType(name).getArgType()+"' not 'int'");
		}
		addValue(name, String.valueOf(val));
	}
	
	/**
	 * This method adds the value for an string argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name or
	 * if the argument <code>name</code> isnt of type string
	 */
	public void addArgumentValueString(String name, String val) {
		if(!cml.getArgType(name).equals(ArgumentType.StringArgument)) {
			logger.error("Type of argument '"+name+"' is '"+cml.getArgType(name).getArgType()+"' not 'string'");
			throw new ArgumentNotFoundException("Type of argument '"+name+"' is '"+cml.getArgType(name).getArgType()+"' not 'string'");
		}		
		addValue(name, val);
	}
	
	/**
	 * This method adds the value for an argument and overwrites any previous values.
	 * The value is passed as a string and converted to the given type for checking
	 * before being added. <b>CallBack types are not accepted, use <code>addArgumentCallback()</code> instead.<b>
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name or
	 * if the value cant be converted, the argument cant be found or is of type callback
	 */
	public void addArgumentValue(String name, String val){
		ArgumentType t = cml.getArgType(name);
		if(t.equals(ArgumentType.FloatArgument)){
			try {
				addArgumentValueFloat(name, Float.parseFloat(val));
			} catch (NumberFormatException nfe){
				logger.error("Cant convert '"+val+"' to float");
				throw new ArgumentNotFoundException("Cant convert '"+val+"' to float");
			}
		} else if(t.equals(ArgumentType.IntegerArgument)){
			try {
			addArgumentValueInt(name, Integer.parseInt(val));
			} catch (NumberFormatException nfe){
				logger.error("Cant convert '"+val+"' to integer");
				throw new ArgumentNotFoundException("Cant convert '"+val+"' to integer");
			}
		} else if(t.equals(ArgumentType.StringArgument))
			addArgumentValueString(name, val);
		else if(t.equals(ArgumentType.CallbackArgument)) {
			logger.error("Given an argument of type CALLBACK");
			throw new ArgumentNotFoundException("Given an argument of type CALLBACK - use addArgumentCallback() instead");
		} else {
			logger.error("Unknown argument type '"+t.getArgType()+"'");
			throw new ArgumentNotFoundException("Unknown argument type '"+t.getArgType()+"'");
		}
	}
	
	/**
	 * This method creates a new Command object from the command template.
	 * The new object is instantiated only if all the arguments have been assigned a value.
	 * @return a new Command object
	 * @throws ConfigurationException if some of the arguments dont have a value
	 */
	public Command getCommand() throws ConfigurationException{
		//Make sure we have all the args and their values
		ArgumentType t;

		for(String name: cml.getArgNames()){
			t = cml.getArgType(name);
			if(t.equals(ArgumentType.CallbackArgument) && callback==null) {
				logger.error("Callback object missing");
				throw new ConfigurationException("CallBack object missing");
			} else if(!t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK) && argValues.get(name)==null) {
				logger.error("Value for argument '"+name+"' missing");
				throw new ConfigurationException("value for argument '"+name+"' missing");
			}
		}
		return new Command(cml.getCID().intValue(), argValues, callback);
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
	
//	/**
//	 * This method takes a CML command instance document and extracts the command ID from it.
//	 * @param inst the CML command instance document
//	 * @return the CID
//	 * @throws ConfigurationException if the instance cant be parsed.
//	 */
//	static int getCIDFromInstance(Document inst) throws ConfigurationException{
//		try {
//			return Integer.parseInt(XMLhelper.getAttributeFromName(CMLConstants.XPATH_CMD_INST, CMLConstants.CID_ATTRIBUTE, inst));
//		} catch (Exception e) {
//			logger.error("Cant parse the cid in CML instance XML doc");
//			throw new ConfigurationException();
//		}
//	}
	
	/**
	 * This method parse the given command description document and extracts the argument types and argNames
	 * @param d the CML command description document
	 * @throws ConfigurationException if the document cant be parsed
	 * @throws SALDocumentException if the CML document is invalid
	 * @throws NotFoundException if the command id doesnt exist in the CML document
	 */
	void parseArguments(Document d, int cid) throws SALDocumentException, NotFoundException {
		cml = new CMLDescriptions(d).getDescription(cid);
		missingArgs = cml.getArgNames();
	}
	
//	/**
//	 * This method parse the given command instance document and extracts the argument values. If the instance document
//	 * contains a value for a callback-type argument, it is ignored.
//	 * @param d the CML command description document
//	 * @throws ConfigurationException if the document cant be parsed
//	 */
//	private void parseArgumentValues(Document d) throws ConfigurationException {
//		String val = null;
//		ArgumentType t;
//		for(String name: cml.getArgNames()){
//			t = cml.getArgType(name);
//			if(!t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
//				try {
//					val = XMLhelper.getTextValue(CMLConstants.XPATH_CMD_INST_ARGUMENT+"[@"+CMLConstants.NAME_ATTRIBUTE+"=\""+name+"\"]", d);
//					addArgumentValue(name, val);
//				} catch (NumberFormatException e2) {
//					logger.error("The string '"+val+"'for argument '"+name+"' cant be parsed to a "+t.getArgType());
//				} catch (NotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	public int getCID(){
		return cml.getCID().intValue();
	}
	
	
	public static Command getCommand(Command c, Map<String,StreamCallback> cb) {
		return new Command(c, cb);
	}
	
	public static Command stripCallbacks(Command c){
		c.streamc = null;
		return c;
	}
	
	public static class Command implements Serializable{

		private static final long serialVersionUID = -8361578743898576812L;
		private int cid;
		private static Logger logger = Logger.getLogger(Command.class);
		static{
			Slog.setupLogger(logger);
		}
		private Map<String,String> parameters;
		private Map<String,StreamCallback> streamc;

		private Command(int cid, Map<String, String> values, Map<String,StreamCallback> c){
			this.cid = cid;
			parameters = values;
			streamc = c;		
		}
		
		private Command(Command cmd, Map<String,StreamCallback> c){
			this(cmd.cid, cmd.parameters, c);
		}

		
		public StreamCallback getStreamCallBack(String name) {
			if(!streamc.containsKey(name))
				throw new ArgumentNotFoundException("No matching argument for name '"+name+"'");
			
			return streamc.get(name);
		}
		
		public Map<String,StreamCallback> getStreamCallBack() {
			return new Hashtable<String,StreamCallback>(streamc);
		}
		
		public Map<String,String> getParameters(){
			return new Hashtable<String,String>(parameters);
		}
		
		public int getCID(){
			return cid; 
		}

		public String getValue(String name){
			return parameters.get(name);
		}
		
		/**
		 * This method returns the value of a config directive as an integer, given its name.
		 * @param name the name of the config directive
		 * @return the int value of the config directive
		 * @throws NumberFormatException if the value of the directive cannot be parsed to int.
		 */
		public int getIntValue(String name){
			return Integer.valueOf(parameters.get(name));
		}
	}
}
