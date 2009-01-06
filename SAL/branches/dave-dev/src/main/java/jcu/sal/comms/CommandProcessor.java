
package jcu.sal.comms;

import jcu.sal.comms.Command;
import jcu.sal.comms.listeners.ResponseListener;

public interface CommandProcessor {
	public void process(Command c, ResponseListener rl);
}
