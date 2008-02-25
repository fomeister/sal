/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.utils.Slog;
import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class FSEndPoint extends EndPoint {

	private Logger logger = Logger.getLogger(FSEndPoint.class);
	private static final String FSENDPOINT_TYPE = "fs";
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public FSEndPoint(EndPointID i, Hashtable<String,String> c) throws ConfigurationException {
		super(i,FSENDPOINT_TYPE ,c);
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor FSEndPoint");
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws ConfigurationException {
		// Not much to do here 
		this.logger.debug("Found filesystem");
		this.configured = true;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove() {
		//Not much to do here...
		this.logger.debug("Removing Filesystem Endpoint.");
		if(started)
			stop();
		this.logger.debug("Filesystem Endpoint removed");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#start()
	 */
	@Override
	public void start() {
		if(configured && !started) {
			this.logger.debug("Starting Filesystem Endpoint.");
			started=true;
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		if(started) {
			this.logger.debug("Stopping Ethernet Endpoint.");
			started=false;
		}
	}
	
	public static void main(String[] args) throws ConfigurationException {
		/* tries building a new FSEndPoint */
		new FSEndPoint(new EndPointID("fs"), new Hashtable<String,String>());
	}
}
