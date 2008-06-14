package jcu.sal.common;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.ArgTypes;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class is an adapter wrapped around a CommandFactory object, which adds code to handle RMI StreamCallback
 * objects.
 * @author gilles
 *
 */
public class RMICommandFactory {
	private static Logger logger = Logger.getLogger(RMICommandFactory.class);
	static {
		Slog.setupLogger(logger);
	}
	private CommandFactory factory;
	private Map<String, List<String>> callbackNames;
	
	/**
	 * Create a command template based on the given Command's CML descriptor 
	 * using values in the command instance
	 * @param desc the command description document
	 * @param inst the command instance document
	 * @return whether or not some arguments are missing
	 */
	public RMICommandFactory(Document desc, Document inst)  throws ConfigurationException{
		factory = new CommandFactory(checkForCallbacks(desc),inst);
	}
	
	/**
	 * Create a empty command template based on the Command description document 
	 * and the givencommand id
	 * @param desc the command description document
	 * @param cid the command id
	 */
	public RMICommandFactory(Document desc, int cid) throws ConfigurationException{
		factory = new CommandFactory(checkForCallbacks(desc),cid);
	}
	
	private Document checkForCallbacks(Document srcDoc) throws ConfigurationException{
		callbackNames = new Hashtable<String, List<String>>();
		CMLDescription src = new CMLDescription(srcDoc);
		
		//Check if we have a callback function
		List<ArgTypes> types = src.getArgTypes();
		if(types.contains(new ArgTypes(CMLConstants.ARG_TYPE_CALLBACK))) {
			List<String> names = src.getArgNames();
			for (int i = 0; i < types.size(); i++) {
				if(types.get(i).getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
					logger.debug("Found arg of type Callback, removing it");
					types.remove(i);
					callbackNames.put(names.remove(i), new Vector<String>(2));
				}
			}
			return new CMLDescription("",src.getCID(),src.getName(),src.getDesc(),types,names, src.getReturnType()).getCML();
		}
		
		return srcDoc;
	}
	
	/**
	 * This method returns the type of a given argument
	 * @param name the name of the argument
	 * @return the type of the argument
	 * @throws ConfigurationException if the argument cant be found
	 */
	public ArgTypes getArgType(String name) throws ConfigurationException {
		return factory.getArgType(name);
			
	}	
	
	/**
	 * This method returns an Enumeration<String> of the argument names for which a value is missing 
	 * @return an Enumeration<String> of the argument names for which a value is missing
	 */
	public Enumeration<String> listMissingArgNames() {
		return factory.listMissingArgNames();
	}
	
	
	/**
	 * This method adds a callback argument and overwrites any previous values.
	 * @param name the name of the callback argument
	 * @param rmiName the name of the RMI client as previously registered with RMIAgent.registerClient().
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

	
	public static class RMICommand implements Serializable{
		private static final long serialVersionUID = 6054676797304225967L;
		private Map<String,List<String>> callbacks;
		private Command c;
		private static Logger logger = Logger.getLogger(RMICommand.class);
		static{
			Slog.setupLogger(logger);
		}

		private RMICommand(Command c, Map<String,List<String>> RMIcallbacks){
			this.c = c;
			callbacks = RMIcallbacks;
		}

		/**
		 * This method returns a list of string representing an RMI callback argument.
		 * The string at position 0 in the list is the rmiName (the name the Client used when calling RMIAgent.registerClient()). 
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
