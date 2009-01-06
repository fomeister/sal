
package jcu.sal.comms.transport;

import jcu.sal.comms.Response;

import javax.xml.bind.JAXBException;

public class TransportResponse extends Response {

	private int id;

	public TransportResponse() {
		super();
	}

	public TransportResponse(Response r, int id) {
		super();
		message = r.getMessage();
		this.id = id;
	}

	public TransportResponse(String xmlString) throws JAXBException {
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

		if (o == null || !(o instanceof TransportResponse)) {
			return false;
		}

		TransportResponse tr = (TransportResponse) o;

		return (super.equals(tr) && id == tr.id);
	}

	public int hashCode() {
		return super.hashCode() + 17 * id;
	}
}
