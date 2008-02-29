/**
 * @author gilles
 */
package jcu.sal.Components.EndPoints;

import java.util.Hashtable;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.Components.componentRemovalListener;
import jcu.sal.Components.Identifiers.EndPointID;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public abstract class EndPoint extends AbstractComponent<EndPointID> {

	public static final String ENDPOINTTYPE_TAG = "type";
	public static final String ENDPOINTNAME_TAG = "name";
	public static final String ENPOINT_TAG="EndPoint";
	
	private Logger logger = Logger.getLogger(EndPoint.class);
	
	protected boolean enabled;
	protected boolean configured;
	/**
	 * 
	 */
	public EndPoint(EndPointID i, String t, Hashtable<String,String> c) {
		super();
		Slog.setupLogger(this.logger);
		enabled=false;
		configured=false;
		id = i;
		type = t;
		config = c;
	}
	
	/**
	 * returns a textual representation of a End Point's instance
	 * @return the textual representation of the Logical Port's instance
	 */
	public String toString() {
		return "EndPoint "+id.getName()+"("+type+")";
	}
	

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove(componentRemovalListener c) {
		synchronized (this) {
			if(enabled)
				stop();
			configured=false;
			this.logger.debug(type+" Endpoint removed");	
		}
		c.componentRemovable(id);
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#start()
	 */
	@Override
	public void start(){
		synchronized (this) {
			if(configured && !enabled) {
				this.logger.debug("Starting "+type+" Endpoint.");
				enabled=true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		synchronized (this) {
			if(enabled) {
				this.logger.debug("Stopping "+type+" Endpoint.");
				enabled=false;
			}
		}
	}


	@Override
	public boolean isStarted() {
		return enabled;
	}
	
}
