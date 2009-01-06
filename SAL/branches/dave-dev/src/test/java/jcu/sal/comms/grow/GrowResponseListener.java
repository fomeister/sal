
package jcu.sal.comms.grow;

import jcu.sal.comms.Response;
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

	public void receivedResponse(Response r) {
		if (numResponses > 1) {
			fail("More responses recieved than expected.");
			return;
		}

		assertTrue(r.getName().equals("GrowResponse"));
		GrowResponse gr = new GrowResponse(r);

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

