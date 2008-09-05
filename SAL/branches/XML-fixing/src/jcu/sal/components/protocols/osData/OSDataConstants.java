package jcu.sal.components.protocols.osData;

public class OSDataConstants {
	/**
	 * The string used in PCML docs to represent this protocol 
	 */
	public static final String PROTOCOL_TYPE = "PlatformData";
	
	/**
	 * The string used in the protocol parameters section of SML docs
	 * which points to the file containing the required data
	 */
	public static String SMLDataFile = "DataFile";
	
	/**
	 * The string used in the protocol parameters section of PCML docs
	 * which points to the file containing the temp1 data
	 */
	public static String Temp1DataFile = "Temp1DataFile";
	
	/**
	 * The string used in the protocol parameters section of PCML docs
	 * which points to the file containing the temp1 data
	 */
	public static String Temp2DataFile = "Temp2DataFile";
	
	/**
	 * The string used in the protocol parameters section of PCML docs
	 * which points to the file containing the temp1 data
	 */
	public static String Temp3DataFile = "Temp3DataFile";
	
	private static String HwMonPath = "/sys/class/hwmon/hwmon0/device/";
	public static String DefaultTemp1File = HwMonPath + "temp1_input";
	public static String DefaultTemp2File = HwMonPath + "temp2_input";
	public static String DefaultTemp3File = HwMonPath + "temp3_input";
	
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
	public static String Temp1 = "CPUTemp";
	/**
	 * Name of NB Temperature sensor
	 */
	public static String Temp2 = "NBTemp";
	/**
	 * Name of SB Temperature sensor
	 */
	public static String Temp3 = "SBTemp";
}
