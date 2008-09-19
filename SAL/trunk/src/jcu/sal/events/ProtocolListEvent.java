package jcu.sal.events;

import jcu.sal.common.events.Event;

public class ProtocolListEvent extends Event {

	private static final long serialVersionUID = 4394564002895838509L;
	public static int PROTOCOL_ADDED = 1;
	public static int PROTOCOL_REMOVED = 2;
	public static int PROTOCOL_MASK = (PROTOCOL_ADDED | PROTOCOL_REMOVED); 
	
	static {
		MAX_TYPE_VALUE = PROTOCOL_MASK;
	}
	
	public ProtocolListEvent(int t, String sid, String p){
		super(t, sid, p);
	}

	public ProtocolListEvent(int t, String sid, String doc, String p) {
		super(t, sid, doc, p);
	}
	
	public String toString(){
		if(type==PROTOCOL_ADDED) return super.toString() + " New protocol added";
		else return super.toString() + " Existing protocol removed";
	}

}
