package jcu.sal.components.protocols.v4l2;

import javax.naming.ConfigurationException;

import jcu.sal.components.protocols.CMLStore;


public class V4L2CML extends CMLStore {
	private static V4L2CML c;
	public static String CCD_KEY = "CCD";
	static {
		try {
			c = new V4L2CML();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} 
	}
	
	public static V4L2CML getStore() {
		return c;
	}
	
	private V4L2CML() throws ConfigurationException{
		int i;
		
		/* 
		 * CCD sensor
		 * */
		i = addPrivateCMLDesc(CCD_KEY, "getFrame", "GetFrame", "Reads a single frame", new String[0], new String[0]);
		//generic GetReading
		addGenericCMLDesc(CCD_KEY, GENERIC_GETREADING, new Integer(i));

		i = addPrivateCMLDesc(CCD_KEY, "NA", "StartJPEGStream", "Starts a new JPEG stream", new String[] {CMLDoc.CALLBACK_ARG_TYPE}, new String[] {"Callback"});
		//generic startStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STARTSTREAM, new Integer(i));

		i = addPrivateCMLDesc(CCD_KEY, "NA", "StopJPEGStream", "Stops a JPEG stream", new String[0], new String[0]);
		//generic stopStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STOPSTREAM, new Integer(i));
	}
}
