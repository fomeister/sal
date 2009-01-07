
package jcu.sal.comms.grow;

import jcu.sal.comms.Message;
import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.xml.Argument;

public class GrowMessageProcessor implements MessageProcessor {

	public void process(Message m, ResponseListener rl) {
		if (m.getName().equals(GrowMessageFactory.GROW_COMMAND_NAME)) {
			processGrow(new GrowCommand(m), rl);
		} else if (m.getName().equals(GrowMessageFactory.GROW_SEQUENCE_COMMAND_NAME)) {
			processGrowSequence(new GrowSequenceCommand(m), rl);
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
