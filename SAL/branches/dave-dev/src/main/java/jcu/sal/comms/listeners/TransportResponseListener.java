
package jcu.sal.comms.listeners;

import jcu.sal.comms.TransportMessage;

public interface TransportResponseListener {
	public void receivedResponse(TransportMessage tm);
}
