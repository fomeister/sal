/**
 * 
 */
package jcu.sal.components.EndPoints;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class EthernetEndPoint extends EndPoint {

	public static final String ETHDEVICEATTRIBUTE_TAG="EthernetDevice";
	public static final String IPADDRESSATTRIBUTE_TAG="IPAddress";
	public static final String ETHERNETENDPOINT_TYPE="ethernet";
	
	private Logger logger = Logger.getLogger(EthernetEndPoint.class);
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public EthernetEndPoint(EndPointID i, Hashtable<String,String> c) throws ConfigurationException {
		super(i, ETHERNETENDPOINT_TYPE, c);
		Slog.setupLogger(this.logger);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#updateConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Check if we have this ethernet device on this platform
		NetworkInterface n = null;
		String intName = null, ipAddress = null;
		boolean found = false;
		
		this.logger.debug("check if we have the ethernet port.");
		try {
			intName = getConfig(ETHDEVICEATTRIBUTE_TAG);
			try {ipAddress = getConfig(IPADDRESSATTRIBUTE_TAG);} catch(BadAttributeValueExpException e) {ipAddress="";}
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
				while(e.hasMoreElements() && !found) {
					n = e.nextElement();
					if(n.getDisplayName().equals(intName)) {
						Enumeration<InetAddress> ee = n.getInetAddresses();
						while(ee.hasMoreElements() && !found) {
							i = ee.nextElement();
							if(i.getHostAddress().equals(ipAddress)) {
								found = true;
							}
						}						
					}
				}
			}
			
			if(found) {
				this.logger.debug("Found ethernet port: " + n.getDisplayName());
				if (!n.isUp()) {
					this.logger.error("The ethernet port is down");
					throw new ConfigurationException("The ethernet port is down");
				}
				this.configured = true;
				this.logger.debug("The ethernet port was successfully configured");
			} else {
				this.logger.error("The ethernet port could not be found");
				throw new ConfigurationException("The ethernet port could not be found");
			}
		} catch (SocketException e) {
			this.logger.error("Couldnt find the ethernet port...");
			e.printStackTrace();
			throw new ConfigurationException("Couldnt find the ethernet port...");
		} catch (BadAttributeValueExpException e) {
			this.logger.debug("Bad ethernet EndPoint XML config");
			e.printStackTrace();
			throw new ConfigurationException("Couldnt initialise the ethernet port...");
		} 
	}
	
	public static void main(String[] args) throws ConfigurationException {
		/* Tries building a new ethernet Endpoint*/
		Hashtable<String,String> c = new Hashtable<String,String>();
		c.put("EthernetDevice","eth0");
		c.put("IPAddress","");
		new EthernetEndPoint(new EndPointID("ethernet"), c);
	}
}
