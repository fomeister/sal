
package jcu.sal.testing.grow;

import jcu.sal.message.Message;
import jcu.sal.message.InvalidMessageException;
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

	public void receivedResponse(Message m) {
		if (numResponses > expectedNumResponses) {
			fail("More responses recieved than expected.");
			return;
		}

		assertTrue(m.getName().equals(GrowMessageFactory.GROW_SEQUENCE_RESPONSE_NAME));

		GrowSequenceResponse gsr = null;
		try {
			gsr = new GrowSequenceResponse(m);
		} catch (InvalidMessageException ime) {
			fail(ime.getMessage());
		}

		assertTrue(gsr.getOutputString().equals(expectedResponse));

		expectedResponse += baseString;
		numResponses++;

		if (gsr.isFinal()) {
			assertTrue(numResponses == expectedNumResponses);
		}
	}
}

