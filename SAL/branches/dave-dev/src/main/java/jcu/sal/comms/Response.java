
package jcu.sal.comms;

import jcu.sal.xml.Message;

import javax.xml.bind.JAXBException;

public class Response extends Command {

	public Response() {
		super();
	}

	public Response(Response response) {
		super(response);
	}

	public Response(String xmlString) throws JAXBException {
		super(xmlString);
	}

	public Boolean isFinal() {
		return message.isFinal();
	}

	public void setFinal(Boolean value) {
		message.setFinal(value);
	}

}
