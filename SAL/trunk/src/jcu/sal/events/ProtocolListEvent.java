package jcu.sal.events;

import javax.naming.ConfigurationException;

public class ProtocolListEvent extends Event {
	public static int PROTOCOL_ADDED = 1;
	public static int PROTOCOL_REMOVED = 2;
	public static int PROTOCOL_MASK = (PROTOCOL_ADDED | PROTOCOL_REMOVED); 
	
	static {
		MAX_TYPE_VALUE = PROTOCOL_MASK;
	}
	
	public ProtocolListEvent(int t, String sid, String p) throws ConfigurationException {
		super(t, sid, p);
	}

	public ProtocolListEvent(int t, String sid, String doc, String p) throws ConfigurationException {
		super(t, sid, doc, p);
	}

}
