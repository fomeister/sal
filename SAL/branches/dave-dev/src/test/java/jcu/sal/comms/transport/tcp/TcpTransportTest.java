
package jcu.sal.comms.transport.tcp;

import jcu.sal.comms.transport.tcp.codec.xml.XmlCodecFactory;
import jcu.sal.comms.transport.tcp.codec.binary.BinaryCodecFactory;

import jcu.sal.comms.transport.TransportTester;

import jcu.sal.testing.grow.GrowProcessorTester;
import jcu.sal.testing.alltypes.AllTypesProcessorTester;

import org.junit.Test;

public class TcpTransportTest {

	@Test
	public void testTcpTransportWithXmlCodec() throws Exception {
		TransportTester tester = new TransportTester();

		TcpClientTransport clientTransport = new TcpClientTransport();
		TcpServerTransport serverTransport = new TcpServerTransport();

		clientTransport.setPort(9000);
		clientTransport.setHost("localhost");
		clientTransport.setCodecFactory(new XmlCodecFactory());
		serverTransport.setPort(9000);
		serverTransport.setCodecFactory(new XmlCodecFactory());

		tester.testTransport(clientTransport, serverTransport, new GrowProcessorTester());
		tester.testTransport(clientTransport, serverTransport, new AllTypesProcessorTester());
	}

	@Test
	public void testTcpTransportWithBinaryCodec() throws Exception {
		TransportTester tester = new TransportTester();

		TcpClientTransport clientTransport = new TcpClientTransport();
		TcpServerTransport serverTransport = new TcpServerTransport();

		clientTransport.setPort(9000);
		clientTransport.setHost("localhost");
		clientTransport.setCodecFactory(new BinaryCodecFactory());
		serverTransport.setPort(9000);
		serverTransport.setCodecFactory(new BinaryCodecFactory());

		tester.testTransport(clientTransport, serverTransport, new GrowProcessorTester());
		tester.testTransport(clientTransport, serverTransport, new AllTypesProcessorTester());
	}

}
