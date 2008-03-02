/**
 * 
 */
package jcu.sal.Components.Protocols.owfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Protocols.Protocol;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.PlatformHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @author gilles
 *
 */
public class OwfsProtocol extends Protocol {

	private static Logger logger = Logger.getLogger(OwfsProtocol.class);
	public final static String OWFSPROTOCOL_TYPE = "owfs";
	public final static String OWFSLOCATIONATTRIBUTE_TAG = "Location";
	public final static String OWFSMOUNTPOINTATTRIBUTE_TAG = "MountPoint";

	static { 
		Slog.setupLogger(logger);
		SUPPORTED_ENDPOINT_TYPES.add("usb");
		SUPPORTED_ENDPOINT_TYPES.add("serial");
		commands.put(new Integer(100), "getReading");
	}
	
	
	/**
	 * Construct the OwfsProtocol object. (parseConfig is called in super())
	 * @throws ConfigurationException if there is a problem with the component's config
	 */
	public OwfsProtocol(ProtocolID i, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super(i,OWFSPROTOCOL_TYPE ,c,d);
		autodetect = true;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		String mtpt, temp;

		try {
			mtpt = getConfig(OWFSMOUNTPOINTATTRIBUTE_TAG);
			if(mtpt.length()==0) throw new BadAttributeValueExpException("Empty mount point directive...");
			if(!PlatformHelper.isDirReadWrite(mtpt)) {
				//try creating it
				logger.debug("OWFS Mount point doesnt exist, try creating it");
				if (! new File(mtpt).mkdirs()) throw new BadAttributeValueExpException("Cant create mount point");
				logger.debug("done");
			}
			//try loading fuse
			PlatformHelper.loadModule("fuse");
			//try unloading ds2490 and wire
			PlatformHelper.unloadModules(new String [] {"ds2490", "wire"});
			
			//concurrent instances of owfs cant coexist 
			//Check whether instances of owfs are running
			if(!PlatformHelper.getPid("owfs").isEmpty()){
				logger.error("An instance of owfs seems to be running");
				logger.error("Try killing it");
				PlatformHelper.killProcesses("owfs");
				
				//sleep for a while
				try { Thread.sleep(1000);} catch (InterruptedException e) {}
				
				//and check again
				if(!PlatformHelper.getPid("owfs").isEmpty()){
					logger.error("CAnt kill it...");
					throw new BadAttributeValueExpException("OWFS already running");
				}
			}

			//Next, we check that OWFS is installed in the given directory
			//and try to get the OWFS version 
			logger.debug("Detecting OWFS version");
			BufferedReader[] b = PlatformHelper.captureOutputs(getConfig(OWFSLOCATIONATTRIBUTE_TAG) + " --version", true);
			while((temp = b[0].readLine()) != null) logger.debug(temp);
			while((temp = b[1].readLine()) != null) logger.debug(temp);
			
			logger.debug("OWFS protocol configured");
			
		} catch (IOException e) {
			logger.error("Could NOT run/read owfs");
			e.printStackTrace();
			throw new ConfigurationException("Could NOT run/read owfs");
		} catch (BadAttributeValueExpException e) {
			logger.error("incorrect OWFS configuration directives...");
			e.printStackTrace();
			throw new ConfigurationException("Could not setup OWFS protocol");
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_remove()
	 */
	protected void internal_stop() {
		logger.debug("OWFS internal stop");

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_start()
	 */
	protected void internal_start() throws ConfigurationException{
		logger.debug("OWFS internal start");
		StringBuffer err = new StringBuffer();
		String s;
		
		try {
			BufferedReader r[] = PlatformHelper.captureOutputs(config.get(OwfsProtocol.OWFSLOCATIONATTRIBUTE_TAG)+" -uall "+config.get(OwfsProtocol.OWFSMOUNTPOINTATTRIBUTE_TAG), false);
			Thread.sleep(1000);
			//check stderr
			while ((s=r[1].readLine())!=null)
				err.append(s);
			
			//Check that it actually started ...
			if(PlatformHelper.getPid("owfs").isEmpty() || err.length() > 0){
				
				logger.error("Starting OWFS command failed with:");
				System.out.println(err);
				logger.error("Killing any instances of owfs");
				PlatformHelper.killProcesses("owfs");
				throw new ConfigurationException();
			}
			
		} catch (IOException e) {
			logger.error("Coudlnt run the OWFS process");
			throw new ConfigurationException();
		} catch (InterruptedException e) {}
		
		// TODO call probeSensors

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_stop()
	 */
	protected void internal_remove() {
		logger.debug("OWFS internal removed");
		PlatformHelper.killProcesses("owfs");
	}
	

	// TODO create an exception class for this instead of Exception
	public String getReading(Hashtable<String,String> c, Sensor s) throws IOException{
		String ret = "";
		try {
			String f = getConfig(OWFSMOUNTPOINTATTRIBUTE_TAG)+"/"+s.getNativeAddress()+"/";
			if(getFamily(s).equals("10.")) {
				//temperature sensor, read from temperature file
				ret = readFromFile(f + "temperature");
			} else if(getFamily(s).equals("26.")) {
				//humidity sensor, read from humidityfile
				ret = readFromFile(f + "humidity");
			} else {
				logger.error("1-wire sensor family not yet supported");
				s.disconnect();
				throw new IOException("1-wire Family not supported yet");
			}
		} catch (Exception e) {
			logger.error("Cant read from 1-wire sensor " +s.toString());
			logger.error("Returned exceoption: "+e.getMessage());
			s.disconnect();
			throw new IOException("Cant read from 1-wire sensor " +s.toString());
		}
		return ret;
	}
	
	/**
	 * This method read one line from a file and returns the line
	 * @param f the file to be read
	 * @return the line read
	 * @throws IOException if an error occurs
	 */
	private String readFromFile (String f) throws IOException{
		return PlatformHelper.getFieldFromFile(f, 1, 1, null, false);
	}

	@Override
	protected boolean internal_isSensorSupported(Sensor sensor) {
		//TODO check the sensor family and make sure it is supported
		//When supporting a new family, modify getReading() to support it
		if(getFamily(sensor).equals("10.") || getFamily(sensor).equals("26."))
			return true;
		else
			return false;
	}

	@Override
	protected boolean internal_probeSensor(Sensor s) {
		// TODO complete this method
		String d = new String(config.get(OWFSMOUNTPOINTATTRIBUTE_TAG)+"/"+s.getNativeAddress());
		try {
			logger.debug("Probing sensor " + s.getNativeAddress());
			if(PlatformHelper.isDirReadable(d)) {
				s.enable();
				logger.debug("Sensor " + s.getNativeAddress()+ " present");
				return true;
			}
		} catch (Exception e) {
			logger.error("couldnt probe sensor "+s.toString());
			logger.error("Raised exception: "+e.getMessage());
		}
		s.disconnect();
		return false;
	}
	
	/**
	 * this method should be overriden by protocols which provide sensor autodetection
	 * and return a Vector of native address (strings) of currently connected/visible sensors 
	 */
	protected Vector<String> detectConnectedSensors() {
		Vector<String> v = new Vector<String>();
		try {
			File dir = new File(getConfig(OWFSMOUNTPOINTATTRIBUTE_TAG));
		    String[] info = dir.list();
		    for (int i = 0; i < info.length; i++) {
		      if (info[i].indexOf(".") != 2) { // name doesn't match
		        continue;
		      }
		      try { Integer.parseInt(info[i].substring(0,2)); } catch (NumberFormatException e) { continue; } 

		      if(!info[i].substring(0, 2).equals("81")) {
			      logger.debug("Autodetect thread: detected " + info[i]);
			      v.add(info[i]);
		      }
		    }
		} catch (BadAttributeValueExpException e) {
			logger.error("bad mount point value");
			e.printStackTrace();
		}
		return v;
	}
	
	private String getFamily(Sensor s){
		return s.getNativeAddress().substring(0, 3);
	}
}
