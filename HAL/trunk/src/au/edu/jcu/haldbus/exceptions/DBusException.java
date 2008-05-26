package au.edu.jcu.haldbus.exceptions;

public class DBusException extends HalDBusException {
	private static final long serialVersionUID = 1L;
	
	public DBusException() {
		super();
	}
	
	public DBusException(String message) {
		super(message);
	}

	public DBusException(Throwable cause) {
		super(cause);
	}

	public DBusException(String message, Throwable cause) {
		super(message, cause);
	}

}
