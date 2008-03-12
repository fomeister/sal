package jcu.sal.Components.Protocols.CMLStore;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * Defines the base behaviour of CMLStores objects
 * Each CML store (class which stores tables of CML docs for given sensors of a protocol)
 * must extend this class. CMLstores should be instanciated by protocols in order to avoid duplicates,
 * and save memory
 * @author gilles
 *
 */
public class CMLStore {
	private Logger logger = Logger.getLogger(CMLStore.class);
	public static int ENABLE_CID=10;
	public static int DISABLE_CID=11;
	
	protected class CMLDoc {
		private Integer cid;
		private String cml;
		
		public CMLDoc(Integer id, String c){
			cid=id; cml=c;
		}
		
		public CMLDoc(int id, String c){
			cid=id; cml=c;
		}
		
		public Integer getCID(){
			return cid;
		}
		
		public String getCML(){
			return cml;
		}
	}
	
	/**
	 * the table where CML docs are stored. Which key is used is up to the protocol.
	 * Native address, sensor families do the job, as long as they re unique !
	 * The table must be filled by the subclass constructor.
	 */
	private static Hashtable<String, Hashtable<Integer, CMLDoc>> cmls = new Hashtable<String, Hashtable<Integer, CMLDoc>>();
	
	/**
	 * Adds the following generic commands to the CML store
	 * Enable - Disable commands
	 *
	 */
	protected CMLStore() {
		Slog.setupLogger(logger);
	}

	/**
	 * Retrieves the CML document for the given key f (native address, sensor family, ...)
	 * @param f the key
	 * @return the CML doc or null if the key can not be found
	 */
	public String getCML(String f){
		if(cmls.containsKey(f)) {
			StringBuffer b = new StringBuffer();
			Enumeration<CMLDoc> i = cmls.get(f).elements();
			while(i.hasMoreElements())
				b.append(i.nextElement().getCML());
			return b.toString();
		}
		return null;
	}

	/**
	 * Adds a CML document to the CML table for a given sensor
	 * @param k the sensor
	 * @param v the CML doc
	 * @throws ConfigurationException 
	 */
	public void addCML(String k, CMLDoc v) throws ConfigurationException{
		Hashtable<Integer, CMLDoc> t = cmls.get(k);
		if(!t.containsKey(v.getCID()))
			t.put(v.getCID(), v);
		else {
			logger.error("trying to add a CML doc (cid:"+v.getCID()+") to sensor " + k + " which already holds a CML for that id.");
			throw new ConfigurationException();
		}
	}
	
	/**
	 * Creates a new CML table for a enw sensor
	 * @param k the sensor
	 */
	public void addSensor(String k) throws ConfigurationException {
		if(!cmls.containsKey(k)){
			Hashtable<Integer, CMLDoc> t = new Hashtable<Integer, CMLDoc>();
			StringBuffer b = new StringBuffer();
			CMLDoc c;
			
			/* Add generic CML docs to this sensor */
			//generic 10 enable command
			b.append("<Command name=\"Enable\">\n");
			b.append("\t<CID>"+ENABLE_CID+"</CID>\n");
			b.append("\t<ShortDescription>Enables the sensor</ShortDescription>\n");
			b.append("\t<arguments count=\"0\" />\n");
			b.append("\t<returnValues count=\"0\" />\n");
			b.append("\t</returnValues>\n");
			b.append("</Command>\n");
			c = new CMLDoc(ENABLE_CID, b.toString());
			t.put(c.getCID(),c);
			b.delete(0, b.length());
			
			//generic 11 disable command
			b.append("<Command name=\"Disable\">\n");
			b.append("\t<CID>"+DISABLE_CID+"</CID>\n");
			b.append("\t<ShortDescription>Disables the sensor</ShortDescription>\n");
			b.append("\t<arguments count=\"0\" />\n");
			b.append("\t<returnValues count=\"0\" />\n");
			b.append("\t</returnValues>\n");
			b.append("</Command>\n");
			c = new CMLDoc(DISABLE_CID, b.toString());
			t.put(c.getCID(),c);
			
			/* create the final table */ 
			cmls.put(k, t);
		}
		else {
			logger.error("trying to add a CML table for sensor " + k + " which already has a CML table.");
			throw new ConfigurationException();
		}

	}

}
