
package jcu.sal.comms.listeners;

import jcu.sal.comms.Command;

public interface CommandListener {
	public void receivedCommand(Command c);
}
