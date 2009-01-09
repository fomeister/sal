
package jcu.sal.comms.transport.local;

import jcu.sal.comms.transport.TransportTester;

import jcu.sal.testing.grow.GrowProcessorTester;
import jcu.sal.testing.alltypes.AllTypesProcessorTester;

import org.junit.Test;

public class LocalTransportTest {

	@Test
	public void testLocalTransport() throws Exception {
		TransportTester tester = new TransportTester();

		LocalTransport localTransport = new LocalTransport();

		tester.testTransport(localTransport, localTransport, new GrowProcessorTester());
		tester.testTransport(localTransport, localTransport, new AllTypesProcessorTester());
	}

}
