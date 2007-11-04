/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.management.BadAttributeValueExpException;

import jcu.sal.utils.Slog;
import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class EthernetEndPoint extends EndPoint {

	public static final String ETHDEVICEATTRIBUTE_TAG="EthernetDevice";
	public static final String IPADDRESSATTRIBUTE_TAG="IPAddress";
	
	private Logger logger = Logger.getLogger(EthernetEndPoint.class);
	
	/**
	 * 
	 */
	public EthernetEndPoint() {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor EthernetEndPoint");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws RuntimeException {
		// Check if we have this ethernet device on this platform
		NetworkInterface n = null;
		String intName = null, ipAddress = null;
		boolean found = false;
		
		this.logger.debug("check if we have the ethernet port.");
		try {
			intName = getConfig(ETHDEVICEATTRIBUTE_TAG);
			ipAddress = getConfig(IPADDRESSATTRIBUTE_TAG);
			this.logger.debug( intName + "(" + ipAddress +")");
			
			/* Look for the ethernet if either by name or by IP address*/
			if (ipAddress.length() == 0) {
				/* by name */
				n = NetworkInterface.getByName(intName);
				found = true;
			} else {
				/* by IP address*/
				InetAddress i;
				Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
				search:
				while(e.hasMoreElements()) {
					n = e.nextElement();
					if(n.getDisplayName().equals(intName)) {
						Enumeration<InetAddress> ee = n.getInetAddresses();
						while(ee.hasMoreElements()) {
							i = ee.nextElement();
							if(i.getHostAddress().equals(ipAddress)) {
								found = true;
								break search;
							}
						}						
					}
				}
			}
			
			if(found) {
				this.logger.debug("Found ethernet port: " + n.getDisplayName());
				if (!n.isUp()) throw new RuntimeException("The ethernet port is down");
				this.configured = true;
				this.logger.debug("The ethernet port was successfully configured");
			} else 
				throw new RuntimeException("The ethernet port could not be found");
		} catch (SocketException e) {
			e.printStackTrace();
			this.logger.debug("Couldnt find the ethernet port...");
			throw new RuntimeException("Couldnt find the ethernet port...");
		} catch (BadAttributeValueExpException e) {
			this.logger.debug("Bad ethernet EndPoint XML config");
			e.printStackTrace();
			throw new RuntimeException("Couldnt initialise the ethernet port...");
		} 
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove() {
		//Not much to do here...
		this.logger.debug("Removing Ethernet Endpoint.");
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
		EthernetEndPoint e = new EthernetEndPoint();
		e.parseConfig();
	}
}
