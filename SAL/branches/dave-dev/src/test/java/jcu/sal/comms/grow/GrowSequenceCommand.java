
package jcu.sal.comms.grow;

import jcu.sal.comms.Command;

public class GrowSequenceCommand extends Command {

	public GrowSequenceCommand() {
		super();
		setName("GrowSequenceCommand");
	}

	public GrowSequenceCommand(String inputString, int reps) {
		this();
		setInputString(inputString);
		setReps(reps);
	}

	public GrowSequenceCommand(Command command) {
		super(command);
		setName("GrowSequenceCommand");
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
