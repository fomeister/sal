/**
 * 
 */
package jcu.sal.components.sensors;

import jcu.sal.common.Constants;
import jcu.sal.components.componentRemovalListener;
import jcu.sal.events.EventDispatcher;
import jcu.sal.events.SensorStateEvent;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class SensorState {
	
	private static Logger logger = Logger.getLogger(SensorState.class);
	static {Slog.setupLogger(logger);}
	
	private componentRemovalListener l;
	private SensorID i;

	private static final EventDispatcher ev = EventDispatcher.getInstance();
	private int state;
	/*
	 * disconnect_timestamp is the timestamp at which the disconnect method has been called.
	 */
	private long disconnect_timestamp;

	public SensorState(SensorID i) { 
		state=SensorConstants.UNASSOCIATED;
		disconnect_timestamp=-1;
		this.i = i;
	}
	
	public long getDisconnectTimestamp() {
		return disconnect_timestamp;
	}
	
	public boolean isStarted() {
		return (state!=SensorConstants.UNASSOCIATED);
	}
	
	public boolean isDisconnectedDisabled() {
		return (state==SensorConstants.DISCONNECTED || state==SensorConstants.DISABLED);
	}
	
	public boolean remove(componentRemovalListener c){
		synchronized (this) {
			if(state==SensorConstants.INUSE) { state=SensorConstants.STOPPED; l =c;} 
			else { state=SensorConstants.REMOVED; c.componentRemovable(i);}
			return true;
		}
	}
	
	public boolean runCommand(){
		synchronized (this) {
			if(state==SensorConstants.IDLE) { state=SensorConstants.INUSE; return true; }
			else return false;	
		}
	}
	
	public boolean doneCommand(){
		synchronized (this) {
			if(state==SensorConstants.INUSE) { state=SensorConstants.IDLE; return true; }
			else if(state==SensorConstants.DISABLED) { logger.debug(i.toString()+" has been disabled while we were reading it "); return true; }
			else if(state==SensorConstants.STOPPED) { state=SensorConstants.REMOVED; l.componentRemovable(i); return true;}
			else { logger.error("trying to finish running a command on a non INUSE/DISABLED sensor"); dumpState(); return false; }	
		}
	}

	public boolean disable(){
		synchronized (this) {
			if(state==SensorConstants.INUSE) {
				logger.error("###############################################################################################");
				logger.error("###############################################################################################");
				logger.error("trying to disable an INUSE sensor"); dumpState();
				logger.error("###############################################################################################");
				logger.error("###############################################################################################");
				return false; 
			} else if(state==SensorConstants.IDLE || state==SensorConstants.DISCONNECTED || state==SensorConstants.DISABLED) { state=SensorConstants.DISABLED; return true; }
			else { logger.error("trying to disable a non IDLE/INUSE/DISCONNECTED sensor"); dumpState(); return false; }
		}
	}
	
	public boolean enable(){
		synchronized (this) {
			if(state==SensorConstants.DISABLED || state==SensorConstants.UNASSOCIATED || state==SensorConstants.IDLE) {
				if(state!=SensorConstants.UNASSOCIATED) {
					ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_CONNECTED,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
				}
				state=SensorConstants.IDLE; return true;
			}
			else { logger.error("trying to enable a non DISABLED sensor"); dumpState(); return false; }
		}
	}
	
	public boolean disconnect(){
		synchronized (this) {
			if(state==SensorConstants.IDLE || state==SensorConstants.INUSE || state==SensorConstants.DISCONNECTED || state==SensorConstants.UNASSOCIATED) {
				if(state!=SensorConstants.DISCONNECTED) {
					ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_DISCONNECTED,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
				}
				state=SensorConstants.DISCONNECTED;
				disconnect_timestamp = System.currentTimeMillis();
/*				logger.error("Sensor has had "+(timeout+1)+ " disconnections");
				if(++timeout>DISCONNECT_TIMEOUT) {
					logger.error("MAX disconnections, removing it.");
					ProtocolManager.getProcotolManager().removeSensor(i);
				}*/
				return true;
			} else { logger.error("trying to disconnect a non-IDLE,DISABLED or INUSE sensor"); dumpState(); return false; }
		}
	}

	public boolean reconnect(){
		synchronized (this) {
			if(state==SensorConstants.DISCONNECTED) { 
				disconnect_timestamp = -1;
				state=SensorConstants.IDLE;
				ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_CONNECTED,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
				return true; 
			}
			else if(state==SensorConstants.IDLE || state==SensorConstants.DISABLED || state==SensorConstants.INUSE) {return true; }//already connected 
			else { logger.error("trying to reconnect a non-DISCONNECTED sensor"); dumpState(); return false; }
		}
	}
	
	public String toString() {
		synchronized(this) {
			switch(state) {
			case SensorConstants.UNASSOCIATED:
				return "UNASSOCIATED";
			case SensorConstants.IDLE:
				return "IDLE";
			case SensorConstants.DISABLED:
				return "DISABLED";
			case SensorConstants.INUSE:
				return "INUSE";
			case SensorConstants.DISCONNECTED:
				return "DISCONNECTED";
			case SensorConstants.STOPPED:
				return "STOPPED";
			case SensorConstants.REMOVED:
				return "REMOVED";
			}
		}
		return "State: unknown state...";
	}
	
	public void dumpState() {
			logger.debug("Current sensor state: "+toString() );
	}
}
