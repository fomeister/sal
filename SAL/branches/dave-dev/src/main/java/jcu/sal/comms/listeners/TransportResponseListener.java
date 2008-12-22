
package jcu.sal.comms.listeners;

import jcu.sal.comms.common.Response;

public interface TransportResponseListener {
	public void receivedResponse(int command_id, Response r);
}
