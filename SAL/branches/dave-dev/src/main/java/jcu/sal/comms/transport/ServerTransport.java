
package jcu.sal.comms.transport;

import jcu.sal.comms.listeners.TransportCommandListener;
import jcu.sal.xml.TransportResponse;

public interface ServerTransport {
	public void setup();
	public void shutdown();

	public void send(TransportResponse tr);
	public void setCommandListener(TransportCommandListener cl);
}
