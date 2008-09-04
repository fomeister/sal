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
public class PCIEndPoint extends EndPoint {

	private Logger logger = Logger.getLogger(PCIEndPoint.class);
	public static final String PCIENDPOINT_TYPE = "pci";
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public PCIEndPoint(EndPointID i, Hashtable<String,String> c) throws ConfigurationException {
		super(i,PCIENDPOINT_TYPE ,c);
		Slog.setupLogger(this.logger);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Not much to do here 
		logger.debug("Found PCI bus");
		configured = true;
	}
}
