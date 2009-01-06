
package jcu.sal.comms.transport;

import jcu.sal.comms.Command;

import javax.xml.bind.JAXBException;

public class TransportCommand extends Command {

	private int id;

	public TransportCommand() {
		super();
	}

	public TransportCommand(Command c, int id) {
		super();
		this.message = c.getMessage();
		this.id = id;
	}

	public TransportCommand(String xmlString) throws JAXBException {
		super(xmlString);
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String toString() {
		return "ID: " + id + " " + super.toString();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !(o instanceof TransportCommand)) {
			return false;
		}

		TransportCommand tc = (TransportCommand) o;

		return (super.equals(tc) && id == tc.id);
	}

	public int hashCode() {
		return super.hashCode() + 17 * id;
	}
}
