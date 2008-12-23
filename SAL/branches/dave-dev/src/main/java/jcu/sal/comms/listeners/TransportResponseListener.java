
package jcu.sal.comms.listeners;

import jcu.sal.xml.TransportResponse;

public interface TransportResponseListener {
	public void receivedResponse(TransportResponse tr);
}
