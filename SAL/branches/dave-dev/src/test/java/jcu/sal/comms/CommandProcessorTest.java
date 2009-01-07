
package jcu.sal.comms;

import jcu.sal.comms.grow.*;
import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.transport.ClientTransport;
import jcu.sal.comms.transport.ServerTransport;
import jcu.sal.comms.transport.local.LocalTransport;
import jcu.sal.comms.transport.tcp.TcpClientTransport;
import jcu.sal.comms.transport.tcp.TcpServerTransport;
import jcu.sal.comms.transport.tcp.codec.xml.XmlCodecFactory;
import jcu.sal.comms.transport.tcp.codec.binary.BinaryCodecFactory;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class CommandProcessorTest {

	private MessageProcessor directProcessor;

	@Before
	public void setUp() {
		directProcessor = new GrowMessageProcessor();
	}

	@After
	public void tearDown() {
		directProcessor = null;
	}

	public void testProcessor(MessageProcessor processor) {
		GrowResponseListener grl = new GrowResponseListener("test", 4);
		GrowSequenceResponseListener gsrl = new GrowSequenceResponseListener("test", 4);

		processor.process(GrowMessageFactory.createGrowCommand("test", 4), grl);

//		assertTrue(grl.getNumResponses() == 1);

		processor.process(GrowMessageFactory.createGrowSequenceCommand("test", 4), gsrl);

//		assertTrue(gsrl.getNumResponses() == 4);
	}

	public void testTransport(ClientTransport clientTransport, ServerTransport serverTransport) throws Exception {
		ClientCommsManager client = new ClientCommsManager();
		ServerCommsManager server = new ServerCommsManager();

		client.setTransport(clientTransport);
		server.setTransport(serverTransport);
		server.setProcessor(directProcessor);

		server.setup();
		client.setup();

		testProcessor(client);

		client.shutdown();
		server.shutdown();
	}

	@Test
	public void testDirectProcessor() {
		testProcessor(directProcessor);
	}

	@Test
	public void testLocalTransport() throws Exception {
		LocalTransport localTransport = new LocalTransport();
		testTransport(localTransport, localTransport);
	}

	@Test
	public void testTcpTransportWithXmlCodec() throws Exception {
		TcpClientTransport clientTransport = new TcpClientTransport();
		TcpServerTransport serverTransport = new TcpServerTransport();

		clientTransport.setPort(9000);
		clientTransport.setHost("localhost");
		clientTransport.setCodecFactory(new XmlCodecFactory());
		serverTransport.setPort(9000);
		serverTransport.setCodecFactory(new XmlCodecFactory());

		testTransport(clientTransport, serverTransport);
	}

	@Test
	public void testTcpTransportWithBinaryCodec() throws Exception {
		TcpClientTransport clientTransport = new TcpClientTransport();
		TcpServerTransport serverTransport = new TcpServerTransport();

		clientTransport.setPort(9000);
		clientTransport.setHost("localhost");
		clientTransport.setCodecFactory(new BinaryCodecFactory());
		serverTransport.setPort(9000);
		serverTransport.setCodecFactory(new BinaryCodecFactory());

		testTransport(clientTransport, serverTransport);
	}
}

