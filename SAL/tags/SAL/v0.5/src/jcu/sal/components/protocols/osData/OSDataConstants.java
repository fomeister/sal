package jcu.sal.components.protocols.osData;

public class OSDataConstants {
	/**
	 * The string used in PCML docs to represent this protocol 
	 */
	public static final String PROTOCOL_TYPE = "PlatformData";
	
	/**
	 * The string used in the procol parameters section of PCML docs
	 * which points to the file containing the CPU temperature 
	 */
	public static String CPUTempFile = "CPUTempFile";
	
	/**
	 * The string used in the procol parameters section of PCML docs
	 * which points to the file containing the north bridge temperature 
	 */
	public static String NBTempFile = "NBTempFile";

	/**
	 * The string used in the procol parameters section of PCML docs
	 * which points to the file containing the south bridge temperature 
	 */
	public static String SBTempFile = "SBTempFile";
	
	/**
	 * Name of Free Memory sensor
	 */
	public static String FreeMem = "FreeMem";
	/**
	 * Name of CPU User Time sensor
	 */
	public static String UserTime = "UserTime";
	/**
	 * Name of CPU Nice Time sensor
	 */
	public static String NiceTime = "NiceTime";
	/**
	 * Name of CPU System Time sensor
	 */
	public static String SystemTime = "SystemTime";
	/**
	 * Name of CPU Idle Time sensor
	 */
	public static String IdleTime = "IdleTime";
	/**
	 * Name of 1-min Load Average sensor
	 */
	public static String LoadAvg1 = "LoadAvg1";
	/**
	 * Name of 5-min Load Average sensor
	 */
	public static String LoadAvg5 = "LoadAvg5";
	/**
	 * Name of 15-min Load Average sensor
	 */
	public static String LoadAvg15 = "LoadAvg15";
	/**
	 * Name of CPU Temperature sensor
	 */
	public static String CPUTemp = "CPUTemp";
	/**
	 * Name of NB Temperature sensor
	 */
	public static String NBTemp = "NBTemp";
	/**
	 * Name of SB Temperature sensor
	 */
	public static String SBTemp = "SBTemp";
}
