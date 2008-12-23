
package jcu.sal.comms.listeners;

import jcu.sal.xml.TransportCommand;

public interface TransportCommandListener {
	public void receivedCommand(TransportCommand tc);
}

