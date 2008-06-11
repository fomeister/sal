package jcu.sal.config.deviceDetection;


import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.ConfigurationException;

import jcu.sal.utils.ListChangeListener;
import jcu.sal.utils.ProtocolModulesList;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.freedesktop.Hal.Device;
import org.freedesktop.Hal.Manager;
import org.freedesktop.Hal.Manager.DeviceAdded;
import org.freedesktop.Hal.Manager.DeviceRemoved;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;

import au.edu.jcu.haldbus.HalFilterInterface;
import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.DBusException;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;
import au.edu.jcu.haldbus.match.HalMatchInterface;

/**
 * Objects of this class run a single thread waiting for HAL DeviceAdded/DeviceRemoved signals. When a DeviceAdded signal is recevied, 
 * a list of <code>HalFilterInterface</code> objects is matched against the new device's properties. If enough 
 * matches are successful, <code>HalFilterInterface.doAction()</code> is called. When a DeviceRemoved signal is received,  
 * The list of <code>HalFilterInterface</code> objects can be changed at runtime using <code>addClient()</code> and 
 * <code>removeClient()</code>. 
 * @author gilles
 *
 */
public class HalHelper implements Runnable, DBusSigHandler, HwProbeInterface, ListChangeListener{
	public static String NAME = "HalHelper";
	private static Logger logger = Logger.getLogger(HalHelper.class);
	private Thread t;
	private DBusConnection conn = null;
	private BlockingQueue<Map<String,Variant<Object>>> properties;
	private List<HalFilterInterface> clients;
	/**
	 * The following is a map of the UDI we have seen so far and their associated successful matches.
	 * Entries to this map are added when a DeviceAdded signal is received and the matches in a filter are successful.
	 * The matches are stored with the UDI of the added object. When this object is removed (DeviceRemoved signal), the same
	 * matches are passed to the filter. 
	 */
	private Map<String, MatchingFilter> udiMatches;
	
