/**
 * 
 */
package jcu.sal.components;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * This class does something
 * @author gilles
 *
 */
public abstract class AbstractComponent<T extends Identifier> implements HWComponent {

	private static Logger logger = Logger.getLogger(AbstractComponent.class);
	static {Slog.setupLogger(logger);}
	
	protected Parameters params;
	protected AtomicBoolean removed;
	protected String type = null;
	protected T id = null;
	
	public AbstractComponent() {}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#getConfig()
	 */
	@Override
	public Parameters getConfig() { return params; }

	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#getConfig(java.lang.String)
	 */
	@Override
	public String getConfig(String directive) throws BadAttributeValueExpException {
		try {
			return params.getParameter(directive).getStringValue();
		} catch (ConfigurationException e) {
			throw new BadAttributeValueExpException("Unable to get a config directive with this name "+ directive);
		}
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#remove()
	 */
	@Override
	public abstract void remove(componentRemovalListener c);

	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#start()
	 */
	@Override
	public abstract void start() throws ConfigurationException;

	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#stop()
	 */
	@Override
	public abstract void stop();
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#getType()
	 */
	@Override
	public String getType() {
		return this.type;
	}
	
	/**
	 * Parses the component's configuration directives stored in config,
	 * and applies them
	 * @throws ConfigurationException Indicates a error parsing/setting configuration. 
	 * Most likely there is a problem with the component's configuration
	 */
	public abstract void parseConfig() throws ConfigurationException;
	
	/**
	 * returns whether a component is started
	 * @return whether a component is started
	 */
	public abstract boolean isStarted();
	
	/**
	 * returns a textual representation of a component
	 * @return the textual representation of the component
	 */
	public abstract String toString();

	/**
	 * Gets the Identifier of a component
	 * @return the Identifier
	 */
			
	public T getID() {
		return this.id;
	}
}
