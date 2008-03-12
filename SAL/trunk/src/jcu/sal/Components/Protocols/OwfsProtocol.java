/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.EndPoints.DeviceListener;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Protocols.CMLStore.OwfsCML;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.PlatformHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @author gilles
 *
 */
public class OwfsProtocol extends Protocol implements DeviceListener {

	private static Logger logger = Logger.getLogger(OwfsProtocol.class);
	private int adapterNb=0;
	private int maxAdaptersSeen=0;

	public final static String OWFSPROTOCOL_TYPE = "owfs";
	public final static String OWFSLOCATIONATTRIBUTE_TAG = "Location";
	public final static String OWFSMOUNTPOINTATTRIBUTE_TAG = "MountPoint";
	public final static int OWFSSTART_MAX_ATTEMPTS = 2;
	public final static String DS2490_USBID= "04fa:2490";
	
	static { 
		Slog.setupLogger(logger);
		SUPPORTED_ENDPOINT_TYPES.add("usb");
		//SUPPORTED_ENDPOINT_TYPES.add("serial");
		
		//getTemperature		10.X
		//GetHumidity			26.X
		commands.put(new Integer(100), "getReading");
		
		//10.X, 26.X
		commands.put(new Integer(101), "getTemperature");
		
		//26.X
		commands.put(new Integer(112), "getHumidity");
		commands.put(new Integer(113), "getHumidityHIH4000");
		commands.put(new Integer(114), "getHumidityHTM1735");
		commands.put(new Integer(115), "getVAD");
		commands.put(new Integer(116), "getVDD");
		commands.put(new Integer(117), "getVIS");
	}
	
	
	/**
	 * Construct the OwfsProtocol object. (parseConfig is called in super())
	 * @throws ConfigurationException if there is a problem with the component's config
	 */
	public OwfsProtocol(ProtocolID i, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super(i,OWFSPROTOCOL_TYPE ,c,d);
		autodetect = true;
		AUTODETECT_INTERVAL = 100;
		cmls = new OwfsCML();
		/* if supported, registers with the USB endpoint to detect newly connected DS9490 adapters*/
		try { ep.registerDeviceListener(this, new String [] {DS2490_USBID}); }
		catch (UnsupportedOperationException e) {
			logger.debug("Autodetect not supported by the EndPoint");
		}
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
		stopOWFS();

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_start()
	 */
	protected void internal_start() {
		adapterNb=0;
		maxAdaptersSeen=0;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_stop()
	 */
	protected void internal_remove() {
		logger.debug("OWFS internal removed");
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Components.Protocols.Protocol#internal_isSensorSupported(jcu.sal.Components.Sensors.Sensor)
	 */
	@Override
	protected boolean internal_isSensorSupported(Sensor sensor) {
		//TODO check the sensor family and make sure it is supported
		//When supporting a new family, modify getReading() to support it
		if(getFamily(sensor).equals("10.") || getFamily(sensor).equals("26."))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Components.Protocols.Protocol#internal_probeSensor(jcu.sal.Components.Sensors.Sensor)
	 */
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
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Components.Protocols.Protocol#internal_getCMLStoreKey(jcu.sal.Components.Sensors.Sensor)
	 */
	@Override
	protected String internal_getCMLStoreKey(Sensor s){
		return getFamily(s);  
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Components.EndPoints.DeviceListener#deviceChange(int)
	 */
	public void adapterChange(int n) {
		if(n>adapterNb) {
			logger.debug("new OWFS adapters have been plugged in, adapterNB:"+adapterNb+" maxSeen:"+maxAdaptersSeen+" currently plugged:"+n);
			if(n>maxAdaptersSeen){
				logger.debug("Restarting OWFS to detect new adapters");
				try {
					Enumeration<Sensor> es = sensors.elements();
					while(es.hasMoreElements()) {
						Sensor s = es.nextElement();
						synchronized(s) {
							s.disable();
						}
					}
					
					stopAutodetectThread();
					stopOWFS();
					try { Thread.sleep(1000); } catch (InterruptedException e) {}
					startOWFS();
					startAutodetectThread();
					maxAdaptersSeen=n;
				} catch (ConfigurationException e) {
					logger.error("Unable to run owfs: "+e.getClass()+" - "+e.getMessage());
					if(e.getCause()!=null) logger.error("caused by: "+e.getCause().getClass()+" - "+e.getCause().getMessage());
				} 
			} else {
				logger.debug("no need to restart OWFS to detect new adapter, max>adapternb");
			}
			adapterNb=n;
		} else if (n<adapterNb) {
			logger.debug("a new OWFS adapter has been unplugged, adapterNB:"+adapterNb+" maxSeen:"+maxAdaptersSeen+" currently plugged:"+n);
			adapterNb=n;
		} else {
			logger.error("Weird condition, recevied a device change event, but the reported device count("+n+") is the same as ours("+adapterNb+")");
		}
	}
	
	
	private void stopOWFS() {PlatformHelper.killProcesses("owfs");}
	
	
	private void startOWFS() throws ConfigurationException{
		logger.debug("OWFS internal start");
		StringBuffer err = new StringBuffer();
		String s;
		int attempt=0;
		boolean started=false;
		
		try {
			while(++attempt<=OWFSSTART_MAX_ATTEMPTS && !started) {
				BufferedReader r[] = PlatformHelper.captureOutputs(config.get(OwfsProtocol.OWFSLOCATIONATTRIBUTE_TAG)+" -uall --timeout_directory 1 --timeout_presence 1 "+config.get(OwfsProtocol.OWFSMOUNTPOINTATTRIBUTE_TAG), false);
				try {Thread.sleep(1000);} catch (InterruptedException e) {} 
				//check stdout & stderr
				while ((s=r[0].readLine())!=null)
					err.append("out: "+s);
				while ((s=r[1].readLine())!=null)
					err.append("err: "+s);
				
				//Check that it actually started ...
				if(PlatformHelper.getPid("owfs").isEmpty() || err.length() > 0){
					
					logger.error("Starting OWFS command failed with:");
					System.out.println(err);
					err.delete(0, err.length());
					logger.error("Killing any instances of owfs");
					PlatformHelper.killProcesses("owfs");
					started=false;
					continue;
				}
				started=true;
			}
			
			if(!started) {
				logger.error("Coudlnt start the OWFS process");
				throw new ConfigurationException();
			}
		} catch (IOException e) {
			logger.error("Coudlnt run the OWFS process");
			throw new ConfigurationException();
		}
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
		    if(info!=null) {
			    for (int i = 0; i < info.length; i++) {
			      if (info[i].indexOf(".") != 2) { // name doesn't match
			        continue;
			      }
			      try { Integer.parseInt(info[i].substring(0,2)); } catch (NumberFormatException e) { continue; } 
	
			      if(!info[i].substring(0, 2).equals("81"))
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

	// TODO create an exception class for this instead of Exception
	public String getReading(Hashtable<String,String> c, Sensor s) throws IOException{
		if(getFamily(s).equals("10.")) {
			//temperature sensor, read from temperature file
			return getTemperature(c, s);
		} else if(getFamily(s).equals("26.")) {
			//humidity sensor, read from humidityfile
			return getHumidity(c, s);
		}
		logger.error("1-wire sensor family not yet supported");
		throw new IOException("1-wire Family not supported yet");
	}
	
	public String getTemperature(Hashtable<String,String> c, Sensor s) throws IOException{
		if(getFamily(s).equals("10.") || getFamily(s).equals("26.")) {
			//temperature sensor, read from temperature file
			return getRawReading(s.getNativeAddress()+ "/" + "temperature");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new IOException("sensor doesnt support this command");
	}
	
	public String getHumidity(Hashtable<String,String> c, Sensor s) throws IOException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "humidity");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new IOException("sensor doesnt support this command");
	}
	
	public String getHumidityHIH4000(Hashtable<String,String> c, Sensor s) throws IOException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "HIH4000/humidity");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new IOException("sensor doesnt support this command");
	}
	
	public String getHumidityHTM1735(Hashtable<String,String> c, Sensor s) throws IOException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "HTM1735/humidity");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new IOException("sensor doesnt support this command");
	}
	
	public String getVAD(Hashtable<String,String> c, Sensor s) throws IOException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "VAD");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new IOException("sensor doesnt support this command");
	}

	public String getVDD(Hashtable<String,String> c, Sensor s) throws IOException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "VDD");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new IOException("sensor doesnt support this command");
	}
	
	public String getVIS(Hashtable<String,String> c, Sensor s) throws IOException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "vis");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new IOException("sensor doesnt support this command");
	}
	
	/**
	 * Gets a raw reading from an OWFS file
	 * @param f the file within OWFSMOUNTPOINTATTRIBUTE which should be read
	 * @return the reading
	 * @throws IOException if something goes wrong
	 */
	private String getRawReading(String f) throws IOException {
		try {
			return PlatformHelper.readFromFile(getConfig(OWFSMOUNTPOINTATTRIBUTE_TAG)+"/"+f);
		} catch (BadAttributeValueExpException e) {
			logger.error("Cant read from 1-wire sensor " +f);
			logger.error("Most likely a wrong OWFS mount point in the OWFS XML config");
			logger.error("Returned exception: "+e.getClass()+" - "+e.getMessage());
			throw new IOException("Cant read from 1-wire sensor " +f);
		} 
	}
}
