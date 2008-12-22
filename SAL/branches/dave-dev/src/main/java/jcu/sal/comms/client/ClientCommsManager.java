
package jcu.sal.comms.client;

import java.util.HashMap;

import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.common.Command;
import jcu.sal.comms.common.CommandProcessor;
import jcu.sal.comms.common.Response;

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
		transport.send(command_id, c);
		command_id++;
	}

	public void receivedResponse(int command_id, Response r) {
		ResponseListener rl = listeners.get(command_id);
		if (rl != null) {
			rl.receivedResponse(r);
			if (r.isFinalResponse()) {
				listeners.remove(command_id);
			}
		}
	}
}
