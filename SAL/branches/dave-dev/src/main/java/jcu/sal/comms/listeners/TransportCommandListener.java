
package jcu.sal.comms.listeners;

import jcu.sal.comms.transport.TransportCommand;

public interface TransportCommandListener {
	public void receivedCommand(TransportCommand tc);
}

