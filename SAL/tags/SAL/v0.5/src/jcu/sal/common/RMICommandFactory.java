package jcu.sal.common;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class is an adapter wrapped around a CommandFactory object, which adds code to handle RMIStreamCallback objects
 * (instead of StreamCallback).
 * @author gilles
 *
 */
public class RMICommandFactory {
	private static Logger logger = Logger.getLogger(RMICommandFactory.class);
	static {Slog.setupLogger(logger);}

	private CommandFactory factory;
	private Map<String, List<String>> callbackNames;
	private CMLDescription cml;
	
//	/**
//	 * Create a RMI command template and set the command arguments to the values in the command instance document.
//	 * The command ID used is the one specified in the command instance document.
//	 * @param desc the CML descriptions document
//	 * @param inst the command instance document
//	 * @return whether or not some arguments are missing
//	 */
//	public RMICommandFactory(Document desc, Document inst)  throws ConfigurationException{
//		Slog.setupLogger(logger);
//		cml = new CMLDescriptions(desc).getDescription(CommandFactory.getCIDFromInstance(inst));
//		factory = new CommandFactory(removeCallbacks(),inst);
//
//	}
	
	/**
	 * Create a empty RMI command template based on the CML descriptions document 
	 * and the given command id
	 * @param desc the command descriptions document
	 * @param cid the command id
	 * @throws ConfigurationException if the given XML document is not a valid CML document
	 */
	public RMICommandFactory(Document desc, int cid) throws ConfigurationException{
		this(new CMLDescriptions(desc).getDescription(cid));
	}
	
	/**
	 * This constructor creates a empty RMI command template based on the CML description document object
	 * @param c the command description object
	 * @throws ConfigurationException if the given XML document is not a valid CML document
	 */
	public RMICommandFactory(CMLDescription c) throws ConfigurationException{
		cml = c;
		factory = new CommandFactory(removeCallbacks());
	}
	
	/**
	 * This method checks our CML description object to see if there are any callback arguments. If there are, this method returns 
	 * a copy of our CML description document in which all callback arguments have been removed.  
	 * @return a copy of our CML description document in which callback arguments have been removed.
	 */
	private CMLDescription removeCallbacks() {
		callbackNames = new Hashtable<String, List<String>>();
		
		//Check if we have a callback function
		List<ArgumentType> types = cml.getArgTypes();
		try {
			if(types.contains(new ArgumentType(CMLConstants.ARG_TYPE_CALLBACK))) {
				List<String> names = cml.getArgNames();
				for (int i = 0; i < types.size(); i++) {
					if(types.get(i).getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
						types.remove(i);
						callbackNames.put(names.remove(i), new Vector<String>(2));
					}
				}
				return new CMLDescription("",cml.getCID(),cml.getName(),cml.getDesc(),types,names, cml.getReturnType());
			}
		}catch (ConfigurationException e) {
			logger.error("We shoudnt be here !!");
			e.printStackTrace();
		}
		
		return cml;
	}
	
	/**
	 * This method returns the type of a given argument
	 * @param name the name of the argument
	 * @return the type of the argument
	 * @throws ConfigurationException 
	 * @throws ConfigurationException if the argument cant be found
	 */
	public ArgumentType getArgType(String name) throws ConfigurationException{
		ArgumentType t;
		try {
			t = factory.getArgType(name);
		} catch (ConfigurationException e) {
			if(!callbackNames.containsKey(name))
				throw new ConfigurationException();
			else
				t = new ArgumentType(CMLConstants.ARG_TYPE_CALLBACK);
		}		
		return t;
			
	}	
	
	/**
	 * This method returns an Enumeration<String> of the argument names for which a value is missing 
	 * @return an Enumeration<String> of the argument names for which a value is missing
	 */
	public List<String> listMissingArgNames() {
		List<String> l = factory.listMissingArgNames();
		Iterator<String> i = callbackNames.keySet().iterator();
		while(i.hasNext())
			l.add(i.next());
		return l;
	}
	
	
	/**
	 * This method adds a callback argument and overwrites any previous values.
	 * @param name the name of the callback argument (as per CML description)
	 * @param rmiName the name of the RMI client as previously registered with RMISALAgent.registerClient().
	 * @param objName the name of the RMI StreamCallback to lookup in the RMI registry.
	 * <code>name</code> isnt of type callback
	 */
	public void addArgumentCallback(String name, String rmiName, String objName) throws ConfigurationException{
		if(callbackNames.containsKey(name)){
			if(callbackNames.get(name).size()==0) {
				callbackNames.get(name).add(rmiName);
				callbackNames.get(name).add(objName);
			} else
				logger.debug("There was a previous value for the callback argument "+name);
		} else {
			logger.error("no callback arguments called "+name);
			throw new ConfigurationException();
		}
	}
	
	/**
	 * This method adds the value for a float argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ConfigurationException if the argument <code>name</code> isnt of type float
	 */
	public void addArgumentValueFloat(String name, float val) throws ConfigurationException{
		factory.addArgumentValueFloat(name, val);
	}
	
	/**
	 * This method adds the value for an integer argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ConfigurationException if the argument <code>name</code> isnt of type int
	 */
	public void addArgumentValueInt(String name, int val) throws ConfigurationException{
		factory.addArgumentValueInt(name, val);
	}
	
	/**
	 * This method adds the value for an string argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ConfigurationException if the argument <code>name</code> isnt of type string
	 */
	public void addArgumentValueString(String name, String val) throws ConfigurationException{
		factory.addArgumentValueString(name, val);
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
		factory.addArgumentValue(name, val);
	}
	
	/**
	 * This method creates a new Command object from the command template.
	 * The new object is instanciated only if all the arguments have been assigned a value.
	 * @return a new Command object
	 * @throws ConfigurationException if some of the arguments dont have a value
	 */
	public RMICommand getCommand() throws ConfigurationException{
		//Make sure we have all the callbacks
		Iterator<String> i = callbackNames.keySet().iterator();
		String n;
		while(i.hasNext()){
			n = i.next();
			List<String> names = callbackNames.get(n);
			if(names.size()!=2) {
				logger.debug("Callback "+n+" doesnt have values" );
				throw new ConfigurationException();
			}				
		}
		
		return new RMICommand(factory.getCommand(), callbackNames);
	}

	/**
	 * Objects of this class represent a SAL command when using the RMI version.
	 * An RMICommandFactory is responsible for instanciating these objects.
	 * @author gilles
	 *
	 */
	public static class RMICommand implements Serializable{
		private static final long serialVersionUID = 6054676797304225967L;
		private Map<String,List<String>> callbacks;
		private Command c;

		private RMICommand(Command c, Map<String,List<String>> RMIcallbacks){
			this.c = c;
			callbacks = RMIcallbacks;
		}

		/**
		 * This method returns a list of string representing an RMI callback argument.
		 * The string at position 0 in the list is the rmiName (the name the Client used when calling RMISALAgent.registerClient()). 
		 * The string at position 1 in the list is the objName (the name of the object in the RMI registry representing the callback object).
		 * @param name the name of the callback argument
		 * @return
		 */
		public Map<String,List<String>> getRMIStreamCallBack(){
			return new Hashtable<String,List<String>>(callbacks);
		}

		
		public String getConfig(String directive) throws BadAttributeValueExpException {
			return c.getConfig(directive);
		}
		
		Command getCommand(){
			return c;
		}
		
		public int getCID(){
			return c.getCID();
		}
		
		public void dumpCommand() {
			c.dumpCommand();
		}

		public String getValue(String name){
			return c.getValue(name);
		}
	}
}
