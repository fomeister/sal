/**
 * 
 */
package jcu.sal.Managers;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Identifier;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.Components.Sensors.SensorID;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * @author gilles
 * 
 */
public class SensorManager extends ManagerFactory<Sensor> implements Runnable{
	
	/**
	 * specifies (in seconds) how long disconnected sensors should remain before being
	 * deleted
	 */
	public static long DISCONNECT_TIMEOUT = 20;
	private Thread sensor_removal;
	
	/**
	 * specifies (in seconds) how often the sensor removal thread kick in
	 */
	public static int REMOVE_SENSOR_INTERVAL = 0;
	
	private static SensorManager s = new SensorManager();
	private Logger logger = Logger.getLogger(SensorManager.class);
	private ProtocolManager pm;
	
	
	/**
	 * Private constructor
	 */
	private SensorManager() {
		super();
		Slog.setupLogger(this.logger);
		pm = ProtocolManager.getProcotolManager();
		sensor_removal = new Thread(this, "sensor_manager_thread");
	}
	
	/**
	 * Returns the instance of the SensorManager 
	 * @return
	 */
	public static SensorManager getSensorManager() {
		return s;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected Sensor build(Node n) throws InstantiationException {
		SensorID i = null;
		Sensor sensor = null;
		logger.debug("building sensor");
		try {
			i = (SensorID) this.getComponentID(n);
			sensor = new Sensor(i, getComponentConfig(n));
			pm.associateSensor(sensor);
		} catch (ConfigurationException e) {
			logger.error("Couldnt instanciate/associate the sensor: " + i.toString());
			//e.printStackTrace();
			throw new InstantiationException();
		} 
		return sensor;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(Node n){
		Identifier id = null;
		try {
			id = new SensorID(XMLhelper.getAttributeFromName("//" + Sensor.SENSOR_TAG, Sensor.SENSORID_TAG, n) );
		} catch (Exception e) {
//			logger.error("Couldnt find the Sensor id - creating a new one");
//			e.printStackTrace();
			id = new SensorID(generateNewSensorID());
			//throw new ParseException("Couldnt create the Sensor identifier", 0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(Sensor component) {
		try {
			pm.unassociateSensor(component);
		} catch (ConfigurationException e) {
			logger.error("Error unassociating sensor");
			e.printStackTrace();
		}
		component.remove(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.Managers.ManagerFactory#getComponentType(org.w3c.dom.Node)
	 */
	@Override
	protected String getComponentType(Node n) throws ParseException {
		return Sensor.SENSOR_TYPE;
	}
	
	public String listSensors(){
		StringBuilder b = new StringBuilder();
		Sensor s;
		b.append("<?xml version=\"1.0\"?>\n<SAL>\n\t<SensorConfiguration>\n");
		synchronized (this) {
			Iterator<Sensor> i = getIterator();
			while(i.hasNext()) {
				s = i.next();
				b.append("\t\t<"+Sensor.SENSOR_TAG+" "+Sensor.SENSORID_TAG+"=\""+s.getID().getName()+"\">\n");
				/* TODO "parameters" should be a static string somewhere ... */
				b.append("\t\t\t<parameters>\n");
				try {
					b.append("\t\t\t\t<"+SensorManager.COMPONENTPARAM_TAG+" name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\" value=\""+s.getConfig(Sensor.PROTOCOLATTRIBUTE_TAG)+"\">\n");
				} catch (BadAttributeValueExpException e) {
					logger.error("Cant find protocol's name from sensor config");
				}
				b.append("\t\t\t\t<"+SensorManager.COMPONENTPARAM_TAG+" name=\""+Sensor.SENSORADDRESSATTRIBUTE_TAG+"\" value=\""+s.getNativeAddress()+"\">\n");
				b.append("\t\t\t</parameters>\n");
				b.append("\t\t</"+Sensor.SENSOR_TAG+">\n");
			}	
		}
		b.append("\t</SensorConfiguration>\n</SAL>\n");
		return b.toString();
	}

	
	/**
	 * Returns the first available unused sensor ID
	 * Must hold the lock to ctable
	 * @return the first available unused sensor ID
	 */
	private String generateNewSensorID() {
		if(getSize()==0)
			return "1";
		
		int[] arr = new int[getSize()];
		Enumeration<Identifier> e = getKeys();
		int i=0;
		while(e.hasMoreElements()){
			arr[i++] = Integer.parseInt(e.nextElement().getName());
		}
		Arrays.sort(arr);
		if(arr[0]==1) {
			for (i = 1; i < arr.length; i++) {
				if(arr[i]>(arr[i-1]+1)) {
					break;
				}
			}
			i=arr[i-1]+1;
		} else i=1;
		
		return String.valueOf(i);
	}
	
	public void stop(){
		sensor_removal.interrupt();
	}
	public void start() {
		if(REMOVE_SENSOR_INTERVAL!=0)
			sensor_removal.start();
	}
	
	/**
	 * This method runs as a spearate thread and deletes sensors which
	 * have been disconnected for too long
	 *
	 */
	public void run() {
		Sensor s;
		long diff, disc_ts;
		logger.debug("Starting sensor removal thread");
		try { 
		while(!Thread.interrupted()){
			Enumeration<Identifier> e  = getKeys();
			while(e.hasMoreElements() && !Thread.interrupted()) {
				synchronized(this) {
					s = getComponent(e.nextElement());
					disc_ts = s.getDisconnectTimestamp();
					if(s!=null && disc_ts!=-1) {
						diff = System.currentTimeMillis() - disc_ts;
						if(diff > (DISCONNECT_TIMEOUT*1000)) {
							logger.debug("About to remove sensor " + s.toString()+ " disconnect timeout expired (diff="+diff+")");
							//try { ProtocolManager.getProcotolManager().removeSensor(s.getID());	} catch (ConfigurationException e1) {}
						}
					}
				}
			}
			Thread.sleep(REMOVE_SENSOR_INTERVAL*1000);
		}
		} catch (InterruptedException e1) {}
		logger.debug("Exiting sensor removal thread");
	}

}
