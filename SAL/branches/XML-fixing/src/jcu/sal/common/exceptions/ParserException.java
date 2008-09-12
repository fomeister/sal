/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are thrown when handling a malformed XML document
 * @author gilles
 *
 */
public class ParserException extends SALAgentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8957985447602260403L;

	/**
	 * This constructor builds a simple ParserException
	 */
	public ParserException() {}

	/**
	 * This constructor builds a simple ParserException with a message
	 * @param message the message
	 */
	public ParserException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple ParserException with a cause
	 * @param cause the cause
	 */
	public ParserException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple ParserException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}

}
