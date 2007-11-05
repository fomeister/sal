/**
 * @author gilles
 */
package jcu.sal.Components.LogicalPorts;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.EndPoints.EndPoint;
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
	}
	
	/**
	 * 
	 */
	public LogicalPort(EndPoint e) {
		this();
		this.ep = e;
		this.logger.debug("Attached EndPoint "+e.getID().getName()+" to the new LogicalPort "+getID().getName());
	}
		
	
	@Override
	protected void parseConfig() throws RuntimeException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}	
}
