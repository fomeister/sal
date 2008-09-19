package jcu.sal.common.events;

import java.io.Serializable;

import jcu.sal.common.exceptions.SALRunTimeException;

public abstract class Event implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1860045513950384541L;
	final protected int type;
	final protected String sourceID;
	final protected String sourceDocument;
	final protected String producer;
	protected static int MAX_TYPE_VALUE = 0;
	
	protected Event(int t, String sid, String p){
		this(t, sid,  null, p);
	}
	
	protected Event(int t, String sid, String doc, String p){
		if(t>MAX_TYPE_VALUE) throw new SALRunTimeException("Type of event greater than MAX_TYPE. Programming error");
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
		return "Event from producer '"+producer+"' about source '"+sourceID+"':";
	}
}
