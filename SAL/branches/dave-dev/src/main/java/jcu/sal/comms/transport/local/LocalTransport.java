
package jcu.sal.comms.transport.local;

import jcu.sal.comms.Message;
import jcu.sal.comms.TransportMessage;
import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.listeners.TransportResponseListenerAdapter;
import jcu.sal.comms.transport.ClientTransport;
import jcu.sal.comms.transport.ServerTransport;

public class LocalTransport implements ClientTransport, ServerTransport {

	private TransportResponseListener responseListener = null;
	private MessageProcessor processor = null;

	public LocalTransport() {
	}

	public void setup() {
	}

	public void shutdown() {
	}

	public void send(TransportMessage tm) {
		processor.process(tm, new TransportResponseListenerAdapter(responseListener, tm.getId()));
	}

	public void setResponseListener(TransportResponseListener responseListener) {
		this.responseListener = responseListener;
	}

	public void setProcessor(MessageProcessor processor) {
		this.processor = processor;
	}
}
