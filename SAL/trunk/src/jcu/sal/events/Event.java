package jcu.sal.events;

import javax.naming.ConfigurationException;

public abstract class Event {
	final protected int type;
	final protected String sourceID;
	final protected String sourceDocument;
	final protected String producer;
	protected static int MAX_TYPE_VALUE = 0;
	
	protected Event(int t, String sid, String p) throws ConfigurationException{
		this(t, sid, p, null);
	}
	
	protected Event(int t, String sid, String doc, String p) throws ConfigurationException{
		if(t>MAX_TYPE_VALUE) throw new ConfigurationException();
		type = t;
		sourceID = sid;
		producer = p;
		sourceDocument = doc;
	}

	public final String getProducer() {
		return producer;
	}

	public final String getSourceDocument() {
		return sourceDocument;
	}

	public final String getSourceID() {
		return sourceID;
	}

	public final int getType() {
		return type;
	}
	
	public String toString(){
		return "Event regarding source '"+sourceID+"' from producer '"+producer+"'";
	}
}
