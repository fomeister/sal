package jcu.sal.components.protocols.v4l2;

import javax.naming.ConfigurationException;

import jcu.sal.common.CMLConstants;
import jcu.sal.components.protocols.AbstractStore;
import jcu.sal.components.protocols.CMLDescription.ArgTypes;
import jcu.sal.components.protocols.CMLDescription.ReturnType;


public class CMLDescriptionStore extends AbstractStore {
	public static String CCD_KEY = "CCD";
	public static String CONTROL_VALUE_NAME="value";

	
	public static CMLDescriptionStore getStore() {
		//return c;
		try {
			return new CMLDescriptionStore();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private CMLDescriptionStore() throws ConfigurationException{
		int i;
		String key, name, mName, desc;
		String[] argNames;
		ArgTypes[] t;
		ReturnType r;
		
		/* 
		 * CCD sensor
		 * */
		key = CCD_KEY;
		mName = V4L2Protocol.GET_FRAME_METHOD;
		name = "GetFrame";
		desc = "Reads a single frame";
		t = new ArgTypes[0];
		argNames = new String[0];
		r = new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic GetReading
		addGenericCMLDesc(CCD_KEY, GENERIC_GETREADING, i);

		mName = V4L2Protocol.START_STREAM_METHOD;
		name =  "StartStream";
		desc = "Starts a new JPEG stream";
		t = new ArgTypes[] {new ArgTypes(CMLConstants.ARG_TYPE_CALLBACK)};
		argNames = new String[] {"Callback"};
		r = new ReturnType(CMLConstants.RET_TYPE_VOID);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic startStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STARTSTREAM, i);

		mName = V4L2Protocol.STOP_STREAM_METHOD;
		name = "StopStream";
		desc = "Stops a JPEG stream";
		t = new ArgTypes[0];
		argNames = new String[0];
		r = new ReturnType(CMLConstants.RET_TYPE_VOID);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic stopStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STOPSTREAM, i);
	}
}
