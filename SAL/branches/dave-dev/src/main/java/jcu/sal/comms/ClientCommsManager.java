
package jcu.sal.comms;

import java.util.HashMap;

import jcu.sal.comms.listeners.CommandProcessor;
import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.transport.ClientTransport;
import jcu.sal.xml.Command;
import jcu.sal.xml.Response;
import jcu.sal.xml.TransportCommand;
import jcu.sal.xml.TransportResponse;

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

		TransportCommand tc = new TransportCommand();
		tc.setId(command_id);
		tc.setCommand(c);

		transport.send(tc);
		command_id++;
	}

	public void receivedResponse(TransportResponse tr) {
		ResponseListener rl = listeners.get(tr.getId());
		if (rl != null) {
			Response r = tr.getResponse();
			rl.receivedResponse(r);
			if (r.isFinalResponse()) {
				listeners.remove(tr.getId());
			}
		}
	}
}
