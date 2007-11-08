package jcu.sal.Components;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;


public class Command {
	
	private Logger logger = Logger.getLogger(Protocol.class);
	private Hashtable<String,String> parameters;
	//private int cid;
	public static final String COMMAND_TAG = "Command";
	public static final String PARAMETER_TAG = "Param";
	
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

	}
	
	
}
