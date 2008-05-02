package jcu.sal.components.protocols;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.ConfigurationException;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * Defines the base behaviour of CMLStore objects
 * Each CML store (class which stores tables of CML docs for each valid command of a sensor managed by a protocol)
 * must extend this class. CMLstores should be instanciated by protocols in order to avoid duplicates,
 * and save memory
 * @author gilles
 *
 */
public class CMLStore {
	
	private Logger logger = Logger.getLogger(CMLStore.class);
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
	/*
	 * Other generic commands:
	 * 100: getReading (owfs, osdata, ssnmp, v4l2)
	 * 101: startStream (v4l2)
	 * 102: stopStream (v4l2)
	 * 110: getTemperature (owfs, osdata)
	 * 111: getHumidity (owfs)
	 */
	
	public static class CMLDoc {
		private static Logger logger = Logger.getLogger(CMLDoc.class);
		private Integer cid;
		private String cml;
		private String methodName;
		
		public static String STRING_ARG_TYPE="string";
		public static String INT_ARG_TYPE="int";
		public static String FLOAT_ARG_TYPE="float";
		public static String CALLBACK_ARG_TYPE="callback";
		
		public CMLDoc(String mName, Integer id, String name, String desc, String[] argTypes,  String[] names) throws ConfigurationException{
			Slog.setupLogger(logger);
			cid=id;
			if(argTypes.length!=names.length) {
				logger.error("Error creating the CML doc: arguments number unequals somewhere");
				throw new ConfigurationException();
			}

			cml = "<CommandDescription name=\""+name+"\">\n"
					+"\t<CID>"+id.toString()+"</CID>\n"
					+"\t<ShortDescription>"+desc+"</ShortDescription>\n"
					+"\t<arguments count=\""+argTypes.length+"\">\n";
			for (int i = 0; i < argTypes.length; i++) {
				if(!argTypes[i].equals(STRING_ARG_TYPE) && !argTypes[i].equals(INT_ARG_TYPE) && !argTypes[i].equals(FLOAT_ARG_TYPE) && !argTypes[i].equals(CALLBACK_ARG_TYPE)){
					logger.error("Error creating the CML doc: wrong argument type");
					throw new ConfigurationException();
				}
					
				cml += "\t\t<Argument type=\""+argTypes[i]+"\">"+names[i]+"</Argument>\n";
			}

			cml +=	"\t</arguments>\n"
					+"</CommandDescription>\n";
			
			methodName = mName;
		}
		
		public CMLDoc(Integer id, String name, CMLDoc existing) throws ConfigurationException{
			Slog.setupLogger(logger);
			cid=id;
			cml = existing.getCML();
			cml = cml.replaceFirst("<CommandDescription.*\n.*/CID>", "");

			cml = "<CommandDescription name=\""+name+"\">\n"
					+"\t<CID>"+id.toString()+"</CID>\n"
					+cml;
			
			methodName = existing.getMethodName();
		}

	
		public Integer getCID(){
			return cid;
		}
		
		public String getCML(){
			return cml;
		}
		
		public String getMethodName(){
			return methodName;
		}
	}
	
	/**
	 * the table where CML docs are stored. Which key is used is up to the protocol.
	 * Native address, sensor families do the job, as long as they re unique !
	 * The table must be filled by the subclass constructor.
	 */
	private Hashtable<String, Hashtable<Integer, CMLDoc>> cmls;
	/**
	 * This table contains the next available private cid
	 */
	private Hashtable<String,Integer> priv_cid;
	
	/**
	 * No arg constructor
	 */
	protected CMLStore() {
		Slog.setupLogger(logger);
		cmls = new Hashtable<String, Hashtable<Integer, CMLDoc>>();
		priv_cid = new Hashtable<String, Integer>();
	}

	/**
	 * Retrieves the CML document for the given key f (native address, sensor family, ...)
	 * @param k the key
	 * @return the CML doc 
	 * @throws ConfigurationException if the key can not be found 
	 */
	public String getCML(String k) throws ConfigurationException{
		if(!cmls.containsKey(k)) {
			logger.error("Cant find key "+k);
			throw new ConfigurationException();
		}
		StringBuffer b = new StringBuffer();
		Enumeration<CMLDoc> i = cmls.get(k).elements();
		while(i.hasMoreElements())
			b.append(i.nextElement().getCML());
		return b.toString();

	}
	
