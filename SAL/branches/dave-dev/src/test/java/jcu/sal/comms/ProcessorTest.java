
package jcu.sal.comms;

import jcu.sal.comms.transport.TransportTester;

import jcu.sal.testing.grow.GrowProcessorTester;
import jcu.sal.testing.alltypes.AllTypesProcessorTester;

import org.junit.Test;

public class ProcessorTest {

	@Test
	public void testDirectProcessor() {
		GrowProcessorTester gpt = new GrowProcessorTester();
		gpt.testProcessor(gpt.getProcessor());

		AllTypesProcessorTester atpt = new AllTypesProcessorTester();
		atpt.testProcessor(atpt.getProcessor());
	}

}

