
package jcu.sal.comms.transport.tcp;

import jcu.sal.comms.transport.ClientTransport;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.transport.TransportCommand;

public class TcpClientTransport implements ClientTransport {

	private TransportResponseListener rl = null;

	public TcpClientTransport() {
	}

	public void setup() {
	}

	public void shutdown() {
	}

	public void send(TransportCommand tc) {
	}

	public void setResponseListener(TransportResponseListener rl) {
		this.rl = rl;
	}
}
