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
public class PCIEndPoint extends EndPoint {

	private static Logger logger = Logger.getLogger(PCIEndPoint.class);
	static {Slog.setupLogger(logger);};
	public static final String ENDPOINT_TYPE = "pci";
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public PCIEndPoint(EndPointID i, EndPointConfiguration c) throws ConfigurationException {
		super(i,ENDPOINT_TYPE ,c);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Not much to do here 
		//logger.debug("Found PCI bus");
		configured = true;
	}
}
