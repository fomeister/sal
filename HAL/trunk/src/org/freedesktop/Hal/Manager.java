package org.freedesktop.Hal;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

public interface Manager extends DBusInterface {
    /**
     * DeviceAdded signal
     */
    public class DeviceAdded extends DBusSignal {
        public final String obj;
        public final String udiAdded;
        /**
         * Default constructor
         * @param obj the UDI of the object which generated the signal
         * (always org.freedesktop.Hal.Manager in this case) 
         * @param udi the UDI of the newly added object
         * @throws DBusException
         */
        public DeviceAdded(String obj, String udi) throws DBusException {
            super(obj, udi);
            this.obj = obj;
            this.udiAdded = udi;
        }
    }
    
    /**
     * DeviceRemoved signal
     */
    public class DeviceRemoved extends DBusSignal {
        public final String obj;
        public final String udiRemoved;
        /**
         * Default constructor
         * @param obj the UDI of the object which generated the signal
         * (always org.freedesktop.Hal.Manager in this case) 
         * @param udi the UDI of the removed object
         * @throws DBusException
         */
        public DeviceRemoved(String obj, String udi) throws DBusException {
            super(obj, udi);
            this.obj = obj;
            this.udiRemoved = udi;
        }
    }
}

