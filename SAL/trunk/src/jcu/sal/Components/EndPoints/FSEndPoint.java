/**
 * 
 */
package jcu.sal.Components.EndPoints;

import jcu.sal.utils.Slog;
import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class FSEndPoint extends EndPoint {

	private Logger logger = Logger.getLogger(FSEndPoint.class);
	
	/**
	 * 
	 */
	public FSEndPoint() {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor FSEndPoint");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws RuntimeException {
		// Not much to do here 
		this.logger.debug("Found filesystem");
		this.configured = true;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove() {
		//Not much to do here...
		this.logger.debug("Removing Filesystem Endpoint.");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#start()
	 */
	@Override
	public void start() {
		if(configured && !started) {
			this.logger.debug("Starting Ethernet Endpoint.");
			started=true;
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		if(started) {
			this.logger.debug("Stopping Ethernet Endpoint.");
			started=false;
		}
	}
	
	public static void main(String[] args) {
		FSEndPoint e = new FSEndPoint();
		e.parseConfig();
	}
}
