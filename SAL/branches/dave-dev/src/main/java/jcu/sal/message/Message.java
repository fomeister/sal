
package jcu.sal.message;

import jcu.sal.message.type.TypeFactory;
import jcu.sal.message.type.SingleType;
import jcu.sal.message.type.ArrayType;

import jcu.sal.xml.MessageContent;
import jcu.sal.xml.MessageDescription;
import jcu.sal.xml.Argument;

import jcu.sal.xml.JaxbHelper;
import jcu.sal.xml.XsdHelper;
import jcu.sal.xml.XmlException;

import java.io.InputStream;
import java.util.List;

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

	public Message(String xmlString) throws InvalidMessageException {
		try {
			content = (MessageContent) JaxbHelper.fromXmlString(xmlString);
		} catch (XmlException xe) {
			throw new InvalidMessageException(xe);
		}
		validate();
	}

	public String toXmlString() throws InvalidMessageException {
		try {
			return JaxbHelper.toXmlString(content);
		} catch (XmlException xe) {
			throw new InvalidMessageException(xe);
		}
	}

	public MessageContent getContent() {
		return content;
	}

	public void setDescription(MessageDescription description) throws InvalidMessageException {
		this.description = description;
		validate();
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
		String s = "";

		s += getName();
		s += "(";

		TypeFactory typeFactory = new TypeFactory();

		int size = getArgument().size();
		for (int i = 0; i < size; ++i) {

			Argument arg = getArgument().get(i);

			String type = "string";
			boolean array = (arg.getValue().size() != 1);

			if (description != null && description.getArgument().size() > i) {
				type = description.getArgument().get(i).getType().value();
				array = description.getArgument().get(i).isArray();
			}

			if (array) {
				ArrayType at = (ArrayType) typeFactory.createType(type, array);

				if (at != null) {
					s += at.toString(arg.getValue().toArray(new String[0]));
				} else {
					s += "#bad array type#";
				}

			} else {
				SingleType st = (SingleType) typeFactory.createType(type, array);

				if (st != null) {
					s += st.toString(arg.getValue().get(0));
				} else {
					s += "#bad type#";
				}
			}

			if (i != size - 1) {
				s += ", ";
			}
		}

		s += ")";

		if (!isFinal()) {
			s += "+";
		}

		return s;
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
