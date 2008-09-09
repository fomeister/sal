/**
 * 
 */
package jcu.sal.components.EndPoints;

import javax.naming.ConfigurationException;

import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class FSEndPoint extends EndPoint {

	private static Logger logger = Logger.getLogger(FSEndPoint.class);
	static {Slog.setupLogger(logger);}
	public static final String FSENDPOINT_TYPE = "fs";
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public FSEndPoint(EndPointID i, EndPointConfiguration c) throws ConfigurationException {
		super(i,FSENDPOINT_TYPE, c);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Not much to do here 
		//logger.debug("Found filesystem");
		configured = true;
	}
}
