/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.Components.Command;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Identifiers.SensorID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.ProcessHelper;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author gilles
 *
 */
public class OSDataProtocol extends Protocol {

	public static final String OSDATAPROTOCOL_TYPE = "PlatformData";
	
	private static Logger logger = Logger.getLogger(OSDataProtocol.class);
	private static Hashtable<String,String> supportedSensors = new Hashtable<String,String>(); 
	static { 
		Slog.setupLogger(logger);
		//Add to the list of supported EndPoint IDs
		SUPPORTED_ENDPOINTS.add("fs");
		//Add to the list of supported commands
		commands.put(new Integer(100), "getReading");
		//Add to the list of supported sensors
		supportedSensors.put("FreeMem","grep \"MemFree\" /proc/meminfo |  tr -s \" \" | cut -f2 -d\" \"");
		supportedSensors.put("UserTime","grep \"cpu0\" /proc/stat |cut -f2 -d\" \"");
		supportedSensors.put("NiceTime","grep \"cpu0\" /proc/stat |cut -f3 -d\" \"");
		supportedSensors.put("SystemTime","grep \"cpu0\" /proc/stat |cut -f4 -d\" \"");
		supportedSensors.put("IdleTime","grep \"cpu0\" /proc/stat |cut -f5 -d\" \"");
		supportedSensors.put("LoadAvg1","cut -f1 -d\" \" /proc/loadavg");
		supportedSensors.put("LoadAvg5","cut -f2 -d\" \" /proc/loadavg");
		supportedSensors.put("LoadAvg15","cut -f3 -d\" \" /proc/loadavg");
	}
	
	
	
	
	/**
	 * Construct the OSDataProtocol object. The Endpoint is instanciated in super(), 
	 * and parseConfig is called in super()
	 * @throws ConfigurationException if there is a problem with the component's config
	 * 
	 */
	public OSDataProtocol(ProtocolID i, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super(i,OSDATAPROTOCOL_TYPE,c,d);
		if(c.get("CPUTempFile")!=null) supportedSensors.put("CPUTempFile","cat " + c.get("CPUTempFile"));
		if(c.get("NBTempFile")!=null) supportedSensors.put("NBTempFile","cat " +c.get("NBTempFile"));
		if(c.get("SBTempFile")!=null)  supportedSensors.put("SBTempFile","cat " +c.get("SBTempFile"));

		
	}

	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_parseConfig()
	 */
	protected void internal_parseConfig() throws ConfigurationException {
		try {
			if(getConfig("CPUTempFile").length()!=0) supportedSensors.put("CPUTemp","cat " + getConfig("CPUTempFile"));
		} catch (BadAttributeValueExpException e) {}
		
		try {
		if(getConfig("NBTempFile").length()!=0) supportedSensors.put("NBTemp","cat " +getConfig("NBTempFile"));
		} catch (BadAttributeValueExpException e) {}
		
		try {
		if(getConfig("SBTempFile").length()!=0)  supportedSensors.put("SBTemp","cat " +getConfig("SBTempFile"));
		} catch (BadAttributeValueExpException e) {}
		
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
				ProcessHelper.captureOutputs(supportedSensors.get(sensor.getNativeAddress()));
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
	public String getReading(Hashtable<String,String> c, Sensor s) throws Exception{
		String ret = "", cmd = "";
		int exitval;
		logger.debug("getReading method called on sensor " +s.toString());
		/* Run the command */
		try {
			cmd = supportedSensors.get(s.getNativeAddress());
			logger.debug("running command " + cmd);
			BufferedReader[] b =  ProcessHelper.captureOutputs(cmd);
			exitval =Integer.parseInt(b[2].readLine());
			logger.error("the command returned an exit value of " + String.valueOf(exitval));
			if( exitval != 0)
				throw new Exception();

			else
				while((ret = b[0].readLine()) != null) logger.debug("raw reading: " + ret);
		} catch (IOException e) {
			logger.error("couldnt run the command to get readings for sensor "+ s.toString());
			throw new Exception(); 
		}
		return ret;
	}
	
	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, ConfigurationException {
		Document d = XMLhelper.createDocument("<EndPoint name='osData' type='fs' />");
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
		o.remove();
	}

}
