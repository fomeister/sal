
package jcu.sal.comms.transport;

import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.xml.TransportCommand;

public interface ClientTransport {
	public void setup();
	public void shutdown();

	public void send(TransportCommand tc);
	public void setResponseListener(TransportResponseListener rl);
}
