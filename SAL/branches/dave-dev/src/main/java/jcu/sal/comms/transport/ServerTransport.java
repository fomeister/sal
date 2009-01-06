
package jcu.sal.comms.transport;

import jcu.sal.comms.listeners.TransportCommandListener;

public interface ServerTransport {
	public void setup();
	public void shutdown();

	public void send(TransportResponse tr);
	public void setCommandListener(TransportCommandListener cl);
}
