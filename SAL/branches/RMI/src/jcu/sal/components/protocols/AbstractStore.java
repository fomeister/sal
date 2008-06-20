package jcu.sal.components.protocols;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * Defines the base behaviour of AbstractStore objects
 * Each CML store (class which stores tables of CML docs for each valid command of a sensor managed by a protocol)
 * must extend this class. CMLstores should be instanciated by protocols in order to avoid duplicates,
 * and save memory
 * @author gilles
 *
 */
public abstract class AbstractStore {
	
	private Logger logger = Logger.getLogger(AbstractStore.class);
	

	public static String GENERIC_ENABLE="Enable";			//10
	public static String GENERIC_DISABLE="Disable";		//11
	public static String GENERIC_GETREADING="getReading";	//100
	public static String GENERIC_STARTSTREAM="startStream";	//102
	public static String GENERIC_STOPSTREAM="stopStream";	//103
	public static String GENERIC_GETTEMP="getTemperature";	//110
	public static String GENERIC_GETHUM="getHumidity";		//111
	
	public static int GENERIC_ENABLE_CID=10;
	public static int GENERIC_DISABLE_CID=11;
	public static int GENERIC_GETREADING_CID=100;
	public static int GENERIC_STARTSTREAM_CID=102;
	public static int GENERIC_STOPSTREAM_CID=103;
	public static int GENERIC_GETTEMP_CID=110;
	public static int GENERIC_GETHUM_CID=111;
	
	private static int PRIVATE_CID_START = 1000;
	
	/**
	 * the table where CML docs are stored. Which key is used is up to the protocol.
	 * Native address, sensor families do the job, as long as they re unique !
	 * The table must be filled by the subclass constructor.
	 */
	private Hashtable<String, Hashtable<Integer, CMLDescription>> cmls;
	/**
	 * This table contains the next available private cid
	 */
	private Hashtable<String,Integer> priv_cid;
	
	
	/**
	 * No arg constructor
	 */
	protected AbstractStore() {
		Slog.setupLogger(logger);
		cmls = new Hashtable<String, Hashtable<Integer, CMLDescription>>();
		priv_cid = new Hashtable<String, Integer>();
	}

	/**
	 * Retrieves the CML descriptions document for the given key f (native address, sensor family, ...)
	 * @param k the key
	 * @return the CML command descriptions doc 
	 * @throws ConfigurationException if the key can not be found 
	 */
	public CMLDescriptions getCMLDescriptions(String k) throws ConfigurationException{
		if(!cmls.containsKey(k)) {
			logger.error("Cant find key "+k);
			throw new ConfigurationException();
		}
				
		return new CMLDescriptions(cmls.get(k));
	}
	
	/**
	 * Retrieves the method name associated with a given command
	 * @param k the key 
	 * @param cid the command id
	 * @return the method name
	 * @throws ConfigurationException if the method can not be found 
	 */
	public String getMethodName(String k, int cid) throws ConfigurationException{
		Hashtable<Integer, CMLDescription> t = cmls.get(k);
		if(t==null) {
			logger.error("Cant find key "+k);
			throw new ConfigurationException();
		}
		CMLDescription d = t.get(new Integer(cid));
		if(d==null){
			logger.error("Cant find the cid "+cid);
			throw new ConfigurationException();
		}
		return d.getMethodName();
	}
	
	/**
	 * This method adds a new private Command Description document fragment to the CML doc for a given key.
	 * @param k the key with which the fragment is to be associated
	 * @param mName the name of the method to be called when this command is received
	 * @param name the name of the command
	 * @param desc the description of the command
	 * @param argTypes an array containing the types (CMLDescription.*_TYPE) of the arguments
	 * @param names an array containing the names of the arguments
	 * @return the cid associated with this command
	 * @throws ConfigurationException if the command cant be created
	 */
	public final int addPrivateCMLDesc(String k, String mName, String name, String desc, List<ArgumentType> argTypes, List<String> names, ReturnType returnType) throws ConfigurationException{
		//computes the CID
		Integer cid = priv_cid.get(k);
		if(cid==null)
			cid = new Integer(PRIVATE_CID_START);
		//builds the CML desc doc
		logger.debug("Adding private CML for key "+k+", method: "+mName+", CID: "+cid.intValue());
		addCML(k, new CMLDescription(mName,cid, name, desc, argTypes, names, returnType));
		priv_cid.put(k, new Integer(cid.intValue()+1));
				
		return cid.intValue();
	}
	
