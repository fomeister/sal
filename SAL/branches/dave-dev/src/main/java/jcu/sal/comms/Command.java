
package jcu.sal.comms;

import jcu.sal.xml.Message;
import jcu.sal.xml.MessageDescription;
import jcu.sal.xml.Argument;
import jcu.sal.xml.ArgumentDescription;
import jcu.sal.xml.XMLHelper;

import java.util.List;

import javax.xml.bind.JAXBException;

public class Command {

	protected Message message = null;

	public Command() {
		message = new Message();
	}

	public Command(Command command) {
		this.message = command.getMessage();
	}

	public Command(String xmlString) throws JAXBException {
		message = (Message) XMLHelper.fromXmlString(xmlString);
	}

	public String toXmlString() throws JAXBException {
		return XMLHelper.toXmlString(message);
	}

	public Message getMessage() {
		return message;
	}

	public boolean valid() {
		return true;
	}

	public List<Argument> getArgument() {
		return message.getArgument();
	}

	public int getValueSize(int argIndex) {
		if (getArgument().size() < argIndex + 1) {
			return -1;
		} else {
			return getArgument().get(argIndex).getValue().size();
		}
	}

	public String getValue(int argIndex) {
		if (getArgument().size() < argIndex + 1 || getArgument().get(argIndex).getValue().size() != 1) {
			return null;
		} else {
			return getArgument().get(argIndex).getValue().get(0);
		}
	}

	public String getValue(int argIndex, int valIndex) {
		if (getArgument().size() < argIndex + 1 || getArgument().get(argIndex).getValue().size() < valIndex + 1) {
			return null;
		} else {
			return getArgument().get(argIndex).getValue().get(valIndex);
		}
	}

	public void setValue(int argIndex, String value) {
		Argument arg = null;

		while (getArgument().size() < argIndex + 1) {
			getArgument().add(new Argument());
		}

		arg = getArgument().get(argIndex);

		if (arg.getValue().size() < 1) {
			arg.getValue().add(value);
		} else {
			arg.getValue().set(0, value);
		}

		getArgument().set(argIndex, arg);
	}

	public void setValue(int argIndex, int valIndex, String value, String defaultValue) {
		Argument arg = null;

		while (getArgument().size() < argIndex + 1) {
			getArgument().add(new Argument());
		}

		arg = getArgument().get(argIndex);

		while (arg.getValue().size() < valIndex + 1) {
			arg.getValue().add(defaultValue);
		}

		arg.getValue().set(valIndex, value);

		getArgument().set(argIndex, arg);
	}

	public String getName() {
		return message.getName();
	}

	public void setName(String value) {
		message.setName(value);
	}

	public String toString() {
		return message.toString();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || !(object instanceof Command)) {
			return false;
		}

		Command c = (Command) object;

		return (message.equals(c.message));
	}

	public int hashCode() {
		return message.hashCode();
	}
}
