
package jcu.sal.comms.listeners;

import jcu.sal.comms.Message;
import jcu.sal.comms.TransportMessage;

public class TransportResponseListenerAdapter implements ResponseListener {

	private int id;
	private TransportResponseListener responseListener;

	public TransportResponseListenerAdapter(TransportResponseListener responseListener, int id) {
		this.responseListener = responseListener;
		this.id = id;
	}

	public void receivedResponse(Message m) {
		responseListener.receivedResponse(new TransportMessage(m, id));
	}
}