	/**
	 * This method adds an alias to a previously created Command Description document fragment identifier by its cid
	 * @param k the key with which the alias is to be associated
	 * @param name the name of the alias (AbstractStore.GENERIC_*)
	 * @param cid the previously created command
	 * @return the cid associated with this command
	 * @throws ConfigurationException if the command cant be created
	 */
	public final int addGenericCMLDesc(String k, String aliasName, int cid) throws ConfigurationException{
		//computes the CID
		Integer c;
		CMLDescription cml;
		List<ArgumentType> t = new Vector<ArgumentType>();
		List<String> s = new Vector<String>();
		logger.debug("Adding generic CML for key "+k+", alias: "+aliasName);
		if(aliasName.equals(GENERIC_ENABLE)){
			c = new Integer(GENERIC_ENABLE_CID);
			addCML(k, new CMLDescription(null, c, GENERIC_ENABLE, "Enables the sensor", t, s, new ReturnType(CMLConstants.RET_TYPE_VOID)));
			return c.intValue();
		} else if(aliasName.equals(GENERIC_DISABLE)){
			c = new Integer(GENERIC_DISABLE_CID);
			addCML(k, new CMLDescription(null, c, GENERIC_DISABLE, "Disables the sensor", t, s, new ReturnType(CMLConstants.RET_TYPE_VOID)));
			return c.intValue();
		} else if(aliasName.equals(GENERIC_GETREADING)){
			c = new Integer(GENERIC_GETREADING_CID);
		} else if(aliasName.equals(GENERIC_STARTSTREAM)){
			c = new Integer(GENERIC_STARTSTREAM_CID);
		} else if(aliasName.equals(GENERIC_STOPSTREAM)){
			c = new Integer(GENERIC_STOPSTREAM_CID);
		} else if(aliasName.equals(GENERIC_GETTEMP)){
			c = new Integer(GENERIC_GETTEMP_CID);
		} else if(aliasName.equals(GENERIC_GETHUM)){
			c = new Integer(GENERIC_GETHUM_CID);
		} else {
			logger.error("Cant create an alias, no such name");
			throw new ConfigurationException();
		}
		
		Hashtable<Integer, CMLDescription> table = cmls.get(k);
		if(table != null) {
			cml = table.get(new Integer(cid));
			if(cml==null){
				logger.error("Cant find any pre-existing command "+cid+" to create the alias");
				throw new ConfigurationException();				
			}
		} else {
			logger.error("Cant find key "+k);
			throw new ConfigurationException();
		}

		//builds the CML desc doc based on the existing one
		addCML(k, new CMLDescription(c, aliasName, cml));
				
		return cid-1;
	}

	/**
	 * Creates a new CML list for a new sensor identified by its key k
	 * @param k the sensor key
	 */
	private Hashtable<Integer, CMLDescription> addSensor(String k) throws ConfigurationException {
		Hashtable<Integer, CMLDescription> t;
		if(cmls.containsKey(k)){
			logger.error("trying to add a CML table for sensor " + k + " which already has a CML table.");
			throw new ConfigurationException();
		}

		t = new Hashtable<Integer, CMLDescription>();
		
		/* create the table */ 
		cmls.put(k, t);

		/* Add generic CML docs to this sensor */
		/* generic enable command */
		addGenericCMLDesc(k, GENERIC_ENABLE, 0);
		
		/* generic disable command */
		addGenericCMLDesc(k, GENERIC_DISABLE, 0);
		
		return t;

	}
	
	/**
	 * Adds a CML document to the CML table for a given sensor
	 * @param k the sensor
	 * @param v the CML doc
	 * @throws ConfigurationException 
	 */
	private void addCML(String k, CMLDescription v) throws ConfigurationException{
		Hashtable<Integer, CMLDescription> t = cmls.get(k);
		if(t==null)
			t = addSensor(k);
		if(!t.containsKey(v.getCID()))
			t.put(v.getCID(), v);
		else {
			logger.error("trying to add a CML doc (cid:"+v.getCID()+") to sensor " + k + " which already holds a CML with this id.");
			throw new ConfigurationException();
		}
		if(false) dumpCML(k);
	}
	
	private void dumpCML(String k){
		Hashtable<Integer, CMLDescription> t = cmls.get(k);
		Integer i;
		if(t!=null){
			Enumeration<Integer> e = t.keys();
			logger.debug("CML store dump:");
			while(e.hasMoreElements()){
				i=e.nextElement();
				logger.debug("CML Doc CID: "+i.intValue());
				logger.debug("\tMethod name: "+t.get(i).getMethodName());
			}
		} else {
			logger.debug("No such key in cmlstore");
		}
	}

}
