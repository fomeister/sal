package au.edu.jcu.haldbus.exceptions;

public class MatchNotFoundException extends DBusException {

	private static final long serialVersionUID = 1L;

	public MatchNotFoundException() {
		super();
	}

	public MatchNotFoundException(String message) {
		super(message);
	}

	public MatchNotFoundException(Throwable cause) {
		super(cause);
	}

	public MatchNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
