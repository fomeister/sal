/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class UsbEndPoint extends EndPoint {

	private Logger logger = Logger.getLogger(EndPoint.class);
	
	/**
	 * 
	 */
	public UsbEndPoint() {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor USBEndPoint");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws RuntimeException {
		// Check if we have any USB ports on this platform
		this.logger.debug("check if we have USB ports.");
		try {
			Process p = Runtime.getRuntime().exec("lsusb");
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			if(!b.readLine().contains("Bus"))
				throw new RuntimeException("Did not detect USB ports");
			configured = true;
			this.logger.debug("USB EndPoint initialised");
		} catch (IOException e) {
			e.printStackTrace();
			this.logger.debug("Problem capturing output of lsusb");
			throw new RuntimeException("Did not detect USB ports");
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove() {
		//Not much to do here...
		this.logger.debug("Removing USB Endpoint.");
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
}
