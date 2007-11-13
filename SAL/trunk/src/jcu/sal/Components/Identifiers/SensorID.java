/**
 * 
 */
package jcu.sal.Components.Identifiers;

import jcu.sal.Components.Sensors.Sensor;

/**
 * this class represents Endpoint names
 * @author gilles
 *
 */
public class SensorID extends AbstractIdentifier{
	
	/** The Protocol ID associated with this sensor **/
	private ProtocolID pid;
	
	/**
	 * Creates a new SensorID with an initial name
	 * @param id the name
	 * @param type the type
	 * @param pid the protocolID
	 */
	public SensorID(String id, String type, ProtocolID pid) {
		super(id, type);
		this.pid = pid; 
	}
	
	/**
	 * Creates a new SensorID with an initial name and type. THe protocolID member is left empty 
	 * @param id the name
	 */
	public SensorID(String id, String type) {
		this(id, Sensor.SENSOR_TYPE, null);
		if(!type.equals(Sensor.SENSOR_TYPE))
			System.out.println("TRYING TO CREATE A SENSOR WITH TYPE: " + type);
	} 

	/**
	 * Creates a new SensorID with an initial name. THe protocolID member is left empty 
	 * @param id the name
	 */
	public SensorID(String id) {	this(id, Sensor.SENSOR_TYPE, null); } 
	
	/**
	 * Return the name of the ProtocolID associated with this Sensor 
	 * @return the protocolID
	 */
	public String getPIDName() {
		return getPid().getName();
	}
	
	/**
	 * Return the type of the ProtocolID associated with this Sensor 
	 * @return the protocolID
	 */
	public String getPIDtype() {
		return getPid().getName();
	}

	/**
	 * Return the ProtocolID associated with this Sensor 
	 * @return the protocolID
	 */
	public ProtocolID getPid() {
		if(pid == null) {
			System.out.println("************ TRYING TO ACCESS AN EMPTY SENSOR ID ***********");
			return new ProtocolID("","");
		}
		else
			return pid;
	}

	/**
	 * Sets the ProtocolID associated with this Sensor 
	 * @param pid the protocolID
	 */
	public void setPid(ProtocolID pid) {
		if(pid == null || (pid.getName().length()== 0 && pid.getType().length() == 0)) {
			System.out.println("************ TRYING TO SET AN EMPTY PROTOCOL ID ON AN SENSOR***********");
			pid = null;
		}
		else
			this.pid = pid;
	}
}
