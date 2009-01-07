
package jcu.sal.comms;

import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.transport.ServerTransport;

public class ServerCommsManager {

	private ServerTransport transport = null;
	private MessageProcessor processor = null;

	public ServerCommsManager() {
	}

	public void setTransport(ServerTransport transport) {
		this.transport = transport;
	}

	public ServerTransport getTransport() {
		return transport;
	}

	public void setProcessor(MessageProcessor processor) {
		this.processor = processor;
	}

	public MessageProcessor getProcessor() {
		return processor;
	}

	public void setup() throws Exception {
		transport.setup();
		transport.setProcessor(processor);
	}

	public void shutdown() throws Exception {
		transport.shutdown();
	}
}
