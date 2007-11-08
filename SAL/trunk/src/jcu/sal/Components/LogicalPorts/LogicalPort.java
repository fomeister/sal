/**
 * @author gilles
 */
package jcu.sal.Components.LogicalPorts;

import javax.naming.ConfigurationException;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.Identifiers.LogicalPortID;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class LogicalPort extends AbstractComponent<LogicalPortID> {
	
	private Protocol protocol;

	private Logger logger = Logger.getLogger(LogicalPort.class);
	public static final String LOGICALPORT_TYPE = "LogicalPort";
	/**
	 * 
	 */
	public LogicalPort(LogicalPortID i, String t, Protocol p) {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor LogicalPort");
		id = i;
		type =t;
		protocol = p;
	}
		
	
	@Override
	protected void parseConfig() throws RuntimeException {
		// Not much to do here...
	}
	@Override
	public void remove() {
		this.logger.debug("Removing logical port" + toString());
		protocol.stop();
		protocol.remove();
	}
	@Override
	public void start() throws ConfigurationException{
		this.logger.debug("Starting logical port" + toString());
	}
	@Override
	public void stop() {
		this.logger.debug("Stopping logical port" + toString());
		protocol.stop();
	}

	@Override
	public String toString() {
		return "LogicalPort " + id.getName() + " ("+type+") with " +protocol.toString();
	}	
}
