
package jcu.sal.comms.server;

import jcu.sal.comms.common.Command;
import jcu.sal.comms.common.CommandProcessor;
import jcu.sal.comms.common.Response;
import jcu.sal.comms.listeners.TransportCommandListener;
import jcu.sal.comms.listeners.ResponseListener;

public class ServerCommsManager implements TransportCommandListener {

	private ServerTransport transport = null;
	private CommandProcessor processor = null;

	public ServerCommsManager() {
	}

	public void setTransport(ServerTransport transport) {
		this.transport = transport;
	}

	public ServerTransport getTransport() {
		return transport;
	}

	public void setProcessor(CommandProcessor processor) {
		this.processor = processor;
	}

	public CommandProcessor getProcessor() {
		return processor;
	}

	public void setup() {
		transport.setup();
		transport.setCommandListener(this);
	}

	public void shutdown() {
		transport.shutdown();
	}

	private void send(int command_id, Response r) {
		transport.send(command_id, r);
	}

	public void receivedCommand(int command_id, Command c) {
		processor.process(c, new ProcessingResponseListener(this, command_id));
	}

	private class ProcessingResponseListener implements ResponseListener {

		public ServerCommsManager manager;
		public int command_id;

		public ProcessingResponseListener(ServerCommsManager manager, int command_id) {
			this.manager = manager;
			this.command_id = command_id;
		}

		public void receivedResponse(Response r) {
			manager.send(command_id, r);
		}
	}
}
