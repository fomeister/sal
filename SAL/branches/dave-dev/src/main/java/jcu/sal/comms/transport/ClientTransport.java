
package jcu.sal.comms.transport;

import jcu.sal.comms.TransportMessage;
import jcu.sal.comms.listeners.TransportResponseListener;

public interface ClientTransport {
	public void setup() throws Exception;
	public void shutdown() throws Exception;

	public void send(TransportMessage tm);
	public void setResponseListener(TransportResponseListener rl);
}
