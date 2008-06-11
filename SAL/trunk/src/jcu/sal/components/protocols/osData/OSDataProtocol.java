/**
 * 
 */
package jcu.sal.components.protocols.osData;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.Command;
import jcu.sal.components.EndPoints.FSEndPoint;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.utils.PlatformHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @author gilles
 *
 */
public class OSDataProtocol extends AbstractProtocol implements Runnable{
	
	class OSdata {
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
	}
	
	private static Logger logger = Logger.getLogger(OSDataProtocol.class);
	private Thread update_counters = null;
	private Hashtable<String,String> lastValues;
	private static Hashtable<String,OSdata> supportedSensors = new Hashtable<String,OSdata>();
	/**
	 * User, System, Nice and idle tiem counter update interval in millisecond
	 */
	private int UPDATE_INTERVAL = 10 * 1000;
	
	
	/**
	 * Construct the OSDataProtocol object
	 */
	public OSDataProtocol(ProtocolID i, Hashtable<String,String> c, Node d){
		super(i,OSDataConstants.OSDATAPROTOCOL_TYPE,c,d);
		Slog.setupLogger(logger);
		
		//Add to the list of supported sensors
		supportedSensors.put(OSDataConstants.FreeMem,new OSdata("/proc/meminfo", "MemFree", 2, null, true));
		supportedSensors.put(OSDataConstants.UserTime,new OSdata("/proc/stat", "cpu0",2, null, false));
		supportedSensors.put(OSDataConstants.NiceTime,new OSdata("/proc/stat", "cpu0",3, null, false));
		supportedSensors.put(OSDataConstants.SystemTime,new OSdata("/proc/stat", "cpu0", 4, null, false));
		supportedSensors.put(OSDataConstants.IdleTime,new OSdata("/proc/stat", "cpu0",5, null, false));
		supportedSensors.put(OSDataConstants.LoadAvg1,new OSdata("/proc/loadavg", null, 1, null, false));
		supportedSensors.put(OSDataConstants.LoadAvg5,new OSdata("/proc/loadavg", null, 2, null, false));
		supportedSensors.put(OSDataConstants.LoadAvg15,new OSdata("/proc/loadavg", null, 3, null, false));
		
		lastValues = new Hashtable<String,String>();
		autodetect = true;
		AUTODETECT_INTERVAL = -1; //run only once
//		Add to the list of supported EndPoint IDs
		supportedEndPointTypes.add(FSEndPoint.FSENDPOINT_TYPE);
		multipleInstances=false;
	}

	
	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_parseConfig()
	 */
	protected void internal_parseConfig() throws ConfigurationException {
		cmls = CMLDescriptionStore.getStore();
		try {
			if(getConfig(OSDataConstants.CPUTempFile)!=null)
				supportedSensors.put(OSDataConstants.CPUTemp,new OSdata(getConfig(OSDataConstants.CPUTempFile), null, 1, null, false));
		} catch (BadAttributeValueExpException e) {}
		try {
			if(getConfig(OSDataConstants.NBTempFile)!=null)
				supportedSensors.put(OSDataConstants.NBTemp,new OSdata(getConfig(OSDataConstants.NBTempFile), null, 1, null, false));
		} catch (BadAttributeValueExpException e) {}
		try {
			if(getConfig(OSDataConstants.SBTempFile)!=null)
				supportedSensors.put(OSDataConstants.SBTemp,new OSdata(getConfig(OSDataConstants.SBTempFile), null, 1, null, false));
		} catch (BadAttributeValueExpException e) {}
		
		logger.debug("OSData protocol configured");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_stop()
	 */
	protected void internal_stop() {stopCounterThread();}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_start()
	 */
	protected void internal_start() {startCounterThread();}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_remove()
	 */
	protected void internal_remove() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_isSensorSupported(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_isSensorSupported(Sensor sensor){
		return supportedSensors.containsKey(sensor.getNativeAddress());	
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_probeSensor(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_probeSensor(Sensor s) {
		OSdata d = supportedSensors.get(s.getNativeAddress());
		if(d!=null) {
			try {
				if(PlatformHelper.isFileReadable(d.file)) {
					logger.debug(s.toString()+" present");
					s.enable();
					return true;
				}
			} catch (Exception e) {
				logger.error("couldnt probe sensor "+s.toString()+". Raised exception: "+e.getMessage());
			}
			logger.debug("Disconnecting sensor "+s.toString()+", couldnt find the matching file: "+d.file);
		} else 
			logger.debug("Disconnecting sensor "+s.toString()+", couldnt find the matching file for "+s.getNativeAddress());
		s.disconnect();
		return false;
	}
	
	@Override
	protected List<String> detectConnectedSensors() {
		List<String> v = new Vector<String>();
		//check that all our supported sensors are here, if not remove them from supportedSensors
		Iterator<String> i = supportedSensors.keySet().iterator();
		OSdata d;
		String s;
		while(i.hasNext()) {
			s = i.next();
			d = supportedSensors.get(s);
			if(!PlatformHelper.isFileReadable(d.file)) {
				logger.error("Cant find file "+d.file);
				i.remove();
			} else
				v.add(s);
		}
		return v; 
	}
	
	private synchronized void startCounterThread() {
		if(update_counters == null || !update_counters.isAlive()) {
			update_counters = new Thread(this, "update_counter_thread");
			update_counters.start();
		}
	}
	
	private synchronized void stopCounterThread() {
		if(update_counters!=null && update_counters.isAlive()){
			update_counters.interrupt();
			try { update_counters.join();}
			catch (InterruptedException e) {}
			update_counters=null;
		}
	}


	@Override
	protected String internal_getCMLStoreKey(Sensor s){
		return s.getNativeAddress();
	}
	
	public void run(){
		OSdata d;
		double u, n, s, i, pu=-1, pn=0, ps=0, pi=0, sum;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		try {
			while(!Thread.interrupted()){
				//update UserTime, NiceTime, SystemTime and IdleTime
				if((d = supportedSensors.get(OSDataConstants.UserTime))!=null) {
					try {
						u = Integer.parseInt(PlatformHelper.getFieldFromFile(d.file, d.pattern, d.field, d.delim, d.translate));
						d = supportedSensors.get(OSDataConstants.NiceTime);
						n = Integer.parseInt(PlatformHelper.getFieldFromFile(d.file, d.pattern, d.field, d.delim, d.translate));
						d = supportedSensors.get(OSDataConstants.SystemTime);
						s = Integer.parseInt(PlatformHelper.getFieldFromFile(d.file, d.pattern, d.field, d.delim, d.translate));
						d = supportedSensors.get(OSDataConstants.IdleTime);
						i = Integer.parseInt(PlatformHelper.getFieldFromFile(d.file, d.pattern, d.field, d.delim, d.translate));
						
						if(pu!=-1) {
							sum = (u-pu) + (n-pn) + (s-ps) + (i-pi);
							lastValues.put(OSDataConstants.UserTime, nf.format(100*(u-pu)/sum));
							lastValues.put(OSDataConstants.NiceTime,  nf.format(100*(n-pn)/sum));
							lastValues.put(OSDataConstants.SystemTime, nf.format(100*(s-ps)/sum));
							lastValues.put(OSDataConstants.IdleTime, nf.format(100*(i-pi)/sum));
							pu=u;pn=n;ps=s;pi=i;
						} else {
							pu = u;
							pn = n;
							ps = s;
							pi = i;
							lastValues.put(OSDataConstants.UserTime, "0");
							lastValues.put(OSDataConstants.NiceTime, "0");
							lastValues.put(OSDataConstants.SystemTime, "0");
							lastValues.put(OSDataConstants.IdleTime, "0");
							Thread.sleep(1000);
							continue;							
						}
					} catch (IOException e) {
						lastValues.put(OSDataConstants.UserTime, "-1");
						lastValues.put(OSDataConstants.NiceTime, "-1");
						lastValues.put(OSDataConstants.SystemTime, "-1");
						lastValues.put(OSDataConstants.IdleTime, "-1");
					}					
				} else break;
				Thread.sleep(UPDATE_INTERVAL);
	
			}// end while interrupted
		} catch (InterruptedException e) {}
		logger.debug("update counter thread exiting");
	}
	
	/*
	 * command handling methods
	 */

	// TODO create an exception class for this instead of Exception
	public static String GET_READING_METHOD = "getReading";
	public byte[] getReading(Command c, Sensor s) throws IOException{
		OSdata d;
		String ret;
		if(s.getNativeAddress().equals(OSDataConstants.UserTime) || s.getNativeAddress().equals(OSDataConstants.NiceTime) || s.getNativeAddress().equals(OSDataConstants.SystemTime)|| s.getNativeAddress().equals(OSDataConstants.IdleTime)) {
			ret = lastValues.get(s.getNativeAddress());
			if(ret.equals("-1")) { 
				logger.error("couldnt run the command to get readings for sensor "+ s.toString());
				s.disable();
				throw new IOException("can read " +s.getNativeAddress()); 
			}
		} else {
			d = supportedSensors.get(s.getNativeAddress());
			try { ret = PlatformHelper.getFieldFromFile(d.file, d.pattern, d.field, d.delim, d.translate); }
			catch (IOException e) {
				logger.error("couldnt get a reading for "+s.toString());
				throw e;
			}
			if(s.getNativeAddress().equals(OSDataConstants.CPUTemp) || s.getNativeAddress().equals(OSDataConstants.NBTemp) || s.getNativeAddress().equals(OSDataConstants.SBTemp)){
				ret = ret.substring(0, 2)+"."+ret.substring(2,4);
			}
		}
		return ret.getBytes();
	}

}
