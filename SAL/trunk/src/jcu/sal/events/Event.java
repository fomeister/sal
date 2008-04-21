package jcu.sal.events;

public abstract class Event {
	final protected int type;
	final protected String sourceID;
	final protected String sourceDocument;
	final protected String producer;
	
	public Event(int t, String sid, String p) {
		type = t;
		sourceID = sid;
		producer = p;
		sourceDocument = null;
	}
	
	public Event(int t, String sid, String doc, String p) {
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
