/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.Managers.SensorManager;
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
		SUPPORTED_ENDPOINT_TYPES.add("fs");
		//Add to the list of supported commands
		commands.put(new Integer(100), "getReading");
		//Add to the list of supported sensors
		supportedSensors.put("FreeMem",new OSdata("/proc/meminfo", "MemFree", 2, null, true));
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
	}

	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_parseConfig()
	 */
	protected void internal_parseConfig() throws ConfigurationException {
		try {
			if(getConfig("CPUTempFile")!=null) supportedSensors.put("CPUTemp",new OSdata(getConfig("CPUTempFile"), null, 1, null, false));
		} catch (BadAttributeValueExpException e) {}
		try {
			if(getConfig("NBTempFile")!=null) supportedSensors.put("NBTemp",new OSdata(getConfig("NBTempFile"), null, 1, null, false));
		} catch (BadAttributeValueExpException e) {}
		try {
			if(getConfig("SBTempFile")!=null)  supportedSensors.put("SBTemp",new OSdata(getConfig("SBTempFile"), null, 1, null, false));
		} catch (BadAttributeValueExpException e) {}
		try {
			if(getConfig("TBTempFile")!=null)  supportedSensors.put("TBTemp",new OSdata(getConfig("TBTempFile"), null, 1, null, false));
		} catch (BadAttributeValueExpException e) {}
		logger.debug("OSData protocol configured");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_stop()
	 */
	protected void internal_stop() {}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_start()
	 */
	protected void internal_start() {}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_remove()
	 */
	protected void internal_remove() {
		logger.debug("OSData internal remove");
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
			s.disable();
			throw e; 
		}
		return ret;
	}


	@Override
	public boolean isSensorSupported(Sensor sensor){
		return supportedSensors.containsKey(sensor.getNativeAddress());	
	}


	@Override
	public boolean probeSensor(Sensor s) {
		OSdata d = supportedSensors.get(s.getNativeAddress());
		try {
			if((new File(d.file)).canRead()) {
				s.enable();
				return true;
			}
		} catch (Exception e) {
			logger.error("couldnt probe sensor "+s.toString()+". Raised exception: "+e.getMessage());
		}
		logger.debug("removing sensor "+s.toString()+", couldnt find the matching file: "+d.file);
		s.remove(SensorManager.getSensorManager());
		return false;
	}

}
