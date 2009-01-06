
package jcu.sal.comms.listeners;

import jcu.sal.comms.transport.TransportResponse;

public interface TransportResponseListener {
	public void receivedResponse(TransportResponse tr);
}
