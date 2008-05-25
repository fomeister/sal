package au.edu.jcu.haldbus;


import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.naming.ConfigurationException;

import org.freedesktop.Hal.Device;
import org.freedesktop.Hal.Manager;
import org.freedesktop.Hal.Manager.DeviceAdded;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

import au.edu.jcu.haldbus.match.HalMatchInterface;


public class HalDBusHelper implements Runnable, DBusSigHandler<Manager.DeviceAdded>{
	private int QUEUE_SIZE=30;
	private Thread t;
	private DBusConnection conn = null;
	private BlockingQueue<Map<String,Variant<Object>>> properties;
	private List<HalClientInterface> clients;
	
	public HalDBusHelper() throws ConfigurationException {
		try {
			conn = DBusConnection.getConnection(DBusConnection.SYSTEM);
			conn.addSigHandler(Manager.DeviceAdded.class, this);
		} catch (DBusException DBe) {
			throw new ConfigurationException();			
		}
		t = new Thread(this);
		properties = new ArrayBlockingQueue<Map<String,Variant<Object>>>(QUEUE_SIZE);
		clients = new LinkedList<HalClientInterface>();
		loadClientLists();
		t.start();
	}
	
	public void stop(){
		t.interrupt();
		join();
		conn.disconnect();
	}
	
	private void join(){
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println("Interrupted while joining");
		}
	}
	
	public void loadClientLists(){
		try {
			clients.add(new V4LHalClient());
		} catch (ConfigurationException e) {
			System.out.println("Cant load client Match list");
			e.printStackTrace();
		}
	}
	
	private String match(Map<String,Variant<Object>> props, HalMatchInterface m) throws ConfigurationException{
		String p;

		//if property is in current HAL object
		if(m.matchThisObject()){
			p = m.getPropName();
			//get property value
			if(props.containsKey(p))
				//and test it
				return m.match(props.get(p).getValue());
			//property not found
			throw new ConfigurationException();
		} else if(m.matchNextObjectValue())
			//if property in another HAL object, get the object's properties and test them
			return match(getAllProperties(m.getNextObjectValue()), m.getNextMatch());
		else if(m.matchNextObjectLink()) {
			//if property in another HAL object, get the object's properties and test them
			p = (String) props.get(m.getNextObjectLink()).getValue();
			return match(getAllProperties(p), m.getNextMatch());
		} else
			throw new ConfigurationException();
	}

	public void run() {
		Map<String,Variant<Object>> map = null;
		Map<String,String> matches = new Hashtable<String,String>();
		Iterator<HalClientInterface> iter;
		Iterator<HalMatchInterface> iter2;
		Iterator<String> iter3;
		HalClientInterface c;
		HalMatchInterface m;
		String s;
		try {			
			while((map=properties.take())!=null && !Thread.interrupted()){
				iter = clients.iterator();
				while(iter.hasNext()){
					//for each HAL client, get the match list
					c = iter.next();
					iter2 = c.getMatchList().iterator();
						while(iter2.hasNext()){
							//for each HALMatch object, check if there is a match
							m = iter2.next();
							try {
								s = match(map, m);
								matches.put(m.getName(),s);
							} catch (ConfigurationException e) {}
						}
						
						//if we have enough matches, call doAction()
						if(c.getMinMatches()<=matches.size() && matches.size()<=c.getMaxMatches()){
							System.out.println("We had "+matches.size()+" matches");
							iter3 = matches.keySet().iterator();
							while(iter3.hasNext()){
								s = iter3.next();
								System.out.println("Key: "+s+" - value: "+matches.get(s));
							}
							c.doAction(matches);
						}
					
					//move on to next client
					matches.clear();
				}
			}
		} catch (InterruptedException e) {} 
	}
	
	public Map<String,Variant<Object>> getAllProperties(String udi) throws ConfigurationException{
		Device d;
		try {
			d = (Device) conn.getRemoteObject("org.freedesktop.Hal", udi, Device.class);
			return d.GetAllProperties();
		} catch (DBusException e) {
			System.out.println("Cant list properties for HAL object '"+udi+"'");
			throw new ConfigurationException();
		} 
	}

	public void handle(DeviceAdded arg0) {
		try {properties.add(getAllProperties(arg0.udiAdded));}
		catch (IllegalStateException e) {
			System.out.println("Cant add Property list for newly added device - Queue full");
		} catch (ConfigurationException e) {
			System.out.println("Cant handle newly added device - no properties");
		}
	}

	public static void main(String[] args) throws ConfigurationException, IOException{
		HalDBusHelper h = new HalDBusHelper();
		System.in.read();
		h.stop();		
	}

}
