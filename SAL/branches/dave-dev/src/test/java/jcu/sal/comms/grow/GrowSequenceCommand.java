
package jcu.sal.comms.grow;

import jcu.sal.comms.Message;

public class GrowSequenceCommand extends Message {

	public GrowSequenceCommand() {
		super();
		init();
	}

	public GrowSequenceCommand(Message message) {
		super(message);
		init();
	}

	public GrowSequenceCommand(String inputString, int reps) {
		super();
		init();
		setInputString(inputString);
		setReps(reps);
	}

	private void init() {
		setName(GrowMessageFactory.GROW_SEQUENCE_COMMAND_NAME);
	}

	public void setInputString(String inputString) {
		setValue(0, inputString);
	}

	public String getInputString() {
		return getValue(0);
	}

	public void setReps(int reps) {
		setValue(1, String.valueOf(reps));
	}

	public int getReps() {
		return Integer.parseInt(getValue(1));
	}
}
