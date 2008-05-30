package jcu.sal.utils;

import java.util.LinkedList;
import java.util.List;

public class HwProbeList {
	public final static String DeviceDetectionHelperPackage = "jcu.sal.config.deviceDetection.";
	private static HwProbeList d = new HwProbeList();
	/**
	 * Map of all hw probe names and their associated classes
	 */
	private List<String> helperTable;
	
	/**
	 * List of listeners
	 */
	private List<ListChangeListener> listeners;

	private HwProbeList(){
		listeners = new LinkedList<ListChangeListener>();
		helperTable = new LinkedList<String>();
		helperTable.add(DeviceDetectionHelperPackage+"HalHelper");
	}
	
	public static List<String> getHelperClassNames(){	
		return new LinkedList<String>(d.helperTable);
	}
	
	public static void addListener(ListChangeListener l){
		synchronized(d.listeners) {
			if(!d.listeners.contains(l))
				d.listeners.add(l);
		}
	}
	
	public static void removeListener(ListChangeListener l){
		synchronized(d.listeners) {
			if(d.listeners.contains(l))
				d.listeners.add(l);
		}
	}
}
