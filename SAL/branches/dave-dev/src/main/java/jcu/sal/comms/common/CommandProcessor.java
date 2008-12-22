
package jcu.sal.comms.common;

import jcu.sal.comms.listeners.ResponseListener;

public interface CommandProcessor {
	public void process(Command c, ResponseListener rl);
}
