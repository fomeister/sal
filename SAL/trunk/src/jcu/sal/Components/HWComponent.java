/**
 * 
 */
package jcu.sal.Components;

import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;


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
	public void updateConfig(Hashtable<String, String> config) throws ConfigurationException;
	
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
	 * Starts a component: puts the component in a state where it can be used
	 * without requiring further configuration
	 */
	public void start() throws ConfigurationException;

	/**
	 * Stops a component
	 *
	 */
	public void stop() ;
	
	/**
	 * Destroy a component 
	 *
	 */
	public void remove(componentRemovalListener c);

}
