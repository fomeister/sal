
package jcu.sal.comms;

import java.util.HashMap;

import jcu.sal.comms.CommandProcessor;
import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.transport.ClientTransport;
import jcu.sal.comms.transport.TransportCommand;
import jcu.sal.comms.transport.TransportResponse;

public class ClientCommsManager implements TransportResponseListener, CommandProcessor {

	private ClientTransport transport = null;

	private int command_id;
	private HashMap<Integer, ResponseListener> listeners;

	public ClientCommsManager() {
		command_id = 0;
		listeners = new HashMap<Integer, ResponseListener>();
	}

	public void setTransport(ClientTransport transport) {
		this.transport = transport;
	}

	public ClientTransport getTransport() {
		return transport;
	}

	public void setup() {
		transport.setup();
		transport.setResponseListener(this);
	}

	public void shutdown() {
		transport.shutdown();
	}

	public void process(Command c, ResponseListener rl) {
		listeners.put(command_id, rl);
		transport.send(new TransportCommand(c, command_id++));
	}

	public void receivedResponse(TransportResponse tr) {
		ResponseListener rl = listeners.get(tr.getId());
		if (rl != null) {
			rl.receivedResponse(tr);
			if (tr.isFinal()) {
				listeners.remove(tr.getId());
			}
		}
	}
}
