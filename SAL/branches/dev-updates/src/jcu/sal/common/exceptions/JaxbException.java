package jcu.sal.common.exceptions;

import jcu.sal.common.exceptions.XmlException;

public class JaxbException extends XmlException {

	private static final long serialVersionUID = -7038632282816593282L;

	public JaxbException() {
		super();
	}

	public JaxbException(String message) {
		super(message);
	}

	public JaxbException(String message, Throwable cause) {
		super(message, cause);
	}

	public JaxbException(Throwable cause) {
		super(cause);
	}

}
