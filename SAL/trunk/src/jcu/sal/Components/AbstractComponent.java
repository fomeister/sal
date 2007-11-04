/**
 * 
 */
package jcu.sal.Components;

import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public abstract class AbstractComponent implements HWComponent {
	
	protected Hashtable<String, String> config;
	private Logger logger = Logger.getLogger(AbstractComponent.class);
	protected boolean started = false;
	protected boolean configured = false;
	
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
	 * @see jcu.sal.Components.HWComponent#setConfig(java.util.Hashtable)
	 */
	public void setConfig(Hashtable<String, String> config) throws RuntimeException
	{
		this.config = config;
		parseConfig();
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#remove()
	 */
	public abstract void remove();

	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#start()
	 */
	public abstract void start() ;

	/* (non-Javadoc)
	 * @see jcu.sal.Components.HWComponent#stop()
	 */
	public abstract void stop() ;
	
	/**
	 * Parses the component's configuration directives stored in config,
	 * and applies them
	 * @throws RuntimeException Indicates a error parsing/setting configuration. 
	 * Most likely there is a problem with the component's configuration
	 */
	protected abstract void parseConfig() throws RuntimeException;

}
