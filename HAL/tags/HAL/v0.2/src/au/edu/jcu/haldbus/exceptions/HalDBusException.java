package au.edu.jcu.haldbus.exceptions;

public class HalDBusException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public HalDBusException() {
		super();
	}

	public HalDBusException(String message) {
		super(message);
	}

	public HalDBusException(Throwable cause) {
		super(cause);
	}

	public HalDBusException(String message, Throwable cause) {
		super(message, cause);
	}

}
