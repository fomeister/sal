package listener;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.freedesktop.DBus.Introspectable;
import org.freedesktop.Hal.Device;
import org.freedesktop.Hal.Manager;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

public class TestListenerDeviceAdded implements DBusSigHandler<Manager.DeviceAdded>{
	
	private DBusConnection conn = null;

	public synchronized void handle(Manager.DeviceAdded s) {
		System.out.println("SIGNAL!!");
		System.out.println("Got a device connect from "+ s.obj + " for "+s.udiAdded);
		//introspect(s.udiAdded);
		dumpProperties(s.udiAdded);
	}
	
	public void introspect(String udi) {
		Introspectable introspectable;
		try {
			introspectable = conn.getRemoteObject("org.freedesktop.Hal", udi , Introspectable.class);
			System.out.println("------------------------------------------------------------");
			System.out.println("\nIntrospect on device '"+udi+"'");
			System.out.println(introspectable.Introspect());
		} catch (DBusException e) {
			e.printStackTrace();
		}
	}
	
	public void dumpProperties(String udi){
		String str;
		Object o = null;

		Device d;
		System.out.println("------------------------------------------------------------");
		System.out.println("\nProperties dump on device '"+udi+"'");

		try {
			d = (Device) conn.getRemoteObject("org.freedesktop.Hal", udi, Device.class);
		} catch (DBusException e) {
			e.printStackTrace();
			return;
		}
		Map<String,Variant<Object>> mp = d.GetAllProperties();
		Iterator<String> iter = mp.keySet().iterator();
		while(iter.hasNext()) {
			str = iter.next();
			o = mp.get(str).getValue();
			System.out.print("Property: "+str + "- type:"+o.getClass().getName() );
			if (o instanceof String)
				System.out.print(" - Value: "+(String) o);
			else
				System.out.print(" - Value: "+o.toString());
			System.out.println();
		}		
	}
	
	public TestListenerDeviceAdded(DBusConnection c) {
		conn = c;
		System.out.println("TestListenerDeviceAdded added");
	}
	
	public static void main(String[] args) {
		DBusConnection conn = null;
		System.out.println("Creating DBus Connection");
		

		try {
			conn = DBusConnection.getConnection(DBusConnection.SYSTEM);
		} catch (DBusException DBe) {
			System.out.println("Could not connect to bus: " + DBe);
			System.exit(1);
		}

		//Manager HAL = null;
		try {
//			HAL = (Manager)conn.getRemoteObject("org.freedesktop.Hal","/org/freedesktop/Hal/Manager",Manager.class);
//			System.out.println("Printing UDI's");
//			String UDI[] = HAL.GetAllDevices();
//			for(int i = 0; i < UDI.length; i++) {
//				System.out.println("UDI: " + UDI[i]);				
//			}
			
			Introspectable introspectable = conn.getRemoteObject("org.freedesktop.Hal", "/org/freedesktop/Hal/Manager" , Introspectable.class);
			System.out.println(introspectable.Introspect());			
//			Device d = (Device) conn.getRemoteObject("org.freedesktop.Hal","/org/freedesktop/Hal/devices/usb_device_46d_8b5_noserial",Device.class);
//			Map<String,Variant<Object>> mp = d.GetAllProperties();
//			Iterator<String> iter = mp.keySet().iterator();
//			while(iter.hasNext()) {
//				s = iter.next();
//				o = mp.get(s).getValue();
//				System.out.print("Property: "+s + "- type:"+o.getClass().getName() );
//				if (o instanceof String)
//					System.out.print(" - Value: "+(String) o);
//				else
//					System.out.print(" - Value: "+o.toString());
//				System.out.println();
//			}
			
			conn.addSigHandler(Manager.DeviceAdded.class, new TestListenerDeviceAdded(conn));
			conn.addSigHandler(Manager.DeviceRemoved.class, new TestListenerDeviceRemoved(conn));
		} catch (DBusException DBe) {
			System.out.println("Could not set connect to HAL: " + DBe);
			conn.disconnect();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			conn.disconnect();
			System.exit(1);
		}
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn.disconnect();
		System.out.println("DBus hooking complete");
		
	}	
}
