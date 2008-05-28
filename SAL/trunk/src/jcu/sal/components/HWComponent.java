/**
 * 
 */
package jcu.sal.components;

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
	 * This method returns the type of this component
	 */
	public String getType();

	/**
	 * Changes this component's configuration and applies these changes 
	 * @param config the component's configuration
	 * 
	 */
	public Identifier getID();
	
		
	/**
	 * Retrieves this component'entire configuration
	 * @param config the component's configuration
	 *
	 */
	public Hashtable<String, String> getConfig();
	
	/**
	 * Retrieves a single configuration directive from theis component
	 * @param key the directive's name
	 * @throws BadAttributeValueExpException
	 * 
	 */
	public String getConfig(String directive) throws BadAttributeValueExpException;
	
	/**
	 * Starts this component: puts the component in a state where it can be used
	 * without requiring further configuration
	 */
	public void start() throws ConfigurationException;

	/**
	 * Stops this component
	 *
	 */
	public void stop() ;
	
	/**
	 * Destroy this component 
	 *
	 */
	public void remove(componentRemovalListener c);

}
