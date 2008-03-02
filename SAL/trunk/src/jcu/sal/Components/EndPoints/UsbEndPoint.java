/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.utils.PlatformHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class UsbEndPoint extends EndPoint {

	private Logger logger = Logger.getLogger(EndPoint.class);
	private static final String USBENDPOINT_TYPE="usb";
	//private UsbEndPoint uep = new UsbEndPoint(new EndPointID(""), ) 
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public UsbEndPoint(EndPointID i, Hashtable<String,String> c) throws ConfigurationException {
		super(i,USBENDPOINT_TYPE,c);
		Slog.setupLogger(this.logger);
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
			BufferedReader b[] = PlatformHelper.captureOutputs("lsusb", true);
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

	public static void main(String[] args) throws ConfigurationException {
		/* tries to build a USB EndPoint */
		new UsbEndPoint(new EndPointID("usb1"), new Hashtable<String,String>());
	}
}
