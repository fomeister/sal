/**
 * 
 */
package jcu.sal.Components.Sensors;

/**
 * @author gilles
 *
 */
class SensorState {
	
	private int use_state = 0;
	private int config_state = 0;
	private int error_state = 0;
	
	public static final int STATE_UNCHANGED = -1;
	
	public static final int USESTATE_DISABLED = 0;
	public static final int USESTATE_INUSE = 1;
	public static final int USESTATE_ENABLED_IDLE = 2;

	
	public static final int CONFIGSTATE_NOTCONFIGURED = 0;
	public static final int CONFIGSTATE_PART_CONFIGURED = 1;
	public static final int CONFIGSTATE_CONFIGURED = 2;

	public static final int ERRORSTATE_PRESENT = 0;
	public static final int ERRORSTATE_MISSING = 1;
	public static final int ERRORSTATE_ERROR = 2;
	
	public SensorState(int use_state, int config_state, int error_state) { 
		this.use_state = use_state;
		this.config_state = config_state;
		this.error_state = error_state;
	}
	
	public SensorState() { 
		this.use_state = USESTATE_ENABLED_IDLE; 
		this.config_state = CONFIGSTATE_NOTCONFIGURED;
		this.error_state = ERRORSTATE_MISSING;
	}
	
	public int getUseState() {return use_state;}
	public int getConfigState() {return config_state;}
	public int getErrorState() {return error_state;}
	
	private void setUseState(int state ) { if(state!=STATE_UNCHANGED)  use_state = state;} 
	private void setConfigState(int state ) { if(state!=STATE_UNCHANGED) config_state = state;} 
	private void setErrorState(int state ) { if(state!=STATE_UNCHANGED) error_state = state;} 
	
	public void setState(int us, int cs, int es) { synchronized(this) {setUseState(us); setConfigState(cs); setErrorState(es);} }
	
	public boolean isAvailable() { synchronized (this) {return use_state==USESTATE_ENABLED_IDLE && config_state==CONFIGSTATE_CONFIGURED && error_state==ERRORSTATE_PRESENT;}}
	
	public void setStateAvailable() { synchronized (this) {use_state=USESTATE_ENABLED_IDLE; config_state=CONFIGSTATE_CONFIGURED; error_state=ERRORSTATE_PRESENT;}}
}
