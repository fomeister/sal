/**
 * 
 */
package jcu.sal.Components.Sensors;

import org.apache.log4j.Logger;

import jcu.sal.Components.componentRemovalListener;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.utils.Slog;

/**
 * @author gilles
 *
 */
class SensorState {
	
	private Logger logger = Logger.getLogger(SensorState.class);
	private componentRemovalListener l;
	private Identifier i;

	public static final int UNUSED=0;
	public static final int IDLE=1;
	public static final int DISABLED=2;
	public static final int INUSE=3;
	public static final int DISCONNECTED=4;
	public static final int STOPPED=5;
	public static final int REMOVED=6;
	
	private int state;

	public SensorState() { 
		this.state=UNUSED;
		Slog.setupLogger(logger);
	}
	
	public boolean isStarted() {
		return (state!=UNUSED);
	}
	
	public boolean stop(componentRemovalListener c, Identifier i){
		synchronized (this) {
			if(state==INUSE) { state=STOPPED; l =c; this.i=i;} 
			else { state=REMOVED; c.componentRemovable(i);}
			return true;
		}
	}
	
	public boolean runCommand(){
		synchronized (this) {
			if(state==IDLE) { state=INUSE; return true; }
			else { logger.error("trying to run a command on a non IDLE sensor"); dumpState(); return false; }	
		}
	}
	
	public boolean doneCommand(){
		synchronized (this) {
			if(state==INUSE) { state=IDLE; return true; }
			else if(state==DISABLED) { return true; }
			else if(state==STOPPED) { state=REMOVED; l.componentRemovable(i); return true;}
			else { logger.error("trying to finish running a command on a non INUSE/DISABLED sensor"); dumpState(); return false; }	
		}
	}

	public boolean disable(){
		synchronized (this) {
			if(state==IDLE || state==INUSE || state==DISCONNECTED || state==DISABLED) { state=DISABLED; return true; }
			else { logger.error("trying to disable a non IDLE/INUSE/DISCONNECTED sensor"); dumpState(); return false; }
		}
	}
	
	public boolean enable(){
		synchronized (this) {
			if(state==DISABLED || state==UNUSED || state==IDLE) { state=IDLE; return true; }
			else { logger.error("trying to enable a non DISABLED sensor"); dumpState(); return false; }
		}
	}
	
	public boolean disconnect(){
		synchronized (this) {
			if(state==IDLE || state==DISABLED || state==INUSE || state==DISCONNECTED) { state=DISCONNECTED; return true; }
			else { logger.error("trying to disconnect a non-IDLE,DISABLED or INUSE sensor"); dumpState(); return false; }
		}
	}

	public boolean reconnect(){
		synchronized (this) {
			if(state==DISCONNECTED) { state=IDLE; return true; }
			else { logger.error("trying to resconnect a non-DISCONNECTED sensor"); dumpState(); return false; }
		}
	}
	
	public void dumpState() {
		synchronized(this) {
			logger.debug("Current sensor state: " );
			switch(state) {
			case UNUSED:
				logger.debug("State: UNUSED" );
				break;
			case IDLE:
				logger.debug("State: IDLE" );
				break;
			case DISABLED:
				logger.debug("State: DISABLED" );
				break;
			case INUSE:
				logger.debug("State: INUSE" );
				break;
			case DISCONNECTED:
				logger.debug("State: DISCONNECTED" );
				break;
			case STOPPED:
				logger.debug("State: STOPPED" );
				break;
			case REMOVED:
				logger.debug("State: REMOVED" );
				break;
			}
		}
	}
}
