/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
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
public class UsbEndPoint extends EndPoint{

	private Logger logger = Logger.getLogger(EndPoint.class);
	private static String USBENDPOINT_TYPE="usb";
	private static String LSUSBOUTPUT_KEY = "Bus";
	
	/**
	 * Contains the USB ID of the USB host controllers so they can be filtered out when
	 * looking for new USB devices
	 */
	private static String NODEVICE_USBID = "0000:0000";

	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public UsbEndPoint(EndPointID i, Hashtable<String,String> c) throws ConfigurationException {
		super(i,USBENDPOINT_TYPE,c);
		Slog.setupLogger(this.logger);
		autodetect = true;
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
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Components.EndPoints.EndPoint#getConnectedDevices()
	 */
	@Override
	protected ArrayList<String> getConnectedDevices() throws IOException {
		/** 
		 * noDevId contains the USB id "0000:0000" which belongs to the USB host controller
		 * obviously this is not a USB device we re interested in, so we add this ID to noDevId
		 * the elements in noDevId are removed from the output of lsusb every time it is called  
		 */
		ArrayList<String> plists, noDevId = new ArrayList<String>();
		noDevId.add(NODEVICE_USBID);
		BufferedReader b[] = PlatformHelper.captureOutputs("lsusb", true);
		plists = PlatformHelper.getFieldsFromBuffer(b[0], LSUSBOUTPUT_KEY, 6, " ", false);
		plists.removeAll(noDevId);
		return plists;
	}
	
	public static void main(String[] args) throws ConfigurationException, InterruptedException{
		UsbEndPoint e = new UsbEndPoint(new EndPointID("usb"), new Hashtable<String, String>());
		e.start();
		Thread.sleep(600*1000);
		e.stop();
	}
}
