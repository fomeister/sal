package au.edu.jcu.haldbus.exceptions;

public class InvalidConstructorArgs extends DBusException {

	private static final long serialVersionUID = 1L;

	public InvalidConstructorArgs() {
		super();
	}

	public InvalidConstructorArgs(String message) {
		super(message);
	}

	public InvalidConstructorArgs(Throwable cause) {
		super(cause);
	}

	public InvalidConstructorArgs(String message, Throwable cause) {
		super(message, cause);
	}

}
