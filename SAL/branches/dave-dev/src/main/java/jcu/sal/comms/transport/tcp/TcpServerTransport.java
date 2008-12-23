
package jcu.sal.comms.transport.tcp;

import jcu.sal.comms.transport.ServerTransport;
import jcu.sal.comms.listeners.TransportCommandListener;
import jcu.sal.xml.TransportResponse;

public class TcpServerTransport implements ServerTransport {

	private TransportCommandListener cl = null;

	public TcpServerTransport() {
	}

	public void setup() {
	}

	public void shutdown() {
	}

	public void send(TransportResponse tr) {
	}

	public void setCommandListener(TransportCommandListener cl) {
		this.cl = cl;
	}
}
