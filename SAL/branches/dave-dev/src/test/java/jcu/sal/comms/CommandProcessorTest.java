
package jcu.sal.comms;

import jcu.sal.comms.grow.*;
import jcu.sal.comms.CommandProcessor;
import jcu.sal.comms.transport.ClientTransport;
import jcu.sal.comms.transport.ServerTransport;
import jcu.sal.comms.transport.local.LocalTransport;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class CommandProcessorTest {

	private GrowCommandProcessor directProcessor;

	@Before
	public void setUp() {
		directProcessor = new GrowCommandProcessor();
	}

	@After
	public void tearDown() {
		directProcessor = null;
	}

	public void testProcessor(CommandProcessor commandProcessor) {
		GrowResponseListener grl = new GrowResponseListener("test", 4);
		GrowSequenceResponseListener gsrl = new GrowSequenceResponseListener("test", 4);

		commandProcessor.process(GrowCommandFactory.createGrowCommand("test", 4), grl);
		assertTrue(grl.getNumResponses() == 1);

		commandProcessor.process(GrowCommandFactory.createGrowSequenceCommand("test", 4), gsrl);
		assertTrue(gsrl.getNumResponses() == 4);
	}

	public void testTransport(ClientTransport clientTransport, ServerTransport serverTransport) {
		ClientCommsManager client = new ClientCommsManager();
		ServerCommsManager server = new ServerCommsManager();

		client.setTransport(clientTransport);
		server.setTransport(serverTransport);
		server.setProcessor(directProcessor);

		client.setup();
		server.setup();

		testProcessor(client);

		client.shutdown();
		server.shutdown();
	}

	@Test
	public void testDirectProcessor() {
		testProcessor(directProcessor);
	}

	@Test
	public void testLocalTransport() {
		LocalTransport localTransport = new LocalTransport();
		testTransport(localTransport, localTransport);
	}

	@Test
	public void testTcpTransportWithXmlCodec() {
		fail("Not implemented.");
	}

	@Test
	public void testTcpTransportWithBinaryCodec() {
		fail("Not implemented.");
	}
}

