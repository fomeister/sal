/**
 * @author gilles
 */
package jcu.sal.Components.LogicalPorts;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.LogicalPortID;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class LogicalPort extends AbstractComponent {
	
	private EndPoint ep = null;
	//private Protocol p = null;

	private Logger logger = Logger.getLogger(LogicalPort.class);
	public static final String LOGICALPORT_TYPE = "LogicalPort";
	/**
	 * 
	 */
	public LogicalPort() {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor LogicalPort");
		id = new LogicalPortID("");
		type = new String("");
	}
	
	/**
	 * 
	 */
	public LogicalPort(EndPoint e) {
		this();
		this.ep = e;
		/* do not use any other var here as they are not initialised yet
		they will be after the creator calls setID and setType */
	}
		
	
	@Override
	protected void parseConfig() throws RuntimeException {
		// Not much to do here...
	}
	@Override
	public void remove() {
		this.logger.debug("Removing logical port" + toString());
		ep.stop();
		ep.remove();
	}
	@Override
	public void start() {
		this.logger.debug("Starting logical port" + toString());
	}
	@Override
	public void stop() {
		this.logger.debug("Stopping logical port" + toString());
	}

	@Override
	public String toString() {
		return "LogicalPort " + id.getName() + " ("+type+") with " +ep.toString();
	}	
}
