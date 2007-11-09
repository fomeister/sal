/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.utils.ProcessHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class UsbEndPoint extends EndPoint {

	private Logger logger = Logger.getLogger(EndPoint.class);
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public UsbEndPoint(EndPointID i, String t, Hashtable<String,String> c) throws ConfigurationException {
		super(i,t,c);
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor USBEndPoint");
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws ConfigurationException {
		// Check if we have any USB ports on this platform
		this.logger.debug("check if we have USB ports.");
		try {
			BufferedReader b[] = ProcessHelper.captureOutputs("lsusb");
			if(!b[0].readLine().contains("Bus"))
				throw new ConfigurationException("Did not detect USB ports");
			configured = true;
			this.logger.debug("Yes we have. USB EndPoint initialised");
		} catch (IOException e) {
			e.printStackTrace();
			this.logger.debug("Problem capturing output of lsusb");
			throw new ConfigurationException("Did not detect USB ports");
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove() {
		//Not much to do here...
		if(started)
			stop();
		this.logger.debug("USB Endpoint removed");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#start()
	 */
	@Override
	public void start() {
		if(configured && !started) {
			this.logger.debug("Starting USB Endpoint.");
			started=true;
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		if(started) {
			this.logger.debug("Stopping USB Endpoint.");
			started=false;
		}
	}
	
	public static void main(String[] args) throws ConfigurationException {
		/* tries to build a USB EndPoint */
		new UsbEndPoint(new EndPointID("usb"), "usb", new Hashtable<String,String>());
	}
}
