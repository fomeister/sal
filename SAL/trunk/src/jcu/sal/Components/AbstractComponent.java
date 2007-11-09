/**
 * 
 */
package jcu.sal.Components;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public abstract class AbstractComponent<T> implements HWComponent {
	
	protected Hashtable<String, String> config;
	private Logger logger = Logger.getLogger(AbstractComponent.class);
	protected boolean started = false;
	protected boolean configured = false;
	protected String type = null;
	protected T id = null;
	
	public AbstractComponent() {
		Slog.setupLogger(this.logger);
		config = new Hashtable<String,String>();
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#getConfig()
	 */
	public Hashtable<String, String> getConfig() { return config; }

	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#getConfig(java.lang.String)
	 */
	public String getConfig(String directive) throws BadAttributeValueExpException {
		String s = config.get(directive);
		if (s==null) {
			this.logger.error("Unable to get a config directive with this name "+ directive);
			throw new BadAttributeValueExpException("Unable to get a config directive with this name "+ directive);
		}			
		return s; 
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#updateConfig(java.util.Hashtable)
	 */
	public void updateConfig(Hashtable<String, String> config) throws ConfigurationException
	{
		if (!started) {
			this.config = config;
			parseConfig();
		} else {
			// TODO
			logger.debug("NOT IMPLEMENTED: attempting to change the configuration while running");
		}
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#remove()
	 */
	public abstract void remove();

	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#start()
	 */
	public abstract void start() throws ConfigurationException;

	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#stop()
	 */
	public abstract void stop();
	
	/**
	 * Parses the component's configuration directives stored in config,
	 * and applies them
	 * @throws ConfigurationException Indicates a error parsing/setting configuration. 
	 * Most likely there is a problem with the component's configuration
	 */
	protected abstract void parseConfig() throws ConfigurationException;
	
	/**
	 * returns a textual representation of a Logical Port's instance
	 * @return the textual representation of the Logical Port's instance
	 */
	public abstract String toString();
	
	/**
	 * Dumps the contents of the configuration table
	 */
	public void dumpConfig() {
		this.logger.debug("current configuration for " + id.toString() +":" );
		Enumeration<String> keys = config.keys();
		Collection<String> cvalues = config.values();
		Iterator<String> iter = cvalues.iterator();
		while ( keys.hasMoreElements() &&  iter.hasNext())
		   this.logger.debug("key: " + keys.nextElement().toString() + " - "+iter.next().toString());
	}
	
	/**
	 * Sets the type of a component
	 * @param t the type
	 */
			
	protected void setType(String t) {
		this.type = t;
	}

	/**
	 * Sets the Identifier of a component
	 * @param i the Identifier
	 */
			
	protected void setID(T i) {
		this.id = i;
	}
	
	/**
	 * Gets the type of a component
	 * @return the type
	 */
			
	public String getType() {
		return this.type;
	}

	/**
	 * Gets the Identifier of a component
	 * @return the Identifier
	 */
			
	public T getID() {
		return this.id;
	}

	public boolean isConfigured() {
		return configured;
	}

	public void setConfigured(boolean configured) {
		this.configured = configured;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
}
