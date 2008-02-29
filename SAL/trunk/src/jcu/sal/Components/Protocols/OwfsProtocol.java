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
import jcu.sal.Components.Sensors.Sensor;
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
	public final static String OWFSPROTOCOL_TYPE = "owfs";
	public final static String OWFSLOCATIONATTRIBUTE_TAG = "Location";
	public final static String OWFSMOUNTPOINTATTRIBUTE_TAG = "MountPoint";

	static { 
		Slog.setupLogger(logger);
		SUPPORTED_ENDPOINT_TYPES.add("usb");
		SUPPORTED_ENDPOINT_TYPES.add("serial");
	}
	
	
	/**
	 * Construct the OwfsProtocol object. (parseConfig is called in super())
	 * @throws ConfigurationException if there is a problem with the component's config
	 */
	public OwfsProtocol(ProtocolID i, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super(i,OWFSPROTOCOL_TYPE ,c,d);
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
			
			//concurrent instances of owfs cant coexist 
			//Check whether instances of owfs are running
			if(!ProcessHelper.getPid("owfs").isEmpty()){
				logger.error("An instance of owfs seems to be running");
				throw new BadAttributeValueExpException("OWFS already running");
			}

			//Next, we check that OWFS is installed in the given directory
			logger.debug("Detecting OWFS version");
			BufferedReader[] b = ProcessHelper.captureOutputs(getConfig(OWFSLOCATIONATTRIBUTE_TAG) + " --version");
			while((temp = b[0].readLine()) != null) logger.debug(temp);
			while((temp = b[1].readLine()) != null) logger.debug(temp);
			
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
	protected void internal_start() throws ConfigurationException{
		logger.debug("OWFS internal start");
		
		try {
			BufferedReader r[] = ProcessHelper.captureOutputs(config.get(OwfsProtocol.OWFSLOCATIONATTRIBUTE_TAG)+" -uall "+config.get(OwfsProtocol.OWFSMOUNTPOINTATTRIBUTE_TAG));
			//check the stdout and stderr
			if(r[1].)
			
			
			//Check that it actually started ...
			if(ProcessHelper.getPid("owfs").isEmpty()){
				logger.error("Starting OWFS command failed with:");
				System.out.println(r[0]);
				System.out.println(r[1]);
				throw new ConfigurationException();
			}
			
		} catch (IOException e) {
			logger.error("Coudlnt run the OWFS process");
			throw new ConfigurationException();
		}
		
		// TODO call probeSensors

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_stop()
	 */
	protected void internal_remove() {
		logger.debug("OWFS internal removed");
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
		OwfsProtocol o = new OwfsProtocol(new ProtocolID("owfs"), c, d);
		o.dumpConfig();
		//o.remove();
	}


	@Override
	public boolean isSensorSupported(Sensor sensor) {
		//TODO check the sensor family and make sure it is supported
		if(sensor.getNativeAddress().substring(0, 3)=="10.")
			return true;
		else
			return false;
	}

	@Override
	public boolean probeSensor(Sensor s) {
		// TODO complete this method
		String f = new String(config.get(OwfsProtocol.OWFSMOUNTPOINTATTRIBUTE_TAG)+"/"+s.getNativeAddress());
		try {
			if(ProcessHelper.isFileReadable(f)) {
				s.enable();
				return true;
			}
		} catch (Exception e) {
			logger.error("couldnt probe sensor "+s.toString()+". Raised exception: "+e.getMessage());
		}
		s.disable();
		return false;
	}

}
