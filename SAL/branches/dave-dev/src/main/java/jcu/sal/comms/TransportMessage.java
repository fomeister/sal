
package jcu.sal.comms;

import javax.xml.bind.JAXBException;

public class TransportMessage extends Message {

	private int id;

	public TransportMessage() {
		super();
	}

	public TransportMessage(Message m, int id) {
		super(m);
		this.id = id;
	}

	public TransportMessage(String xmlString, int id) throws JAXBException {
		super(xmlString);
		this.id = id;
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

		if (o == null || !(o instanceof TransportMessage)) {
			return false;
		}

		TransportMessage tm = (TransportMessage) o;

		return (super.equals(tm) && id == tm.id);
	}

	public int hashCode() {
		return super.hashCode() + 17 * id;
	}
}
