
package jcu.sal.comms.grow;

import jcu.sal.comms.Response;
import jcu.sal.comms.listeners.ResponseListener;

import static org.junit.Assert.*;

public class GrowSequenceResponseListener implements ResponseListener {

	private int numResponses;
	private int expectedNumResponses;
	private String baseString;
	private String expectedResponse;

	public GrowSequenceResponseListener(String baseString, int expectedNumResponses) {
		numResponses = 0;
		this.expectedNumResponses = expectedNumResponses;
		this.baseString = baseString;
		this.expectedResponse = baseString;
	}

	public int getNumResponses() {
		return numResponses;
	}

	public void receivedResponse(Response r) {
		if (numResponses > expectedNumResponses) {
			fail("More responses recieved than expected.");
			return;
		}

		assertTrue(r.getName().equals("GrowSequenceResponse"));
		GrowSequenceResponse gsr = new GrowSequenceResponse(r);

		assertTrue(gsr.getOutputString().equals(expectedResponse));

		expectedResponse += baseString;
		numResponses++;

		if (gsr.isFinal()) {
			assertTrue(numResponses == expectedNumResponses);
		}
	}
}

