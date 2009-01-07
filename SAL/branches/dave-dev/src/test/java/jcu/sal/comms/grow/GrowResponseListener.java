
package jcu.sal.comms.grow;

import jcu.sal.comms.Message;
import jcu.sal.comms.InvalidMessageException;
import jcu.sal.comms.listeners.ResponseListener;

import static org.junit.Assert.*;

public class GrowResponseListener implements ResponseListener {

	private int numResponses;
	private String baseResponse;
	private int reps;

	public GrowResponseListener(String baseResponse, int reps) {
		numResponses = 0;
		this.baseResponse = baseResponse;
		this.reps = reps;
	}

	public int getNumResponses() {
		return numResponses;
	}

	public void receivedResponse(Message m) {
		if (numResponses > 1) {
			fail("More responses recieved than expected.");
			return;
		}

		assertTrue(m.getName().equals(GrowMessageFactory.GROW_RESPONSE_NAME));

		GrowResponse gr = null;
		try {
			gr = new GrowResponse(m);
		} catch (InvalidMessageException ime) {
			fail(ime.getMessage());
		}

		String[] response = gr.getOutputStrings();

		String s = baseResponse;

		assertTrue(response.length == reps);

		for (int i = 0; i < reps; ++i) {
			assertTrue(response[i].equals(s));
			s += baseResponse;
		}

		numResponses++;
	}
}

