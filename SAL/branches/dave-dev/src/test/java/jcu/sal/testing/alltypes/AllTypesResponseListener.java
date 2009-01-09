
package jcu.sal.testing.alltypes;

import jcu.sal.message.Message;
import jcu.sal.message.InvalidMessageException;
import jcu.sal.comms.listeners.ResponseListener;

import static org.junit.Assert.*;

public class AllTypesResponseListener implements ResponseListener {

	private Message expectedResponse;

	public AllTypesResponseListener(Message expectedResponse) {
		this.expectedResponse = expectedResponse;
	}

	public void receivedResponse(Message m) {
		assertTrue(expectedResponse.equals(m));
	}
}

