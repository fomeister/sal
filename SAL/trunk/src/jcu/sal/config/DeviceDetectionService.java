package jcu.sal.config;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import jcu.sal.config.deviceDetection.HwDetectorInterface;
import jcu.sal.utils.DeviceDetectionList;
import jcu.sal.utils.DeviceDetectionListener;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * This class checks what DeviceDetection helpers are available
 * and is responsible for starting and stopping them
 * @author gilles
 *
 */
public class DeviceDetectionService implements DeviceDetectionListener{
	private static Logger logger = Logger.getLogger(DeviceDetectionService.class);
	private static DeviceDetectionService d = new DeviceDetectionService();
	
	/**
	 * This map contains a list of helper classes, and a reference to their instance.
	 */
	private Map<String, HwDetectorInterface> helperMap;
	
	/**
	 * Singleton contructor
	 *
	 */
	private DeviceDetectionService(){
		Slog.setupLogger(logger);
		helperMap = new Hashtable<String, HwDetectorInterface>();
	}
	
	/**
	 * This method returns an instance of the DeviceDetectionService.
	 * @return
	 */
	public DeviceDetectionService getService(){
		return d;
	}
	
	/**
	 * This method reloads the list of helpers and starts the new ones
	 *
	 */
	public void loadNStart() {
		Map<String, HwDetectorInterface> m = loadHelpers(); 
		Iterator<String> iter = m.keySet().iterator();
		HwDetectorInterface h;
		String name;
		while (iter.hasNext()) {
			try {
				name = iter.next();
				h = m.get(name);
				h.start();
				helperMap.put(name, h);
			} catch (Exception e) {
				logger.error("error starting helper");
				e.printStackTrace();
			}			
		}
	}
	
	public void stopAll() {
		Map<String, HwDetectorInterface> m = loadHelpers(); 
		Iterator<String> iter = m.keySet().iterator();
		HwDetectorInterface h;
		String name;
		while (iter.hasNext()) {
			name = iter.next();
			h = m.get(name);
			h.stop();
			helperMap.remove(name);
		}
	}
	
	
	/**
	 * This methods fetches the list of known DeviceDetection helpers and instanciate any new ones
	 * that are not in <code>m</code> already. It then returns a list of the newly instanciated helpers.
	 * Helpers that have been removed from the list as returned by DeviceDetectionList, are already loaded
	 * will be removed when <code>stopAll()</code> is invoked.
	 * @return a list of the newly instanciated helpers (if any)
	 */
	private Map<String, HwDetectorInterface> loadHelpers(){
		Map<String, HwDetectorInterface> ret = new Hashtable<String, HwDetectorInterface>();
		String [] list = DeviceDetectionList.getHelperClassNames();
		for (String name : list) {
			//Instanciate the new helper if it isnt in the map already
			if(!helperMap.containsKey(name)) {
				Constructor<?> c;
				try {
					c = Class.forName(name).getConstructor(new Class<?>[0]);
					HwDetectorInterface h = (HwDetectorInterface) c.newInstance(new Object[0]);
					ret.put(name, h);
				} catch (Exception e) {
					logger.error("Cant instanciate Helper "+name);
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	public void listChanged() {
		new Thread(new Runnable() {
			public void run() {loadNStart();}
		}).start();
	}
}
