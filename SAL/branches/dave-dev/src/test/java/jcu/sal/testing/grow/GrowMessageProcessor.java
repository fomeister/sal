
package jcu.sal.testing.grow;

import jcu.sal.message.Message;
import jcu.sal.message.InvalidMessageException;
import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.xml.Argument;

import static org.junit.Assert.*;

public class GrowMessageProcessor implements MessageProcessor {

	public void process(Message m, ResponseListener rl) {
		try {
			if (m.getName().equals(GrowMessageFactory.GROW_COMMAND_NAME)) {
				processGrow(new GrowCommand(m), rl);
			} else if (m.getName().equals(GrowMessageFactory.GROW_SEQUENCE_COMMAND_NAME)) {
				processGrowSequence(new GrowSequenceCommand(m), rl);
			}
		} catch (InvalidMessageException ime) {
			fail(ime.getMessage());
		}
	}

	private void processGrow(GrowCommand gc, ResponseListener rl) {
		String inputString = gc.getInputString();
		int reps = gc.getReps();

		String[] response = new String[reps];

		String s = "";

		for (int i = 0; i < reps; ++i) {
			s += inputString;
			response[i] = s;
		}

		rl.receivedResponse(GrowMessageFactory.createGrowResponse(response));
	}

	private void processGrowSequence(GrowSequenceCommand gsc, ResponseListener rl) {
		String inputString = gsc.getInputString();
		int reps = gsc.getReps();

		String s = "";

		for (int i = 0; i < reps; ++i) {
			s += inputString;
			rl.receivedResponse(GrowMessageFactory.createGrowSequenceResponse(s, (i == reps - 1)));
		}
	}
}
