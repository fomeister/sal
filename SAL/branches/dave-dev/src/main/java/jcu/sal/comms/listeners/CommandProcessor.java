
package jcu.sal.comms.listeners;

import jcu.sal.xml.Command;

public interface CommandProcessor {
	public void process(Command c, ResponseListener rl);
}
