package au.edu.jcu.haldbus.exceptions;

public class AddRemoveElemException extends DBusException {

	private static final long serialVersionUID = 1L;

	public AddRemoveElemException() {
		super();
	}

	public AddRemoveElemException(String message) {
		super(message);
	}

	public AddRemoveElemException(Throwable cause) {
		super(cause);
	}

	public AddRemoveElemException(String message, Throwable cause) {
		super(message, cause);
	}

}
