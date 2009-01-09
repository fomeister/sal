
package jcu.sal.testing.grow;

import jcu.sal.message.Message;
import jcu.sal.message.InvalidMessageException;

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
		try {
			setName(GrowMessageFactory.GROW_SEQUENCE_RESPONSE_NAME);
			setDescription(GrowMessageFactory.getDescription(GrowMessageFactory.GROW_SEQUENCE_RESPONSE_NAME));
		} catch (InvalidMessageException ime) {
		}
	}

	public void setOutputString(String outputString) {
		setValue(0, outputString);
	}

	public String getOutputString() {
		return getValue(0);
	}
}

