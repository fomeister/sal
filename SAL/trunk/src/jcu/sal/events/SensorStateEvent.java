package jcu.sal.events;

import jcu.sal.common.events.Event;

public class SensorStateEvent extends Event {

	private static final long serialVersionUID = 6308401724433999891L;
	public static int SENSOR_STATE_CONNECTED = 1;
	public static int SENSOR_STATE_DISCONNECTED = 2;
	public static int SENSOR_STATE_MASK = (SENSOR_STATE_CONNECTED | SENSOR_STATE_DISCONNECTED);
	
	static {
		MAX_TYPE_VALUE = SENSOR_STATE_MASK;
	}
	
	public SensorStateEvent(int t, String sid, String p) {
		super(t, sid, p);
	}

	public SensorStateEvent(int t, String sid, String doc, String p) {
		super(t, sid, doc, p);
	}
	
	public String toString(){
		if(type==SENSOR_STATE_CONNECTED) return super.toString() + " Sensor connected";
		else return super.toString() + "  Sensor disconnected";
	}

}
