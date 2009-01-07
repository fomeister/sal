
package jcu.sal.comms.grow;

import jcu.sal.comms.Message;

public class GrowSequenceResponse extends Message {

	public GrowSequenceResponse() {
		super();
		init();
	}

	public GrowSequenceResponse(Message message) {
		super(message);
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

