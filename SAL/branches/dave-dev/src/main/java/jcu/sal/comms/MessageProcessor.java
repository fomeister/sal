
package jcu.sal.comms;

import jcu.sal.message.Message;
import jcu.sal.comms.listeners.ResponseListener;

public interface MessageProcessor {
	public void process(Message m, ResponseListener rl);
}
