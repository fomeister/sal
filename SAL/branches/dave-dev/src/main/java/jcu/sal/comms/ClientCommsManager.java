
package jcu.sal.comms;

import java.util.HashMap;

import jcu.sal.message.Message;
import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.TransportMessage;
import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.transport.ClientTransport;

public class ClientCommsManager implements TransportResponseListener, MessageProcessor {

	private ClientTransport transport = null;

	private int message_id;
	private HashMap<Integer, ResponseListener> listeners;

	public ClientCommsManager() {
		message_id = 0;
		listeners = new HashMap<Integer, ResponseListener>();
	}

	public void setTransport(ClientTransport transport) {
		this.transport = transport;
	}

	public ClientTransport getTransport() {
		return transport;
	}

	public void setup() throws Exception {
		transport.setup();
		transport.setResponseListener(this);
	}

	public void shutdown() throws Exception {
		transport.shutdown();
	}

	public void process(Message m, ResponseListener rl) {
		listeners.put(message_id, rl);
		transport.send(new TransportMessage(m, message_id++));
	}

	public void receivedResponse(TransportMessage tm) {
		ResponseListener rl = listeners.get(tm.getId());
		if (rl != null) {
			rl.receivedResponse(tm);
			if (tm.isFinal()) {
				listeners.remove(tm.getId());
			}
		}
	}
}
