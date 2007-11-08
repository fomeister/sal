/**
 * @author gilles
 */
package jcu.sal.Components.LogicalPorts;

import java.util.Hashtable;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Identifiers.LogicalPortID;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class LogicalPort extends AbstractComponent<LogicalPortID> {
	
	private EndPoint ep;
	private Hashtable<SensorID, Sensor> sensorTable;
	//private Protocol p = null;

	private Logger logger = Logger.getLogger(LogicalPort.class);
	public static final String LOGICALPORT_TYPE = "LogicalPort";
	/**
	 * 
	 */
	public LogicalPort(LogicalPortID i, String t, EndPoint e) {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor LogicalPort");
		id = i;
		type =t;
		ep = e;
		sensorTable = new Hashtable<SensorID, Sensor>();
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
		ep.stop();
	}

	@Override
	public String toString() {
		return "LogicalPort " + id.getName() + " ("+type+") with " +ep.toString();
	}	
}
