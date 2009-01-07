
package jcu.sal.comms.transport;

import jcu.sal.comms.MessageProcessor;

public interface ServerTransport {
	public void setup() throws Exception;
	public void shutdown() throws Exception;

	public void setProcessor(MessageProcessor processor);
}
