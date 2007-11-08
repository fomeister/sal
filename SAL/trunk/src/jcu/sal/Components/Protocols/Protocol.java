/**
 * @author gilles
 */
package jcu.sal.Components.Protocols;

import java.util.Hashtable;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.Command;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Sensors.Sensor;


/**
 * @author gilles
 *
 */
public abstract class Protocol extends AbstractComponent<ProtocolID> {

	public static final String PROTOCOLPARAMNAME_TAG = "name";
	public static final String PROTOCOLPARAM_TAG = "Param";
	public static final String PROTOCOLTYPE_TAG = "type";
	public static final String PROTOCOLNAME_TAG = "name";
	public static final String PROTOCOL_TAG="Protocol";
	/**
	 * 
	 */
	public Protocol(ProtocolID i, String t, Hashtable<String,String> c) {
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
		return "Protocol "+id.getName()+"("+type+")";
	}
	
	/**
	 * Sends a command to a sensor
	 * @param c the command
	 * @param s the sensor
	 * @return the result
	 */
	public abstract String execute(Command c, Sensor s);
	
	/**
	 * Check whether all the sensors are connected, and change their status accordingly
	 */
	public abstract void probeSensors();
	
}
