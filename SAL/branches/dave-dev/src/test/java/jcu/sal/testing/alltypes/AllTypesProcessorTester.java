
package jcu.sal.testing.alltypes;

import jcu.sal.message.Message;
import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.ProcessorTester;

public class AllTypesProcessorTester implements ProcessorTester {

	public MessageProcessor getProcessor() {
		return new AllTypesMessageProcessor();
	}

	public void testProcessor(MessageProcessor processor) {
		Message m = AllTypesMessageFactory.createDefaultAllTypesMessage();

		AllTypesResponseListener rl = new AllTypesResponseListener(m);
		processor.process(m, rl);
	}

}
