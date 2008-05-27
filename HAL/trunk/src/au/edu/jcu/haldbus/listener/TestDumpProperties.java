package au.edu.jcu.haldbus.listener;

import java.util.Iterator;
import java.util.Map;

import org.freedesktop.Hal.Device;
import org.freedesktop.Hal.Manager;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

public class TestDumpProperties {
	private DBusConnection conn = null;
	private Manager HAL;

	public TestDumpProperties(){
		try {
			conn = DBusConnection.getConnection(DBusConnection.SYSTEM);
			HAL = conn.getRemoteObject("org.freedesktop.Hal","/org/freedesktop/Hal/Manager",Manager.class);
			String UDI[] = HAL.GetAllDevices();
			for(int i = 0; i < UDI.length; i++)
				dumpProperties(UDI[i]);
			
		} catch (DBusException DBe) {
			System.out.println("Could not connect to bus: " + DBe);
			System.exit(1);
		}
		conn.disconnect();
			
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
	
	public static void main(String[] args){
		new TestDumpProperties();
	}
	
}
