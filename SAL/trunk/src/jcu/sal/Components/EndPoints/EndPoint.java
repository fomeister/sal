/**
 * @author gilles
 */
package jcu.sal.Components.EndPoints;

import java.util.Hashtable;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.Identifiers.EndPointID;


/**
 * @author gilles
 *
 */
public abstract class EndPoint extends AbstractComponent<EndPointID> {

	public static final String ENDPOINTPARAMNAME_TAG = "name";
	public static final String ENDPOINTPARAM_TAG = "Param";
	public static final String ENDPOINTTYPE_TAG = "type";
	public static final String ENDPOINTNAME_TAG = "name";
	public static final String ENPOINT_TAG="EndPoint";
	/**
	 * 
	 */
	public EndPoint(EndPointID i, String t, Hashtable<String,String> c) {
		super();
		id = i;
		type = t;
		config = c;
	}
	
	/**
	 * returns a textual representation of a End Point's instance
	 * @return the textual representation of the Logical Port's instance
	 */
	public String toString() {
		return "EndPoint "+id.getName()+"("+type+")";
	}
	
}
