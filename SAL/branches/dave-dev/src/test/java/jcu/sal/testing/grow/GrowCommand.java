
package jcu.sal.testing.grow;

import jcu.sal.message.Message;
import jcu.sal.message.InvalidMessageException;

public class GrowCommand extends Message {

	public GrowCommand() {
		super();
		init();
	}

	public GrowCommand(Message message) throws InvalidMessageException {
		super(message);

		if (message != null && !message.getName().equals(GrowMessageFactory.GROW_COMMAND_NAME)) {
			String em = "Incompatible message name -";
			em += " Expected: " + GrowMessageFactory.GROW_COMMAND_NAME;
			em += " Found: " + message.getName();
			throw new InvalidMessageException(em);
		}

		init();
	}

	public GrowCommand(String inputString, int reps) {
		super();
		init();
		setInputString(inputString);
		setReps(reps);
	}

	private void init() {
		try {
			setName(GrowMessageFactory.GROW_COMMAND_NAME);
			setDescription(GrowMessageFactory.getDescription(GrowMessageFactory.GROW_COMMAND_NAME));
		} catch (InvalidMessageException ime) {
		}
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
