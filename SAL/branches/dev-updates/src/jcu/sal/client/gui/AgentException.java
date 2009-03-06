package jcu.sal.client.gui;

public class AgentException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1149332650455332117L;

	/**
	 * This constructor builds a simple AgentException
	 */
	public AgentException() {}

	/**
	 * This constructor builds a simple AgentException with a message
	 * @param message the message
	 */
	public AgentException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple AgentException with a cause
	 * @param cause the cause
	 */
	public AgentException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple AgentException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public AgentException(String message, Throwable cause) {
		super(message, cause);
	}
}
