
package jcu.sal.comms.listeners;

import jcu.sal.comms.common.Command;

public interface TransportCommandListener {
	public void receivedCommand(int command_id, Command c);
}

