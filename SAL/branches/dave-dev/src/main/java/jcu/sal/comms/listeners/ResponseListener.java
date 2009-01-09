
package jcu.sal.comms.listeners;

import jcu.sal.message.Message;

public interface ResponseListener {
	public void receivedResponse(Message m);
}
