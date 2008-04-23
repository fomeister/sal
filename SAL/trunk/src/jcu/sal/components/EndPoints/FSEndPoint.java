/**
 * 
 */
package jcu.sal.components.EndPoints;

import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.utils.Slog;
import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class FSEndPoint extends EndPoint {

	private Logger logger = Logger.getLogger(FSEndPoint.class);
	public static final String FSENDPOINT_TYPE = "fs";
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public FSEndPoint(EndPointID i, Hashtable<String,String> c) throws ConfigurationException {
		super(i,FSENDPOINT_TYPE ,c);
		Slog.setupLogger(this.logger);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws ConfigurationException {
		// Not much to do here 
		this.logger.debug("Found filesystem");
		this.configured = true;
	}
	
	public static void main(String[] args) throws ConfigurationException {
		/* tries building a new FSEndPoint */
		new FSEndPoint(new EndPointID("fs"), new Hashtable<String,String>());
	}
}
