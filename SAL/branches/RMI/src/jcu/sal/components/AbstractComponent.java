/**
 * 
 */
package jcu.sal.components;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * This class does something
 * @author gilles
 *
 */
public abstract class AbstractComponent<T extends Identifier> implements HWComponent {
	
	protected Map<String, String> config;
	private Logger logger = Logger.getLogger(AbstractComponent.class);
//	protected boolean started = false;
	protected AtomicBoolean removed;
	protected String type = null;
	protected T id = null;
	
	public AbstractComponent() {
		Slog.setupLogger(this.logger);
		config = new Hashtable<String,String>();
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#getConfig()
	 */
	@Override
	public Map<String, String> getConfig() { return config; }

	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#getConfig(java.lang.String)
	 */
	@Override
	public String getConfig(String directive) throws BadAttributeValueExpException {
		String s = config.get(directive);
		if (s==null) {
			//logger.error("Unable to get a config directive with this name "+ directive);
			throw new BadAttributeValueExpException("Unable to get a config directive with this name "+ directive);
		}			
		return s; 
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
