
package jcu.sal.comms.transport;

import jcu.sal.comms.ProcessorTester;
import jcu.sal.comms.ClientCommsManager;
import jcu.sal.comms.ServerCommsManager;

public class TransportTester {

	public void testTransport(ClientTransport clientTransport, ServerTransport serverTransport, ProcessorTester tester) throws Exception {
		ClientCommsManager client = new ClientCommsManager();
		ServerCommsManager server = new ServerCommsManager();

		client.setTransport(clientTransport);
		server.setTransport(serverTransport);
		server.setProcessor(tester.getProcessor());

		server.setup();
		client.setup();

		tester.testProcessor(client);

		client.shutdown();
		server.shutdown();
	}
}
