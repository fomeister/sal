package jcu.sal.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;


public class Command {
	
	private Logger logger = Logger.getLogger(Protocol.class);
	private Hashtable<String,String> parameters;
	private StreamCallback streamc;

	public static final String COMMAND_TAG = "Command";
	public static final String PARAMETER_TAG = "Param";
	public static final String CIDATTRIBUTE_TAG = "CID";
	
	public Command(Document d, StreamCallback s) throws ParseException {
		this(d);
		if(s==null) {
			logger.error("Callback object null");
			throw new ParseException("Callback object null", 0);
		}
		streamc = s;
	}
	
	public Command(Document d) throws ParseException {
		Slog.setupLogger(this.logger);
		ArrayList<String> xml = null;
		parameters = new Hashtable<String, String>();
		String name = null, value = null;
		
		try {
			xml = XMLhelper.getAttributeListFromElements("//" + PARAMETER_TAG, d);
		} catch (XPathExpressionException e) {
			this.logger.error("Cannot find parameters for this command");
			throw new ParseException("Cannot find parameters for this Command", 0);
		}
		
		Iterator<String> iter = xml.iterator();
		
		while(iter.hasNext()) {
			iter.next();
			name = iter.next();
			iter.next();
			value = iter.next();
			parameters.put(name,value);
		}
		
		logger.debug("Command arguments:");
		dumpCommand();

	}
	
	public Command(Integer cid, String key, String value){
		Slog.setupLogger(this.logger);
		parameters = new Hashtable<String, String>();
		parameters.put(CIDATTRIBUTE_TAG,String.valueOf(cid));
		parameters.put(key,value);
	}
	
	public Command(Integer cid, String key, String value, StreamCallback s){
		this(cid, key, value);
		if(s==null) {
			logger.error("Callback object null");
			//throw new ParseException("Callback object null", 0);
		}
		streamc = s;
	}
	
	public StreamCallback getStreamCallBack(){
		return streamc;
	}

	
	public String getConfig(String directive) throws BadAttributeValueExpException {
		String s = parameters.get(directive);
		if (s==null) {
			this.logger.error("Unable to get a config directive with this name "+ directive);
			throw new BadAttributeValueExpException("Unable to get a config directive with this name "+ directive);
		}			
		return s; 
	}
	
	public Integer getCID() throws BadAttributeValueExpException {
		return Integer.parseInt(getConfig(CIDATTRIBUTE_TAG)); 
	}
	
	public void dumpCommand() {
		this.logger.debug("Command parameters:");
		Enumeration<String> keys = parameters.keys();
		Collection<String> values= parameters.values();
		Iterator<String> iter = values.iterator();
		while ( keys.hasMoreElements() &&  iter.hasNext())
		   this.logger.debug("key: " + keys.nextElement().toString() + " - "+iter.next().toString());
	}

	public Hashtable<String, String> getParameters() {
		return parameters;
	}
	
	
}
