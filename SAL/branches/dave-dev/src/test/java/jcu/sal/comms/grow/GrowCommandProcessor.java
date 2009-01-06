
package jcu.sal.comms.grow;

import jcu.sal.comms.Command;
import jcu.sal.comms.CommandProcessor;
import jcu.sal.comms.listeners.ResponseListener;
import jcu.sal.xml.Argument;

public class GrowCommandProcessor implements CommandProcessor {

	public void process(Command c, ResponseListener rl) {
		if (c.getName().equals("GrowCommand")) {
			processGrow(c, rl);
		} else if (c.getName().equals("GrowSequenceCommand")) {
			processGrowSequence(c, rl);
		}
	}

	private void processGrow(Command c, ResponseListener rl) {
		String inputString = c.getArgument().get(0).getValue().get(0);
		int reps = Integer.parseInt(c.getArgument().get(1).getValue().get(0));

		String[] response = new String[reps];

		String s = "";

		for (int i = 0; i < reps; ++i) {
			s += inputString;
			response[i] = s;
		}

		rl.receivedResponse(GrowResponseFactory.createGrowResponse(response));
	}

	private void processGrowSequence(Command c, ResponseListener rl) {
		String inputString = c.getArgument().get(0).getValue().get(0);
		int reps = Integer.parseInt(c.getArgument().get(1).getValue().get(0));

		String s = "";

		for (int i = 0; i < reps; ++i) {
			s += inputString;
			rl.receivedResponse(GrowResponseFactory.createGrowSequenceResponse(s, (i == reps - 1)));
		}
	}
}
