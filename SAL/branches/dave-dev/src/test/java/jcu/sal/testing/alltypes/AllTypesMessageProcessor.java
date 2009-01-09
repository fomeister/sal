
package jcu.sal.testing.alltypes;

import jcu.sal.message.Message;
import jcu.sal.message.InvalidMessageException;
import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.xml.Argument;

public class AllTypesMessageProcessor implements MessageProcessor {

	public void process(Message m, ResponseListener rl) {
		if (m.getName().equals(AllTypesMessageFactory.ALL_TYPES_MESSAGE_NAME)) {
				rl.receivedResponse(m);
		}
	}
}
