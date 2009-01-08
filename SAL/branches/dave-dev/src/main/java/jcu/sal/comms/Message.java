
package jcu.sal.comms;

import jcu.sal.xml.MessageContent;
import jcu.sal.xml.MessageDescription;
import jcu.sal.xml.Argument;
import jcu.sal.xml.JaxbHelper;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

public class Message {

	protected MessageContent content;
	protected MessageDescription description = null;

	public Message() {
		content = new MessageContent();
	}

	public Message(Message message) {
		this.content = message.getContent();
		this.description = message.getDescription();
	}

	public Message(InputStream is) throws JAXBException {
		content = (MessageContent) JaxbHelper.fromInputStream(is);
	}

	public Message(String xmlString) throws JAXBException {
		content = (MessageContent) JaxbHelper.fromXmlString(xmlString);
	}

	public String toXmlString() throws JAXBException {
		return JaxbHelper.toXmlString(content);
	}

	public MessageContent getContent() {
		return content;
	}

	public void setDescription(MessageDescription description) {
		this.description = description;
	}

	public MessageDescription getDescription() {
		return description;
	}

	public void validate() throws InvalidMessageException {
		MessageValidator.validate(content, description);
	}

	public List<Argument> getArgument() {
		return content.getArgument();
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
		return content.getName();
	}

	public void setName(String value) {
		content.setName(value);
	}

	public Boolean isFinal() {
		return content.isFinal();
	}

	public void setFinal(Boolean isFinal) {
		content.setFinal(isFinal);
	}

	public String toString() {
		return content.toString();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || !(object instanceof Message)) {
			return false;
		}

		Message m = (Message) object;

		return (content.equals(m.content));
	}

	public int hashCode() {
		return content.hashCode();
	}
}