	/**
	 * Retrieves the method name associated with a given command
	 * @param k the key 
	 * @param cid the command id
	 * @return the method name
	 * @throws ConfigurationException if the method can not be found 
	 */
	public String getMethodName(String k, Integer cid) throws ConfigurationException{
		Hashtable<Integer, CMLDoc> t = cmls.get(k);
		if(t==null) {
			logger.error("Cant find key "+k);
			throw new ConfigurationException();
		}
		CMLDoc d = t.get(cid);
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
	 * @param argTypes an array containing the types (CMLDoc.*_TYPE) of the arguments
	 * @param names an array containing the names of the arguments
	 * @return the cid associated with this command
	 * @throws ConfigurationException if the command cant be created
	 */
	public final int addPrivateCMLDesc(String k, String mName, String name, String desc, String[] argTypes, String[] names) throws ConfigurationException{
		//computes the CID
		Integer cid = priv_cid.get(k);
		if(cid==null)
			cid = new Integer(PRIVATE_CID_START);
		//builds the CML desc doc
		logger.debug("Adding private CML for key "+k+", method: "+mName+", CID: "+cid.intValue());
		addCML(k, new CMLDoc(mName,cid, name, desc, argTypes, names));
		priv_cid.put(k, new Integer(cid.intValue()+1));
				
		return cid.intValue();
	}
	
	/**
	 * This method adds an alias to a previously created Command Description document fragment identifier by its cid
	 * @param k the key with which the alias is to be associated
	 * @param name the name of the alias (CMLStore.GENERIC_*)
	 * @param cid the previously created command
	 * @return the cid associated with this command
	 * @throws ConfigurationException if the command cant be created
	 */
	public final int addGenericCMLDesc(String k, String aliasName, Integer cid) throws ConfigurationException{
		//computes the CID
		Integer c;
		CMLDoc cml;
		logger.debug("Adding generic CML for key "+k+", alias: "+aliasName);
		if(aliasName.equals(GENERIC_ENABLE)){
			c = new Integer(GENERIC_ENABLE_CID);
			String[] s = new String[0];
			addCML(k, new CMLDoc(null, c, GENERIC_ENABLE, "Enables the sensor", s, s));
			return c.intValue();
		} else if(aliasName.equals(GENERIC_DISABLE)){
			c = new Integer(GENERIC_DISABLE_CID);
			String[] s = new String[0];
			addCML(k, new CMLDoc(null, c, GENERIC_DISABLE, "Disables the sensor", s, s));
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
		
		Hashtable<Integer, CMLDoc> t = cmls.get(k);
		if(t != null) {
			cml = t.get(cid);
			if(cml==null){
				logger.error("Cant find any pre-existing command "+cid+" to create the alias");
				throw new ConfigurationException();				
			}
		} else {
			logger.error("Cant find key "+k);
			throw new ConfigurationException();
		}

		//builds the CML desc doc based on the existing one
		addCML(k, new CMLDoc(c, aliasName, cml));
				
		return cid.intValue()-1;
	}

	/**
	 * Creates a new CML list for a new sensor identified by its key k
	 * @param k the sensor key
	 */
	private Hashtable<Integer, CMLDoc> addSensor(String k) throws ConfigurationException {
		Hashtable<Integer, CMLDoc> t;
		if(cmls.containsKey(k)){
			logger.error("trying to add a CML table for sensor " + k + " which already has a CML table.");
			throw new ConfigurationException();
		}

		t = new Hashtable<Integer, CMLDoc>();
		
		/* create the table */ 
		cmls.put(k, t);

		/* Add generic CML docs to this sensor */
		/* generic enable command */
		addGenericCMLDesc(k, GENERIC_ENABLE, null);
		
		/* generic disable command */
		addGenericCMLDesc(k, GENERIC_DISABLE, null);
		
		return t;

	}
	
	/**
	 * Adds a CML document to the CML table for a given sensor
	 * @param k the sensor
	 * @param v the CML doc
	 * @throws ConfigurationException 
	 */
	private void addCML(String k, CMLDoc v) throws ConfigurationException{
		Hashtable<Integer, CMLDoc> t = cmls.get(k);
		if(t==null)
			t = addSensor(k);
		if(!t.containsKey(v.getCID()))
			t.put(v.getCID(), v);
		else {
			logger.error("trying to add a CML doc (cid:"+v.getCID()+") to sensor " + k + " which already holds a CML with this id.");
			throw new ConfigurationException();
		}
		dumpCML(k);
	}
	
	private void dumpCML(String k){
		Hashtable<Integer, CMLDoc> t = cmls.get(k);
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
