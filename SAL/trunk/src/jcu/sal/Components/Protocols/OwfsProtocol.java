/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.utils.ProcessHelper;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author gilles
 *
 */
public class OwfsProtocol extends Protocol {

	private static Logger logger = Logger.getLogger(OwfsProtocol.class);
	public final static String OWFSLOCATIONATTRIBUTE_TAG = "Location";
	public final static String OWFSMOUNTPOINTATTRIBUTE_TAG = "MountPoint";

	static { 
		Slog.setupLogger(logger);
		SUPPORTED_ENDPOINTS.add("usb");
		SUPPORTED_ENDPOINTS.add("serial");
	}
	
	
	/**
	 * Construct the OwfsProtocol object. (parseConfig is called in super())
	 * @throws ConfigurationException if there is a problem with the component's config
	 */
	public OwfsProtocol(ProtocolID i, String t, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super(i,t,c,d);
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		String mtpt, temp;

		try {
			mtpt = getConfig(OWFSMOUNTPOINTATTRIBUTE_TAG);
			if(mtpt.length()==0) throw new BadAttributeValueExpException("Empty mount point directive...");
			
			//concurrent instances of owfs can coexist as long as their mount points are different
			//Check whether instances of owfs are using the same mount point as ours
			if(ProcessHelper.getRunningProcessArgs("owfs").containsValue(mtpt)) {
				logger.error("An instance of owfs seems to be using the same mountpoint as ours: " + mtpt);
				throw new BadAttributeValueExpException("Wrong OWFS mount point configuration");
			}

			//Next, we check that OWFS is installed in the given directory
			logger.debug("Detecting OWFS version");
			BufferedReader[] b = ProcessHelper.captureOutputs(getConfig(OWFSLOCATIONATTRIBUTE_TAG) + " --version");
			while((temp = b[0].readLine()) != null) logger.debug(temp);
			while((temp = b[1].readLine()) != null) logger.debug(temp);
			
			configured = true;
			logger.debug("OWFS protocol configured");
			
		} catch (IOException e) {
			logger.error("Could NOT run/read owfs");
			e.printStackTrace();
			throw new ConfigurationException("Could NOT run/read owfs");
		} catch (BadAttributeValueExpException e) {
			logger.error("incorrect OWFS configuration directives...");
			e.printStackTrace();
			throw new ConfigurationException("Could not setup OWFS protocol");
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_remove()
	 */
	protected void internal_stop() {
		logger.debug("OWFS internal stop");

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_start()
	 */
	public void internal_start() {
		logger.debug("OWFS internal start");
		// TODO Check that the sensors table has some sensors
		// TODO start owfs with arguments
		// TODO call probeSensors

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_stop()
	 */
	protected void internal_remove() {
		logger.debug("OWFS internal removed");
	}

	/**
	 * Check whether all the sensors are connected, and change their status accordingly
	 */
	public void probeSensors() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, ConfigurationException {
		Document d = XMLhelper.createDocument("<EndPoint name='usb' type='usb' />");
		Hashtable<String, String> c = new Hashtable<String, String>();
		c.put(OwfsProtocol.OWFSLOCATIONATTRIBUTE_TAG, "/opt/owfs/bin/owfs");
		c.put(OwfsProtocol.OWFSMOUNTPOINTATTRIBUTE_TAG, "/mnt/w1");
		OwfsProtocol o = new OwfsProtocol(new ProtocolID("owfs"), "owfs", c, d);
		o.dumpConfig();
		o.remove();
	}

}
