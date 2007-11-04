/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.util.Enumeration;

import javax.comm.CommPortIdentifier;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class SerialEndPoint extends EndPoint {

	private Logger logger = Logger.getLogger(SerialEndPoint.class);
	
	/**
	 * 
	 */
	public SerialEndPoint() {
		super();
		Slog.setupLogger(this.logger);
		this.logger.error("ctor SerialEndPoint");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws RuntimeException {
		// Check if we have this serial port on this platform
		this.logger.debug("check if we have the required serial port");
		try {
			Enumeration e = CommPortIdentifier.getPortIdentifiers();
			while(e.hasMoreElements()) {
				CommPortIdentifier portId = (CommPortIdentifier) e.nextElement();
				this.logger.debug("Found serial port: " +portId.getName());
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not setup the serial port");
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove() {
		//Not much to do here...
		this.logger.debug("Removing serial Endpoint.");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#start()
	 */
	@Override
	public void start() {
		if(configured && !started) {
			this.logger.debug("Starting serial Endpoint.");
			started=true;
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		if(started) {
			this.logger.debug("Stopping serial Endpoint.");
			started=false;
		}
	}
}
