/**
 * @author gilles
 */
package jcu.sal.Components.EndPoints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.ConfigurationException;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.componentRemovalListener;
import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public abstract class EndPoint extends AbstractComponent<EndPointID> {

	public static final String ENDPOINTTYPE_TAG = "type";
	public static final String ENDPOINTNAME_TAG = "name";
	public static final String ENPOINT_TAG="EndPoint";
	
	private Logger logger = Logger.getLogger(EndPoint.class);
	
	/**
	 * Is Autodetection of Adapters (sensor native controllers) supported ?
	 * It is up to subclassed to set this flag
	 */
	protected boolean autodetect;
	
	/**
	 * The table containing the device change listeners and their associated usb IDs
	 * access to this variable must be synchronized(listeners)
	 */
	protected Hashtable<String,ArrayList<DeviceListener>> listeners;
	
	/**
	 * Contains the currently connected devices IDs (String) and how many of these there are (Integer)
	 */
	private Hashtable<String, Integer> devices;
	
	/**
	 * The thread watching for adapter devices plug/unplug events
	 */
	private Autodetection deviceWatcher;
	
	/**
	 * how often the thread should chek for plugged/unplugged devices (in milliseconds)
	 */
	protected long deviceWatchInterval = 1 * 1000;
	
	protected boolean enabled;
	protected boolean configured;

	
	/**
	 * 
	 */
	public EndPoint(EndPointID i, String t, Hashtable<String,String> c) {
		super();
		Slog.setupLogger(this.logger);
		autodetect=false;
		listeners = new Hashtable<String,ArrayList<DeviceListener>>();
		enabled=false;
		configured=false;
		id = i;
		type = t;
		config = c;
		devices = new Hashtable<String, Integer>();
	}
	
	/**
	 * returns a textual representation of a End Point's instance
	 * @return the textual representation of the Logical Port's instance
	 */
	public String toString() {
		return "EndPoint "+id.getName()+"("+type+")";
	}
	

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove(componentRemovalListener c) {
		synchronized (this) {
			if(enabled)
				stop();
			configured=false;
			internal_remove();
			this.logger.debug(type+" Endpoint removed");	
		}
		c.componentRemovable(id);
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#start()
	 */
	@Override
	public void start() throws ConfigurationException{
		synchronized (this) {
			if(configured && !enabled) {
				this.logger.debug("Starting "+type+" Endpoint.");
				internal_start();
				enabled=true;
			}
		}

		deviceWatcher = new Autodetection();
		if(autodetect && deviceWatchInterval!=0) {
			this.logger.debug("Starting autodetect thread");
			deviceWatcher.start();
		} else if (deviceWatchInterval==0){
			logger.info("Autodetect interval set to 0 in the endpoint config.");
			logger.info("Disabling sensor autodetection");
		} else {
			logger.debug("Sensor autodetection not supported");
		}

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		if(deviceWatcher.isAlive()) {
			deviceWatcher.interrupt();
			try { deviceWatcher.join();}
			catch (InterruptedException e) {}
			logger.debug("autodetect thread stopped");
		}
		synchronized (this) {
			if(enabled) {
				this.logger.debug("Stopping "+type+" Endpoint.");
				internal_stop();
				enabled=false;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return enabled;
	}
	
	/**
	 * Stops the endpoint
	 */
	protected void internal_stop() {}
	
	/**
	 * Starts the endpoint.
	 */
	protected void internal_start() throws ConfigurationException {}
	
	/**
	 * Prepare the subclass to be removed 
	 */
	protected void internal_remove() {}
	
	/**
	 * this method should be overriden by Endpoints which provide native adapter autodetection
	 * and return a Array of native address (strings) of currently connected/visible adapters 
	 */
	protected ArrayList<String> getConnectedDevices() throws IOException {
		logger.debug(id.toString() + "Not configured for autodetection");
		throw new IOException("Not configured for autodetection");
	}
	
	/**
	 * this method registers device change listeners. Whenever a device is plugged/unplugged,
	 * its associated device listeners are notified.
	 * @param d the device listener which should be notifier
	 * @param ids the device ID which should be watched
	 * @throws UnsupportedOperationException if the EndPoint does not support autodetection
	 */
	public void registerDeviceListener(DeviceListener d, String[] ids) throws UnsupportedOperationException{
		if(!autodetect)
			throw new UnsupportedOperationException("Autodetection of sensor native controllers not supported by this Endpoint");
		
		for (int i = 0; i < ids.length; i++) {
			if(!listeners.containsKey(ids[i])) 
				listeners.put(ids[i], new ArrayList<DeviceListener>());
			synchronized (listeners) {listeners.get(ids[i]).add(d);}
			logger.debug("Added device listener for ID: '"+ids[i]+"', "+listeners.containsKey(ids[i])+" - "+listeners.get(ids[i]));
		}
	}
	/**
	 * This method unregisters device listeners
	 * @param d the listener to be removed
	 * @throws UnsupportedOperationException if the EndPoint does not support autodetection
	 */
	public void unregisterDeviceListener(DeviceListener d) throws UnsupportedOperationException{
		if(!autodetect)
			throw new UnsupportedOperationException("Autodetection of sensor native controllers not supported by this Endpoint");
		
		synchronized (listeners) {
			Iterator<ArrayList<DeviceListener>> i = listeners.values().iterator();
			while(i.hasNext())
				i.next().remove(d);	
		}
	}
	
	/**
	 * This inner class runs a thread that automatically detects native adapter additions / removals 
	 * @author gilles
	 *
	 */
	private class Autodetection implements Runnable{
		Thread t;
		
		public Autodetection() {
			t= new Thread(this, "EP_autodetection_thread_"+id.getName());
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
			String temp;
			Iterator<String> i = ids.iterator();
			while(i.hasNext()){
				temp = i.next();
				/* finds initially connected devices*/
				synchronized (listeners) {
					if(listeners.containsKey(temp)) {
						idl = listeners.get(temp).iterator();
						while(idl.hasNext()) {
							logger.debug("Notifying change on device(s) with ID: "+temp+", nb: "+devices.get(temp));
							idl.next().adapterChange(devices.get(temp));
						}
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
						if(!changedList.contains(temp)) changedList.add(temp);
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
}
