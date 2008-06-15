package jcu.sal.common.events;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jcu.sal.events.Event;

public interface RMIEventHandler extends Remote {
	public void handle(Event e) throws RemoteException;
}
