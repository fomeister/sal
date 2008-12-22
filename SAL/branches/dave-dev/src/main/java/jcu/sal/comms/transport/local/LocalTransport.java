
package jcu.sal.comms.transport.local;

import jcu.sal.comms.client.ClientTransport;
import jcu.sal.comms.common.Command;
import jcu.sal.comms.common.Response;
import jcu.sal.comms.listeners.TransportCommandListener;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.server.ServerTransport;

public class LocalTransport implements ClientTransport, ServerTransport {

	private TransportResponseListener response_listener = null;
	private TransportCommandListener command_listener = null;

	public LocalTransport() {
	}

	public void setup() {
	}

	public void shutdown() {
	}

	public void send(int command_id, Command c) {
		command_listener.receivedCommand(command_id, c);
	}

	public void setResponseListener(TransportResponseListener rl) {
		response_listener = rl;
	}

	public void send(int command_id, Response r) {
		response_listener.receivedResponse(command_id, r);
	}

	public void setCommandListener(TransportCommandListener cl) {
		command_listener = cl;
	}
}
