package au.edu.jcu.haldbus.listener;

import org.freedesktop.Hal.Manager;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;

public class TestListenerDeviceRemoved implements DBusSigHandler<Manager.DeviceRemoved>{

	public synchronized void handle(Manager.DeviceRemoved s) {
		System.out.println("SIGNAL!!");
		System.out.println("Got a DeviceRemoved signal from "+ s.obj + " for "+s.udiRemoved);
	}

	public TestListenerDeviceRemoved(DBusConnection c) {
		System.out.println("TestListenerDeviceRemoved added");
	}
	
}
