
package jcu.sal.comms.server;

import jcu.sal.comms.common.Response;
import jcu.sal.comms.listeners.TransportCommandListener;

public interface ServerTransport {
	public void setup();
	public void shutdown();

	public void send(int command_id, Response r);
	public void setCommandListener(TransportCommandListener cl);
}
