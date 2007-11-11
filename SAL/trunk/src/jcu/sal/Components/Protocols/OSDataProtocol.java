/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

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
	public OSDataProtocol(ProtocolID i, String t, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super(i,t,c,d);
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
	public void probeSensors() throws ConfigurationException{
		Sensor s = null;
		boolean error = false;
		logger.debug("probing sensors");
		Collection<Sensor> c = sensors.values();
		Iterator<Sensor> iter = c.iterator();
		while(iter.hasNext()) {
			s = iter.next();
			if(supportedSensors.containsKey(s.getNativeAddress())) {
				logger.debug("found sensor " + s.toString());
				try {
					ProcessHelper.captureOutputs(supportedSensors.get(s.getNativeAddress()));
				} catch (IOException e) {
					logger.error("Cannot read from the sensor");
					error = true;
				}
			}
		}		
		if(error) throw new ConfigurationException();
	}
	
	public String getReading(Hashtable<String,String> c, Sensor s) {
		logger.debug("getReading method called on sensor " +s.toString());
		return null;
	}
	
	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, ConfigurationException {
		Document d = XMLhelper.createDocument("<EndPoint name='osData' type='fs' />");
		Hashtable<String, String> c = new Hashtable<String, String>();
		SensorID sid = new SensorID("fictifSensor");
		Sensor s = new Sensor(sid, "Sensor",c);
		OSDataProtocol o = new OSDataProtocol(new ProtocolID("OSData"), "OSData", c, d);
		o.addSensor(s);
		try {
			o.execute(new Command("100", "param", "value"), sid);
		} catch (BadAttributeValueExpException e) {
			System.out.println("Incorrect value");
		} catch (ParseException e) {
			System.out.println("Malformed command");
		}
		o.dumpConfig();
		o.remove();
	}

}
