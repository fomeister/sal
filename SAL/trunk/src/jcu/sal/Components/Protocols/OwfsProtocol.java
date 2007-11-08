/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Command;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.ProcessHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class OwfsProtocol extends Protocol {

	private Logger logger = Logger.getLogger(OwfsProtocol.class);
	public final static String OWFSLOCATION_TAG = "Location";
	public final static String OWFSMOUNTPOINT_TAG = "MountPoint";

	static { 
		OWFS_SUPPORTED_ENDPOINTS.add("usb");
		OWFS_SUPPORTED_ENDPOINTS.add("serial");
	}
	
	
	/**
	 * 
	 */
	public OwfsProtocol(ProtocolID i, String t, Hashtable<String,String> c) {
		super(i,t,c);
		Slog.setupLogger(logger);
		//parseConfig can not be called here, and configured can not be set to true here
		//since we are missing our EndPoint, so all of this is differed to setEp()
		
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocols.Protocol#execute(jcu.sal.Components.Command)
	 */
	@Override
	public String execute(Command c, Sensor s) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws ConfigurationException {
		String mtpt, temp;

		logger.debug("Parsing our configuration");
		logger.debug("1st, Check the EndPoint");
		if(!ep.isConfigured() || !OWFS_SUPPORTED_ENDPOINTS.contains(ep.getType())) {
			logger.error("This Protocol has been setup with the wrong enpoint: got endpoint type: " +ep.getType()+", expected: ");
			Iterator<String> iter = OWFS_SUPPORTED_ENDPOINTS.iterator();
			while(iter.hasNext())
				logger.error(iter.next());
			throw new ConfigurationException("Wrong Endpoint type");
		}
			
		logger.debug("2nd Check OWFS software");
		try {
			mtpt = getConfig(OWFSMOUNTPOINT_TAG);
			if(mtpt.length()==0) throw new BadAttributeValueExpException("Empty mount point directive...");
			
			//concurrent instances of owfs can coexist as long as their mount points are different
			//Check whether instances of owfs are using the same mount point as ours
			if(ProcessHelper.getRunningProcessArgs("owfs").containsValue(mtpt)) {
				logger.error("An instance of owfs seems to be using the same mountpoint as ours: " + mtpt);
				throw new BadAttributeValueExpException("Wrong OWFS mount point configuration");
			}

			//Next, we check that OWFS is installed in the given directory
			logger.debug("Detecting OWFS version");
			BufferedReader[] b = ProcessHelper.captureOutputs(getConfig(OWFSLOCATION_TAG) + " --version");
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
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		logger.debug("OWFS internal stopped");

	}


	@Override
	public void probeSensors() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Hashtable<String, String> c = new Hashtable<String, String>();
		c.put(OwfsProtocol.OWFSLOCATION_TAG, "/opt/owfs/bin/owfs");
		c.put(OwfsProtocol.OWFSMOUNTPOINT_TAG, "/mnt/w1");
		OwfsProtocol o = new OwfsProtocol(new ProtocolID("owfs"), "owfs", c);
		o.dumpConfig();
	}

	@Override
	protected void internal_remove() {
		logger.debug("OWFS internal removed");
	}
	
}
