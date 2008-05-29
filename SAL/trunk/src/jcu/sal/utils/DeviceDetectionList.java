package jcu.sal.utils;

import java.util.LinkedList;
import java.util.List;

public class DeviceDetectionList {
	public final static String DeviceDetectionHelperPackage = "jcu.sal.config.deviceDetection.";
	private static DeviceDetectionList d = new DeviceDetectionList();
	/**
	 * Map of all Device Detection helper names and their associated classes
	 */
	private List<String> helperTable;
	
	/**
	 * List of listeners
	 */
	private List<DeviceDetectionListener> listeners;

	private DeviceDetectionList(){
		listeners = new LinkedList<DeviceDetectionListener>();
		helperTable = new LinkedList<String>();
		helperTable.add(DeviceDetectionHelperPackage+"HalHelper");
	}
	
	public static String[] getHelperClassNames(){	
		return (String []) d.helperTable.toArray();
	}
	
	public static void addListener(DeviceDetectionListener l){
		synchronized(d.listeners) {
			if(!d.listeners.contains(l))
				d.listeners.add(l);
		}
	}
	
	public static void removeListener(DeviceDetectionListener l){
		synchronized(d.listeners) {
			if(d.listeners.contains(l))
				d.listeners.add(l);
		}
	}
}
