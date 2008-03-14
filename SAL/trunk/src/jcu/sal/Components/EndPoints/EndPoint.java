/**
 * @author gilles
 */
package jcu.sal.Components.EndPoints;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.ConfigurationException;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.componentRemovalListener;
import jcu.sal.Components.Protocols.ProtocolID;
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
	 * The table containing the device change listeners and their associated usb IDs
	 * access to this variable must be synchronized(listeners)
	 */
	protected static Hashtable<String,ArrayList<DeviceListener>> listeners;
	
	protected boolean enabled;
	protected boolean configured;
	protected boolean autodetect=false;

	
	/**
	 * 
	 */
	public EndPoint(EndPointID i, String t, Hashtable<String,String> c) {
		super();
		Slog.setupLogger(this.logger);
		enabled=false;
		configured=false;
		id = i;
		type = t;
		config = c;
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
	public final void remove(componentRemovalListener c) {
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
	public final void start() throws ConfigurationException{
		synchronized (this) {
			if(configured && !enabled) {
				this.logger.debug("Starting "+type+" Endpoint.");
				internal_start();
				enabled=true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public final void stop() {
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
	 * Stops the endpoint.
	 * this method should be overriden by Endpoints if more things need to be done 
	 */
	protected void internal_stop() {}
	
	/**
	 * Starts the endpoint.
	 * this method should be overriden by Endpoints if more things need to be done 
	 */
	protected void internal_start() throws ConfigurationException {}
	
	/**
	 * Prepare the subclass to be removed 
	 * this method should be overriden by Endpoints if more things need to be done 
	 */
	protected void internal_remove() {}
	
	/**
	 * Sets the protocol with which we are associated
	 */
	public final void setPid(ProtocolID i){
		id.setPid(i);
	}

	
	/**
	 * this method registers device change listeners. Whenever a device is plugged/unplugged,
	 * its associated device listeners are notified.
	 * @param d the device listener which should be notifier
	 * @param ids the device ID which should be watched
	 * @throws UnsupportedOperationException if the EndPoint does not support autodetection
	 */
	public final void registerDeviceListener(DeviceListener d, String[] ids) throws UnsupportedOperationException{
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
	public final void unregisterDeviceListener(DeviceListener d) throws UnsupportedOperationException{
		if(!autodetect)
			throw new UnsupportedOperationException("Autodetection of sensor native controllers not supported by this Endpoint");
		
		synchronized (listeners) {
			Iterator<ArrayList<DeviceListener>> i = listeners.values().iterator();
			while(i.hasNext())
				i.next().remove(d);	
		}
	}


	
	
}
