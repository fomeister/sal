
package jcu.sal.testing.grow;

import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.ProcessorTester;

public class GrowProcessorTester implements ProcessorTester {

	public MessageProcessor getProcessor() {
		return new GrowMessageProcessor();
	}

	public void testProcessor(MessageProcessor processor) {
		GrowResponseListener grl = new GrowResponseListener("test", 4);
		GrowSequenceResponseListener gsrl = new GrowSequenceResponseListener("test", 4);
		processor.process(GrowMessageFactory.createGrowCommand("test", 4), grl);
		processor.process(GrowMessageFactory.createGrowSequenceCommand("test", 4), gsrl);
	}

}
