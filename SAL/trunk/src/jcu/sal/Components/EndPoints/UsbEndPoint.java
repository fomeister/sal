/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.ConfigurationException;

import jcu.sal.utils.PlatformHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class UsbEndPoint extends EndPoint{

	private static Logger logger = Logger.getLogger(EndPoint.class);
	public static String USBENDPOINT_TYPE="usb";
	private static String LSUSBOUTPUT_KEY = "Bus";
	
	/**
	 * Contains the USB ID of the USB host controllers so they can be filtered out when
	 * looking for new USB devices
	 */
	private static String NODEVICE_USBID = "0000:0000";
	
	/**
	 * Contains the currently connected devices IDs (String) and how many of these there are (Integer)
	 */
	private  static Hashtable<String, Integer> devices;
	
	/**
	 * The thread watching for adapter devices plug/unplug events
	 */
	private static Autodetection deviceWatcher= null;
	
	/**
	 * How many UsbEndPoint are there ? (used as ref count to know when to stop the thread)
	 */
	private static int ep_nb=0;
	
	/**
	 * how often the thread should chek for plugged/unplugged devices (in milliseconds)
	 */
	protected static long deviceWatchInterval = 1 * 1000;


	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public UsbEndPoint(EndPointID i, Hashtable<String,String> c) throws ConfigurationException {
		super(i,USBENDPOINT_TYPE,c);
		Slog.setupLogger(logger);
		devices = new Hashtable<String, Integer>();
		listeners = new Hashtable<String,ArrayList<DeviceListener>>();
		autodetect = true;
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws ConfigurationException {
		// Check if we have any USB ports on this platform
		logger.debug("check if we have USB ports.");
		try {
			BufferedReader b[] = PlatformHelper.captureOutputs("lsusb", true);
			if(!b[0].readLine().contains("Bus"))
				throw new ConfigurationException("Did not detect USB ports");
			configured = true;
			logger.debug("Yes we have. USB EndPoint initialised");
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug("Problem capturing output of lsusb");
			throw new ConfigurationException("Did not detect USB ports");
		}
	}
	
	/**
	 * Stops the endpoint.
	 * this method should be overriden by Endpoints if more things need to be done 
	 */
	@Override
	protected void internal_stop() {stopAutoDectectThread();}
	
	/**
	 * Starts the endpoint.
	 * this method should be overriden by Endpoints if more things need to be done 
	 */
	@Override
	protected void internal_start() throws ConfigurationException {startAutoDectectThread();}
	
	/**
	 * This method runs the "lsusb" and parse the output to find out the currently connected USB devices 
	 * @return an arraylist of USB IDs (string) of currently connected devices 
	 * @throws IOException if there s a problem running/parsing the lsusb
	 */
	
	private static ArrayList<String> getConnectedDevices() throws IOException {
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
	
	
	private static synchronized void startAutoDectectThread() {
		if(ep_nb==0) {
			deviceWatcher = new Autodetection("USB");
			if(deviceWatchInterval!=0) {
				logger.debug("Starting autodetect thread");
				deviceWatcher.start();
			} else if (deviceWatchInterval==0){
				logger.info("Autodetect interval set to 0 in the endpoint config.");
				logger.info("Disabling sensor autodetection");
			} else {
				logger.debug("Sensor autodetection not supported");
			}
		}// else logger.debug("USB device watcher already started - ep_nb:"+ep_nb+" isalive?"+deviceWatcher.isAlive());
		ep_nb++;
	}
	
	private static synchronized void stopAutoDectectThread() {
		if(deviceWatcher.isAlive() && ep_nb==1) {
			deviceWatcher.interrupt();
			try { deviceWatcher.join();}
			catch (InterruptedException e) {}
			deviceWatcher=null;
			logger.debug("autodetect thread stopped");
		}// else logger.debug("USB device watcher not stopped- ep_nb:"+ep_nb+" isalive?"+deviceWatcher.isAlive());
		ep_nb--;
	}
	
	/**
	 * This inner class runs a thread that automatically detects native adapter additions / removals 
	 * @author gilles
	 *
	 */
	private static class Autodetection implements Runnable{
		Thread t;
		
		public Autodetection(String n) {
			t= new Thread(this, "EP_autodetection_thread_"+n);
		}
		
		public void start() {
			t.start();
		}
		
		public void interrupt() {
			t.interrupt();
		}
		
		public boolean isAlive(){
			return t.isAlive();
		}

		public void join() throws InterruptedException {
			t.join();
		}
		
		private void notifyListeners(HashSet<String> ids) {
			Iterator<DeviceListener> idl;
			ArrayList<DeviceListener> d = null;
			String temp;
			Iterator<String> i = ids.iterator();
			while(i.hasNext()){
				temp = i.next();
				/* finds connected devices*/
				synchronized(listeners) {
					if(listeners.containsKey(temp))
						/* make a copy of the array of device listeners
						 * so we can call each of them without holding the listeners
						 * lock to avoid race conditions
						 */
						d = new ArrayList<DeviceListener>(listeners.get(temp));
					else d = null;
				}
				
				if(d!=null) {
					idl = d.iterator();
					while(idl.hasNext()) {
						logger.debug("Notifying change on device(s) with ID: "+temp+", nb: "+devices.get(temp));
						idl.next().adapterChange(devices.get(temp));
					}
				}
			}
			ids.clear();
		}
		
		/**
		 * Implements the autodetection thread
		 */
		public void run(){
			ArrayList<String> previousList, currentList, tempList;
			String temp;
			HashSet<String> changedList = new HashSet<String>();
			
			try {
				previousList = getConnectedDevices();
				Iterator<String> i = previousList.iterator();
				while(i.hasNext()){
					temp = i.next();
					devices.put(temp, (devices.get(temp)==null) ? 1 : devices.get(temp)+1);
					changedList.add(temp);
					logger.debug("device "+temp+" initially connected");
				}
				
				/* notify the listeners of the connected devices */
				notifyListeners(changedList);
				
				while(!Thread.interrupted()){
					
					Thread.sleep(deviceWatchInterval);

					/* get the list of connected usb devices */
					tempList = currentList = getConnectedDevices();
					
					/* parse it */
					i = currentList.iterator();
					while(i.hasNext()){
						temp = i.next();
						
						/* finds new plugged-in devices*/
						if(!previousList.contains(temp)) {
							logger.debug("Found newly connected device with ID: "+temp);
							devices.put(temp, (devices.get(temp)==null) ? 1 : devices.get(temp)+1);
							changedList.add(temp);
						} else 
							previousList.remove(temp);
					}
					
					i = previousList.iterator();
					while(i.hasNext()){
						temp = i.next();
						
						/* finds unplugged devices*/
						logger.debug("Found newly disconnected device with ID: "+temp);
						devices.put(temp, (devices.get(temp)==null) ? 0 : devices.get(temp)-1);
						changedList.add(temp);
					}
					
					notifyListeners(changedList);
					
					previousList = tempList;
				}
			} catch (InterruptedException e) {}
			catch (IOException e) {
				logger.error("error detecting connected devices");
				logger.error("Exception: " + e.getClass()+" - "+e.getMessage());
				if(e.getCause()!=null) logger.error("caused by: " + e.getCause().getClass()+" - "+e.getCause().getMessage());
			}
		}
		
	}
	
	public static void main(String[] args) throws ConfigurationException, InterruptedException{
		UsbEndPoint e = new UsbEndPoint(new EndPointID("usb"), new Hashtable<String, String>());
		UsbEndPoint e1 = new UsbEndPoint(new EndPointID("usb"), new Hashtable<String, String>());
		UsbEndPoint e2 = new UsbEndPoint(new EndPointID("usb"), new Hashtable<String, String>());
		e.start();
		e1.start();
		e2.start();
		Thread.sleep(6*1000);
		e2.stop();
		e1.stop();
		e.stop();
	}
}
