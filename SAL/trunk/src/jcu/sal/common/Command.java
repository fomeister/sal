package jcu.sal.common;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;

import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


public class Command {
	
	private int cid;
	private Logger logger = Logger.getLogger(AbstractProtocol.class);
	private Hashtable<String,String> parameters;
	private StreamCallback streamc;


	public static final String PARAMETER_TAG = "Param";

	Command(int cid, Hashtable<String, String> values, StreamCallback c){
		this.cid = cid;
		parameters = values;
		streamc = c;		
	}
//	Command(Document d, StreamCallback s) throws ParseException {
//		this(d);
//		if(s==null) {
//			logger.error("Callback object null");
//			throw new ParseException("Callback object null", 0);
//		}
//		streamc = s;
//	}
//	
//	public Command(Document d) throws ParseException {
//		Slog.setupLogger(this.logger);
//		ArrayList<String> xml = null;
//		parameters = new Hashtable<String, String>();
//		String name = null, value = null;
//		
//		try {
//			xml = XMLhelper.getAttributeListFromElements("//" + PARAMETER_TAG, d);
//		} catch (XPathExpressionException e) {
//			this.logger.error("Cannot find parameters for this command");
//			throw new ParseException("Cannot find parameters for this Command", 0);
//		}
//		
//		Iterator<String> iter = xml.iterator();
//		
//		while(iter.hasNext()) {
//			iter.next();
//			name = iter.next();
//			iter.next();
//			value = iter.next();
//			parameters.put(name,value);
//		}
//		
//		logger.debug("Command arguments:");
//		dumpCommand();
//
//	}
//	
	public Command(int cid, String key, String value){
		Slog.setupLogger(this.logger);
		this.cid = cid;
		parameters = new Hashtable<String, String>();
		parameters.put(key,value);
	}
//	
//	public Command(Integer cid, String key, String value, StreamCallback s){
//		this(cid, key, value);
//		if(s==null) {
//			logger.error("Callback object null");
//			//throw new ParseException("Callback object null", 0);
//		}
//		streamc = s;
//	}
	
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
	
	public int getCID(){
		return cid; 
	}
	
	public void dumpCommand() {
		this.logger.debug("Command '"+cid+"' parameters:");
		Enumeration<String> keys = parameters.keys();
		Collection<String> values= parameters.values();
		Iterator<String> iter = values.iterator();
		while ( keys.hasMoreElements() &&  iter.hasNext())
		   this.logger.debug("key: " + keys.nextElement().toString() + " - "+iter.next().toString());
	}

	public String getValue(String name){
		return parameters.get(name);
	}
}
