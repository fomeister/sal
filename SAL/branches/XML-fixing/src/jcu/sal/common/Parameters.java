package jcu.sal.common;

import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

public class Parameters {
	private Map<String,Parameter> params;
	
	public Parameters(List<Parameter> p) {
		
	}
	
	
	/**
	 * The ParamType class represent the type of a single parameter. A set of static strings list the permitted types (see
	 * ParamType.*_TYPE). 
	 * @author gilles
	 */
	public static class ParamType{		
		/**
		 * TEXT_TYPE represents a text-based parameter (a simple string of characters)
		 */
		public static String TEXT_TYPE = "t";
		private String type;
		
		/**
		 * This constructor creates a parameter type object. Valid types are represented by the 
		 * ParamType.*_TYPE contants.
		 * @param t the type of the parameter
		 * @throws ConfigurationException if the supplied type is invalid
		 */
		public ParamType(String t) throws ConfigurationException{
			if(!t.equals(TEXT_TYPE))
				throw new ConfigurationException("Parameter type invalid");
			type =t;
		}
		
		/**
		 * this method returns the type associated with this parameter type object (See ParamType.*_TYPE contants)
		 * @return the type associated with this parameter type object
		 */
		public String getType() {
			return type;
		}
	}
	
	/**
	 * The Parameter class encapsulate data related to a single parameter: a name, a value and a type (ParamType)
	 * @author gilles
	 */
	private static class Parameter{
		private String name;
		private String value;
		private ParamType type;
		
		/**
		 * This constructor creates a Parameter object which encpsualtes a name, a value and a type
		 * @param n the name of this parameter
		 * @param v the value associated with this parameter
		 * @param t the type of this parameter
		 */
		public Parameter(String n, String v, ParamType t){
			name = n;
			value = v;
			type = t;
		}
		
		/**
		 * This method returns the name of this parameter
		 * @return the name of this parameter
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * This method returns the value of this parameter
		 * @return the value of this parameter
		 */
		public String getValue() {
			return value;
		}
		
		/**
		 * This method returns the type of this parameter
		 * @return the type of this parameter
		 */
		public ParamType getType() {
			return type;
		}
	}

}
