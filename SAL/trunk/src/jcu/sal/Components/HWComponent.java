/**
 * 
 */
package jcu.sal.Components;

import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;


/**
 * This interface specifies the basic methods required by Components 
 * (LogicalPorts, EndPoints, Sensors and Procotol)
 * @author gilles
 *
 */
public interface HWComponent {

	
	/**
	 * Changes the component's configuration and applies these changes 
	 * @param config the component's configuration
	 * 
	 */
	public void setConfig(Hashtable<String, String> config) throws RuntimeException;
	
	/**
	 * Retrieves the component'entire configuration
	 * @param config the component's configuration
	 *
	 */
	public Hashtable<String, String> getConfig();
	
	/**
	 * Retrieves a single configuration directive
	 * @param key the directive's name
	 * @throws BadAttributeValueExpException
	 * 
	 */
	public String getConfig(String directive) throws BadAttributeValueExpException;
	
	/**
	 * Starts a component or do nothing for those that cant be started
	 *
	 */
	public void start();

	/**
	 * Stops a component
	 *
	 */
	public void stop();
	
	/**
	 * Destroy a component 
	 *
	 */
	public void remove();

}
