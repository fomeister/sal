/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.ProcessHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @author gilles
 *
 */
public class OSDataProtocol extends Protocol {
	
	static class OSdata {
		public String file;
		public String pattern;
		public int field;
		public String delim;
		boolean translate;
		public OSdata(String file, String pattern, int field, String delim, boolean translate) {
			this.file = file;
			this.pattern = pattern;
			this.field = field;
			this.delim = delim;
			this.translate = translate;
		}
		public String toString(){
			return "file: " + file + " - pattern: " + pattern + " - field: " + field + " - delim: '" + delim + "' - tr: "+ translate;
		}
	}

	public static final String OSDATAPROTOCOL_TYPE = "PlatformData";
	
	private static Logger logger = Logger.getLogger(OSDataProtocol.class);
	private static Hashtable<String,OSdata> supportedSensors = new Hashtable<String,OSdata>(); 
	static { 
		Slog.setupLogger(logger);
		//Add to the list of supported EndPoint IDs
		SUPPORTED_ENDPOINTS.add("fs");
		//Add to the list of supported commands
		commands.put(new Integer(100), "getReading");
		//Add to the list of supported sensors
		supportedSensors.put("FreeMem",new OSdata("/proc/meminfo", null, 6, null, true));
		supportedSensors.put("UserTime",new OSdata("/proc/stat", "cpu0",2, null, false));
		supportedSensors.put("NiceTime",new OSdata("/proc/stat", "cpu0",3, null, false));
		supportedSensors.put("SystemTime",new OSdata("/proc/stat", "cpu0", 4, null, false));
		supportedSensors.put("IdleTime",new OSdata("/proc/stat", "cpu0",5, null, false));
		supportedSensors.put("LoadAvg1",new OSdata("/proc/loadavg", null, 1, null, false));
		supportedSensors.put("LoadAvg5",new OSdata("/proc/loadavg", null, 2, null, false));
		supportedSensors.put("LoadAvg15",new OSdata("/proc/loadavg", null, 3, null, false));
	}
	
	
	
	
	/**
	 * Construct the OSDataProtocol object. The Endpoint is instanciated in super(), 
	 * and parseConfig is called in super()
	 * @throws ConfigurationException if there is a problem with the component's config
	 * 
	 */
	public OSDataProtocol(ProtocolID i, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super(i,OSDATAPROTOCOL_TYPE,c,d);
		if(c.get("CPUTempFile")!=null) supportedSensors.put("CPUTempFile",new OSdata(c.get("CPUTempFile"), null, 1, null, false));
		if(c.get("NBTempFile")!=null) supportedSensors.put("NBTempFile",new OSdata(c.get("NBTempFile"), null, 1, null, false));
		if(c.get("SBTempFile")!=null)  supportedSensors.put("SBTempFile",new OSdata(c.get("SBTempFile"), null, 1, null, false));
		
	}

	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_parseConfig()
	 */
	protected void internal_parseConfig() throws ConfigurationException {
		/*try {
			if(getConfig("CPUTempFile").length()!=0) supportedSensors.put("CPUTemp","cat " + getConfig("CPUTempFile"));
		} catch (BadAttributeValueExpException e) {}
		
		try {
		if(getConfig("NBTempFile").length()!=0) supportedSensors.put("NBTemp","cat " +getConfig("NBTempFile"));
		} catch (BadAttributeValueExpException e) {}
		
		try {
		if(getConfig("SBTempFile").length()!=0)  supportedSensors.put("SBTemp","cat " +getConfig("SBTempFile"));
		} catch (BadAttributeValueExpException e) {}
		*/
		configured = true;
		logger.debug("OSData protocol configured");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_remove()
	 */
	protected void internal_stop() {
		logger.debug("OSData internal stop");

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_start()
	 */
	public void internal_start() {
		logger.debug("OSData internal start");
		// TODO Check that the sensors table has some sensors
		// TODO call probeSensors

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_stop()
	 */
	protected void internal_remove() {
		logger.debug("OSData internal removed");
	}

	/**
	 * Check whether all the sensors are connected, and change their status accordingly
	 */
	public void probeSensor(Sensor sensor) throws ConfigurationException{
		if(supportedSensors.containsKey(sensor.getNativeAddress())) {
			logger.debug("Sensor " + sensor.toString()+" supported");
			try {
				logger.debug(getReading(null, sensor));
				logger.debug("Sensor probed successfully");
			} catch (IOException e) {
				logger.error("Cannot read from the sensor");
				throw new ConfigurationException();
			}
		} else {
			logger.debug("Sensor not supported");
			throw new ConfigurationException();
		}
	}
	
	
	// TODO create an exception class for this instead of Exception
	public String getReading(Hashtable<String,String> c, Sensor s) throws IOException{
		String ret = "";
		OSdata d = null;
		logger.debug("getReading method called on sensor " +s.toString());
		try {
			d = supportedSensors.get(s.getNativeAddress());
			ret = ProcessHelper.getFieldFromFile(d.file, d.pattern, d.field, d.delim, d.translate);
		} catch (IOException e) {
			logger.error("couldnt run the command to get readings for sensor "+ s.toString());
			throw e; 
		}
		return ret;
	}

	
	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws ConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, ConfigurationException, IOException {
		/*Document d = XMLhelper.createDocument("<EndPoint name='osData' type='fs' />");
		Hashtable<String, String> c = new Hashtable<String, String>();
		c.put("CPUTempFile", "/sys/class/i2c-adapter/i2c-9191/device/9191-0290/temp2_input");
		c.put("NBTempFile", "/sys/class/i2c-adapter/i2c-9191/device/9191-0290/temp1_input");
		c.put("SBTempFile", "/sys/class/i2c-adapter/i2c-9191/device/9191-0290/temp3_input");
		SensorID sid = new SensorID("fictifSensor");
		Sensor s = new Sensor(sid,c);
		OSDataProtocol o = new OSDataProtocol(new ProtocolID("OSData", "OSData"), c, d);
		o.addSensor(s);
		try {
			o.execute(new Command(new Integer(100), "param", "value"), sid);
		} catch (BadAttributeValueExpException e) {
			System.out.println("Incorrect value");
		} 
		o.dumpConfig();
		o.remove();*/
		System.out.println("freemem: " + ProcessHelper.getFieldFromFile("/proc/meminfo", "MemFree:", 2, null, true));
		System.out.println("user: " + ProcessHelper.getFieldFromCommand("vmstat", 3, 13, null, false));
		System.out.println("system: " + ProcessHelper.getFieldFromCommand("vmstat", 3, 14, null, false));
		System.out.println("idle: " + ProcessHelper.getFieldFromCommand("vmstat", 3, 15, null, false));
		System.out.println("IO: " + ProcessHelper.getFieldFromCommand("vmstat", 3, 16, null, false));
		System.out.println("loadavg1: " + ProcessHelper.getFieldFromFile("/proc/loadavg", null, 1, null, false));
		System.out.println("loadavg5: " + ProcessHelper.getFieldFromFile("/proc/loadavg", null, 2, null, false));
		System.out.println("loadavg15: " + ProcessHelper.getFieldFromFile("/proc/loadavg", null, 3, null, false));
	}

}
