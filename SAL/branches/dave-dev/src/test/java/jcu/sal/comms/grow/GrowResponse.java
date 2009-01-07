
package jcu.sal.comms.grow;

import jcu.sal.comms.Message;
import jcu.sal.comms.InvalidMessageException;

public class GrowResponse extends Message {

	public GrowResponse() {
		super();
		init();
	}

	public GrowResponse(Message message) throws InvalidMessageException {
		super(message);

		if (message != null && !message.getName().equals(GrowMessageFactory.GROW_RESPONSE_NAME)) {
			String em = "Incompatible message name -";
			em += " Expected: " + GrowMessageFactory.GROW_RESPONSE_NAME;
			em += " Found: " + message.getName();
			throw new InvalidMessageException(em);
		}

		init();
	}

	public GrowResponse(String[] outputStrings) {
		super();
		init();
		setOutputStrings(outputStrings);
	}

	private void init() {
		setName(GrowMessageFactory.GROW_RESPONSE_NAME);
		setFinal(true);
	}

	public void setOutputStrings(String[] outputStrings) {
		for (int i = 0; i < outputStrings.length; ++i) {
			setValue(0, i, outputStrings[i], "");
		}
	}

	public String[] getOutputStrings() {
		int size = getValueSize(0);
		String[] response = new String[size];

		for (int i = 0; i < size; ++i) {
			response[i] = getValue(0, i);
		}

		return response;
	}
}

