/**
 * 
 */
package jcu.sal.components.sensors;

import javax.naming.ConfigurationException;

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
	
	private Logger logger = Logger.getLogger(SensorState.class);
	private componentRemovalListener l;
	private SensorID i;

	public static final int UNASSOCIATED=0;
	public static final int IDLE=1;
	public static final int DISABLED=2;
	public static final int INUSE=3;
	public static final int DISCONNECTED=4;
	public static final int STOPPED=5;
	public static final int REMOVED=6;
	private static final EventDispatcher ev = EventDispatcher.getInstance();
	public static final String PRODUCER_ID = "SensorState";

	
	private int state;
	/*
	 * disconnect_timestamp is the timestamp at which the disconnect method has been called.
	 */
	private long disconnect_timestamp;

	public SensorState(SensorID i) { 
		state=UNASSOCIATED;
		disconnect_timestamp=-1;
		this.i = i;
		Slog.setupLogger(logger);
	}
	
	public long getDisconnectTimestamp() {
		return disconnect_timestamp;
	}
	
	public boolean isStarted() {
		return (state!=UNASSOCIATED);
	}
	
	public boolean isDisconnectedDisabled() {
		return (state==DISCONNECTED || state==DISABLED);
	}
	
	public boolean remove(componentRemovalListener c){
		synchronized (this) {
			if(state==INUSE) { state=STOPPED; l =c;} 
			else { state=REMOVED; c.componentRemovable(i);}
			return true;
		}
	}
	
	public boolean runCommand(){
		synchronized (this) {
			if(state==IDLE) { state=INUSE; return true; }
			else return false;	
		}
	}
	
	public boolean doneCommand(){
		synchronized (this) {
			if(state==INUSE) { state=IDLE; return true; }
			else if(state==DISABLED) { logger.debug(i.toString()+" has been disabled while we were reading it "); return true; }
			else if(state==STOPPED) { state=REMOVED; l.componentRemovable(i); return true;}
			else { logger.error("trying to finish running a command on a non INUSE/DISABLED sensor"); dumpState(); return false; }	
		}
	}

	public boolean disable(){
		synchronized (this) {
			if(state==INUSE) {
				logger.error("###############################################################################################");
				logger.error("###############################################################################################");
				logger.error("trying to disable an INUSE sensor"); dumpState();
				logger.error("###############################################################################################");
				logger.error("###############################################################################################");
				return false; 
			} else if(state==IDLE || state==DISCONNECTED || state==DISABLED) { state=DISABLED; return true; }
			else { logger.error("trying to disable a non IDLE/INUSE/DISCONNECTED sensor"); dumpState(); return false; }
		}
	}
	
	public boolean enable(){
		synchronized (this) {
			if(state==DISABLED || state==UNASSOCIATED || state==IDLE) {
				if(state!=UNASSOCIATED) {
					try {
						ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_CONNECTED,i.getName(),PRODUCER_ID));
					} catch (ConfigurationException e) {logger.error("Cant queue event");}
				}
				state=IDLE; return true;
			}
			else { logger.error("trying to enable a non DISABLED sensor"); dumpState(); return false; }
		}
	}
	
	public boolean disconnect(){
		synchronized (this) {
			if(state==IDLE || state==INUSE || state==DISCONNECTED || state==UNASSOCIATED) {
				if(state!=DISCONNECTED) {
					try {
						ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_DISCONNECTED,i.getName(),PRODUCER_ID));
					} catch (ConfigurationException e) {logger.error("Cant queue event");}
				}
				state=DISCONNECTED;
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
			if(state==DISCONNECTED) { 
				disconnect_timestamp = -1;
				state=IDLE;
				try {
					ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_CONNECTED,i.getName(),PRODUCER_ID));
				} catch (ConfigurationException e) {logger.error("Cant queue event");}
				return true; 
			}
			else if(state==IDLE || state==DISABLED || state==INUSE) {return true; }//already connected 
			else { logger.error("trying to reconnect a non-DISCONNECTED sensor"); dumpState(); return false; }
		}
	}
	
	public String toString() {
		synchronized(this) {
			switch(state) {
			case UNASSOCIATED:
				return "UNASSOCIATED";
			case IDLE:
				return "IDLE";
			case DISABLED:
				return "DISABLED";
			case INUSE:
				return "INUSE";
			case DISCONNECTED:
				return "DISCONNECTED";
			case STOPPED:
				return "STOPPED";
			case REMOVED:
				return "REMOVED";
			}
		}
		return "State: unknown state...";
	}
	
	public void dumpState() {
			logger.debug("Current sensor state: "+toString() );
	}
}
