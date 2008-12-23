
package jcu.sal.comms.transport.local;

import jcu.sal.comms.listeners.TransportCommandListener;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.transport.ClientTransport;
import jcu.sal.comms.transport.ServerTransport;
import jcu.sal.xml.Command;
import jcu.sal.xml.Response;
import jcu.sal.xml.TransportCommand;
import jcu.sal.xml.TransportResponse;

public class LocalTransport implements ClientTransport, ServerTransport {

	private TransportResponseListener response_listener = null;
	private TransportCommandListener command_listener = null;

	public LocalTransport() {
	}

	public void setup() {
	}

	public void shutdown() {
	}

	public void send(TransportCommand tc) {
		command_listener.receivedCommand(tc);
	}

	public void setResponseListener(TransportResponseListener rl) {
		response_listener = rl;
	}

	public void send(TransportResponse tr) {
		response_listener.receivedResponse(tr);
	}

	public void setCommandListener(TransportCommandListener cl) {
		command_listener = cl;
	}
}
