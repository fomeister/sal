
package jcu.sal.comms.grow;

import jcu.sal.comms.Message;
import jcu.sal.comms.InvalidMessageException;

public class GrowSequenceResponse extends Message {

	public GrowSequenceResponse() {
		super();
		init();
	}

	public GrowSequenceResponse(Message message) throws InvalidMessageException {
		super(message);

		if (message != null && !message.getName().equals(GrowMessageFactory.GROW_SEQUENCE_RESPONSE_NAME)) {
			String em = "Incompatible message name -";
			em += " Expected: " + GrowMessageFactory.GROW_SEQUENCE_RESPONSE_NAME;
			em += " Found: " + message.getName();
			throw new InvalidMessageException(em);
		}

		init();
	}

	public GrowSequenceResponse(String outputString, boolean isFinal) {
		super();
		init();
		setOutputString(outputString);
		setFinal(isFinal);
	}

	private void init() {
		setName(GrowMessageFactory.GROW_SEQUENCE_RESPONSE_NAME);
	}

	public void setOutputString(String outputString) {
		setValue(0, outputString);
	}

	public String getOutputString() {
		return getValue(0);
	}
}

