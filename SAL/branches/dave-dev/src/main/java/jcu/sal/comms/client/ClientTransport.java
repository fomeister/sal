
package jcu.sal.comms.client;

import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.common.Command;

public interface ClientTransport {
	public void setup();
	public void shutdown();

	public void send(int command_id, Command c);
	public void setResponseListener(TransportResponseListener rl);
}
