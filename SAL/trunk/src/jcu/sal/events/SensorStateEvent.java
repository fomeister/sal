package jcu.sal.events;

import javax.naming.ConfigurationException;

public class SensorStateEvent extends Event {

	public static int SENSOR_STATE_CONNECTED = 1;
	public static int SENSOR_STATE_DISCONNECTED = 2;
	public static int SENSOR_STATE_MASK = (SENSOR_STATE_CONNECTED | SENSOR_STATE_DISCONNECTED);
	
	static {
		MAX_TYPE_VALUE = SENSOR_STATE_MASK;
	}
	
	public SensorStateEvent(int t, String sid, String p) throws ConfigurationException {
		super(t, sid, p);
	}

	public SensorStateEvent(int t, String sid, String doc, String p) throws ConfigurationException {
		super(t, sid, doc, p);
	}

}
