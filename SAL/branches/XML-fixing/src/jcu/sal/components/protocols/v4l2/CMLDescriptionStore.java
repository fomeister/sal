package jcu.sal.components.protocols.v4l2;

import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore {
	public static String CCD_KEY = "CCD";
	public static String CONTROL_VALUE_NAME="value";
	public static String CALLBACK_ARG_NAME="Callback";

	
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
		List<String> argNames;
		List<ArgumentType> t;
		ReturnType r;
		
		/* 
		 * CCD sensor
		 * */
		key = CCD_KEY;
		mName = V4L2Protocol.GET_FRAME_METHOD;
		name = "GetFrame";
		desc = "Reads a single frame";
		t = new Vector<ArgumentType>();
		argNames = new Vector<String>();
		r = new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic GetReading
		addGenericCMLDesc(CCD_KEY, GENERIC_GETREADING, i);
		
		mName = V4L2Protocol.STOP_STREAM_METHOD;
		name = "StopStream";
		desc = "Stops a JPEG stream";
		r = new ReturnType(CMLConstants.RET_TYPE_VOID);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic stopStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STOPSTREAM, i);
		
		mName = V4L2Protocol.STOP_STREAM_FAKE_METHOD;
		name = "StopStreamFake";
		desc = "Stops a fake JPEG stream";
		r = new ReturnType(CMLConstants.RET_TYPE_VOID);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
				
		
		t.add(new ArgumentType(CMLConstants.ARG_TYPE_CALLBACK));
		argNames.add(CALLBACK_ARG_NAME);
		mName = V4L2Protocol.START_STREAM_METHOD;
		name =  "StartStream";
		desc = "Starts a new JPEG stream";
		r = new ReturnType(CMLConstants.RET_TYPE_VOID);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic startStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STARTSTREAM, i);
		

		mName = V4L2Protocol.START_STREAM_FAKE_METHOD;
		name =  "StartStreamFake";
		desc = "Starts a new fake JPEG stream";
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);

	}
}