	/**
	 * Default constructor. It initialises the new object's members and creates the list of filters
	 */
	public HalHelper(){
		Slog.setupLogger(logger);
		t = new Thread(this, "HalHelper thread");
		properties = new LinkedBlockingQueue<Map<String,Variant<Object>>>();
		clients = new LinkedList<HalFilterInterface>();
		udiMatches = new Hashtable<String, MatchingFilter>();
		listChanged();
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized void start() throws Exception{
		if(!t.isAlive()) {
			try {
				conn = DBusConnection.getConnection(DBusConnection.SYSTEM);
				conn.addSigHandler(Manager.DeviceAdded.class, this);
				conn.addSigHandler(Manager.DeviceRemoved.class, this);
			} catch (org.freedesktop.dbus.exceptions.DBusException e) {
				throw new DBusException(e);
			}
			initialCheck();
			t.start();
		}
	}
	
	@Override
	public synchronized void stop(){
		if(t.isAlive()) {
			t.interrupt();
			join();
			conn.disconnect();
		}
	}
	
	public void addClient(HalFilterInterface f) throws AddRemoveElemException{
		synchronized (clients) {
			if(clients.contains(f))
				throw new AddRemoveElemException();
			clients.add(f);
		}
	}
	
	public void removeClient(HalFilterInterface f) throws AddRemoveElemException{
		synchronized (clients) {
			if(!clients.contains(f))
				throw new AddRemoveElemException();
			clients.remove(f);
		}
	}

	/**
	 * This method implements a loop which waits for HAL property list to be available, and then filters them
	 */
	@Override
	public void run() {
		Map<String,Variant<Object>> map = null;

		try {			
			while((map=properties.take())!=null && !Thread.interrupted())
				checkProperties(map, false);
		} catch (InterruptedException e) {} 
	}

	/**
	 * This method is called by DBus whenever a new HAL object is created. It retrieves the object's properties and
	 * queues them for filtering by the thread. 
	 * @param d the DeviceAdded signal object.
	 */
	@Override
	public void handle(DBusSignal s) {
		if(s instanceof DeviceAdded) {
			DeviceAdded d = (DeviceAdded) s;
			logger.debug("New device added '"+d.udiAdded+"'");
			try {properties.add(getAllProperties(d.udiAdded));}
			catch (DBusException e) {
				logger.error("Cant handle newly added device - unable to list its properties");
			}
		} else if(s instanceof DeviceRemoved) {
			DeviceRemoved d = (DeviceRemoved) s;
			logger.debug("Device removed '"+d.udiRemoved+"'");
			MatchingFilter f = udiMatches.get(d.udiRemoved);
			if(f!=null) {
				f.f.deviceRemoved(f.matches);
				udiMatches.remove(d.udiRemoved);
			}
		}
			
	}
	
	
	@Override
	public void listChanged() {
		List<HalFilterInterface> tmp;
		HalFilterInterface h = null;
		Iterator<HalFilterInterface> iter;

		synchronized(clients) {
			tmp = new LinkedList<HalFilterInterface>(clients);
			
			//Create the new Halfilter and try to insert it in clients 
			for (String name : getNewFilterList())
				try {
					h = createClient(name);
					addClient(h);
				} catch (AddRemoveElemException e) {
					tmp.remove(h);				
				} catch (InstantiationException e) {} 
				
			if(tmp.size()>0){
				iter = tmp.iterator();
				while(iter.hasNext()) 
					try { removeClient(iter.next());} catch (AddRemoveElemException e) {}			
			}
		}
	}
	
	
	/*
	 * PRIVATE METHODS 
	 */
	
	private List<String> getNewFilterList(){
		return ProtocolModulesList.getFilter(NAME);
	}
	
	/**
	 * this method creates a filter given its class name.
	 * @return the filter or null
	 */
	private HalFilterInterface createClient(String className) throws InstantiationException{
		Constructor<?> c;
		HalFilterInterface h=null;
		try {
			c = Class.forName(className).getConstructor(new Class<?>[0]);
			h = (HalFilterInterface) c.newInstance(new Object[0]);
		} catch (Exception e) {
			logger.error("Cant instanciate filter "+className);
			e.printStackTrace();
			throw new InstantiationException();
		}
		return h;		
	}
	
	/**
	 * this method is invoked only once, when the helper is started. It runs the list of filters
	 * against the exiting devices.
	 */
	private void initialCheck() {
		//long start;
		try {
			String[] list = getAllDevices();
			//start = System.currentTimeMillis();
			for (String udi : list) 
				checkProperties(getAllProperties(udi), true);
			//logger.debug("took "+((float) (System.currentTimeMillis() - start)/1000 ) + " sec overall");
		} catch (DBusException e) {
			logger.error("Cant carryout initial check: DBus error "+e.getMessage());
		}
	}
	
	
	/**
	 * This method matches each of the HAL match objects against the HAl property list given in argument.
	 * @param map the property list against which each of the HAL match object must be tested.
	 */
	private void checkProperties(Map<String,Variant<Object>> map, boolean initial){
		Map<String,String> matches = new Hashtable<String,String>();
		Map<String, HalMatchInterface> matchList;
		Iterator<HalFilterInterface> iter;
		Iterator<String> iter2;
		HalFilterInterface c;
		HalMatchInterface m;
		String s, matchName;
		int maxUnmatch, countUnmatch;
		
		synchronized(clients) {
			iter = clients.iterator();
			while(iter.hasNext()){	
				//for each HAL client, get the match list
				c = iter.next();
				maxUnmatch = c.countMatches() - c.getMinMatches();
				countUnmatch = 0;
				//logger.debug("Checking client "+c.getName() + c.initialMatch() + c.subsequentMatch());

				matchList = c.getMatchList(); 
				iter2 = matchList.keySet().iterator();
				while(iter2.hasNext()){
					//for each HALMatch object, check if there is a match
					matchName = iter2.next();
					m = matchList.get(matchName);
					try {
						s = match(map, m);
						//logger.debug("Match for "+matchName+ " - "+m.getName() + " : "+s	);
						matches.put(matchName,s);
					} catch (MatchNotFoundException e) {
						//logger.debug("No Match for "+matchName+ " - "+m.getName());
						if(++countUnmatch>maxUnmatch) break;
					}
				}

				//if we have enough matches, call deviceAdded()
				//logger.debug("We had "+matches.size()+" matches - expected min: "+c.getMinMatches()+" - max: "+c.getMaxMatches());
				if(c.getMinMatches()<=matches.size() && matches.size()<=c.getMaxMatches()) {
					
					if(udiMatches.put(getUDI(map), new MatchingFilter(c, new Hashtable<String,String>(matches)))!=null)
						logger.error("There was a previous MatchingFilter in udiMatches for UDI "+getUDI(map));

					if( (initial && c.initialMatch()) || (!initial && c.subsequentMatch()) )
						c.deviceAdded(matches);
				}

				//move on to next client
				matches.clear();
			}
		}
	}
	
	/**
	 * This recursive method matches a <code>HalMatchInterface</code> object to a list of HAL properties.
	 * @param props the list of HAL properties
	 * @param m the <code>HalMatchInterface</code> object
	 * @return the result of the match if successful
	 * @throws ConfigurationException if the match is unsuccessful
	 */
	private String match(Map<String,Variant<Object>> props, HalMatchInterface m) throws MatchNotFoundException{
		String p;
		//if property is in current HAL object
		if(m.matchThisObject()){
			p = m.getPropName();

			//get property value
			if(props.containsKey(p))
				//check for match
				return m.match(props.get(p).getValue());

			//property not found
			throw new MatchNotFoundException();
		} else {
			//if property in another HAL object, get the object's properties and test them
			p = (m.matchNextObjectValue()) ? m.getNextObjectValue() : props.get(m.getNextObjectLink())!=null ? (String) props.get(m.getNextObjectLink()).getValue() : null;

			if(p==null)
				throw new MatchNotFoundException();

			try {
				return match(getAllProperties(p), m.getNextMatch());
			} catch (DBusException e){
				throw new MatchNotFoundException(e);
			}
		}
	}
	
	/**
	 * This method fetches the properties of an HAL object given its UDI.
	 * @param udi the UDI of the object whose properties are neede.
	 * @return a map of the object's properties
	 * @throws ConfigurationException if there is an error getting the properties
	 */
	private Map<String,Variant<Object>> getAllProperties(String udi) throws DBusException{
		Device d;
		try {
			d = (Device) conn.getRemoteObject("org.freedesktop.Hal", udi, Device.class);
			return d.GetAllProperties();
		} catch (org.freedesktop.dbus.exceptions.DBusException e) {
			logger.error("Cant list properties for HAL object '"+udi+"'");
			throw new DBusException(e);
		} 
	}
	
	private String getUDI(Map<String,Variant<Object>> m) {
		return (String) m.get("info.udi").getValue();
	}
	
	/**
	 * This method fetches the properties of an HAL object given its UDI.
	 * @param udi the UDI of the object whose properties are neede.
	 * @return a map of the object's properties
	 * @throws ConfigurationException if there is an error getting the properties
	 */
	private String[] getAllDevices() throws DBusException{
		Manager m;
		try {
			m =  conn.getRemoteObject("org.freedesktop.Hal", "/org/freedesktop/Hal/Manager", Manager.class);
			return m.GetAllDevices();
		} catch (org.freedesktop.dbus.exceptions.DBusException e) {
			logger.error("Cant list existing HAL objects");
			throw new DBusException(e);
		} 
	}
	
	/**
	 * This method calls <code>join()</code> on the thread object and wait till the thread exits.
	 */
	private void join(){
		try {
			t.join();
		} catch (InterruptedException e) {
			logger.error("Interrupted while joining");
		}
	}
	
	private class MatchingFilter {
		public HalFilterInterface f;
		public Map<String,String> matches;
		public MatchingFilter(HalFilterInterface f, Map<String,String> m){
			this.f =f;
			matches = m;
		}
	}
}
