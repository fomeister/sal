package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are raised whenever an unexpected run-time exception
 * related to XML methods is caught.
 * @author gilles
 *
 */
public class DocumentHandlingException extends SALRunTimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4634483728899365147L;

	/**
	 * This constructor builds a simple DocumentHandlingException
	 */
	public DocumentHandlingException() {}

	/**
	 * This constructor builds a simple DocumentHandlingException with a message
	 * @param message the message
	 */
	public DocumentHandlingException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple DocumentHandlingException with a cause
	 * @param cause the cause
	 */
	public DocumentHandlingException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple DocumentHandlingException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public DocumentHandlingException(String message, Throwable cause) {
		super(message, cause);
	}
}
