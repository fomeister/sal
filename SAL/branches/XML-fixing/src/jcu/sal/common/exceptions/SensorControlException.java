/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * This class of exceptions acts as the super class of all exceptions related to controlling and
 * piloting sensors. All exceptions raised when sending commands to sensors are subclasses of this
 * class.
 * @author gilles
 *
 */
public class SensorControlException extends SALAgentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1086645283955690804L;

	/**
	 * This constructor builds a simple SensorControlException
	 */
	public SensorControlException() {}

	/**
	 * This constructor builds a simple SensorControlException with a message
	 * @param message the message
	 */
	public SensorControlException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple SensorControlException with a cause
	 * @param cause the cause
	 */
	public SensorControlException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple SensorControlException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public SensorControlException(String message, Throwable cause) {
		super(message, cause);
	}
}
