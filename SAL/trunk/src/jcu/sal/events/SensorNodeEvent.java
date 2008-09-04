package jcu.sal.events;

import javax.naming.ConfigurationException;

import jcu.sal.common.events.Event;

public class SensorNodeEvent extends Event {
	private static final long serialVersionUID = -7000825364492820746L;
	public static int SENSOR_NODE_ADDED = 1;
	public static int SENSOR_NODE_REMOVED = 2;
	public static int SENSOR_NODE_MASK = (SENSOR_NODE_ADDED | SENSOR_NODE_REMOVED); 
	
	static {
		MAX_TYPE_VALUE = SENSOR_NODE_MASK;
	}
	
	public SensorNodeEvent(int t, String sid, String p) throws ConfigurationException {
		super(t, sid, p);
	}

	public SensorNodeEvent(int t, String sid, String doc, String p) throws ConfigurationException {
		super(t, sid, doc, p);
	}
	
	public String toString(){
		if(type==SENSOR_NODE_ADDED) return super.toString() + " New sensor added";
		else return super.toString() + " Existing sensor removed";
	}

}
