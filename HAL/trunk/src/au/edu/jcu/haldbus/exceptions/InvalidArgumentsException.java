package au.edu.jcu.haldbus.exceptions;

public class InvalidArgumentsException extends RuntimeException {

	private static final long serialVersionUID = 1504023821456202474L;

	public InvalidArgumentsException() {
		super();
	}

	public InvalidArgumentsException(String message) {
		super(message);
	}

	public InvalidArgumentsException(Throwable cause) {
		super(cause);
	}

	public InvalidArgumentsException(String message, Throwable cause) {
		super(message, cause);
	}

}

