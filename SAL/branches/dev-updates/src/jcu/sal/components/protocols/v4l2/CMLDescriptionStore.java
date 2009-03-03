package jcu.sal.components.protocols.v4l2;

import java.util.List;
import java.util.Vector;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore {
	public static String CCD_KEY = "CCD";
	public static String CONTROL_VALUE_NAME="value";
	public static String WIDTH_VALUE_NAME="width";
	public static String HEIGHT_VALUE_NAME="height";
	public static String CHANNEL_VALUE_NAME="channel";
	public static String STANDARD_VALUE_NAME="standard";
	public static String QUALITY_VALUE_NAME="quality";
	public static String CALLBACK_ARG_NAME="Callback";

	
	public static CMLDescriptionStore getStore() {
		//return c;
		try {
			return new CMLDescriptionStore();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private CMLDescriptionStore() throws NotFoundException, AlreadyPresentException{
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
		argNames.add(WIDTH_VALUE_NAME);
		t.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));
		argNames.add(HEIGHT_VALUE_NAME);
		t.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));
		argNames.add(CHANNEL_VALUE_NAME);
		t.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));
		argNames.add(STANDARD_VALUE_NAME);
		t.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));
		argNames.add(QUALITY_VALUE_NAME);
		t.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));
		r = new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic GetReading
		addGenericCMLDesc(CCD_KEY, GENERIC_GETREADING, i);
		
		t.add(new ArgumentType(CMLConstants.ARG_TYPE_CALLBACK));
		argNames.add(CALLBACK_ARG_NAME);
		mName = V4L2Protocol.START_STREAM_METHOD;
		name =  "StartStream";
		desc = "Starts a new JPEG stream";
		r = new ReturnType(CMLConstants.RET_TYPE_VOID);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic startStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STARTSTREAM, i);
		
		t = new Vector<ArgumentType>();
		argNames = new Vector<String>();
		mName = V4L2Protocol.STOP_STREAM_METHOD;
		name = "StopStream";
		desc = "Stops a JPEG stream";
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic stopStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STOPSTREAM, i);
		
		mName = V4L2Protocol.STOP_STREAM_FAKE_METHOD;
		name = "StopStreamFake";
		desc = "Stops a fake JPEG stream";
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
				

		mName = V4L2Protocol.START_STREAM_FAKE_METHOD;
		name =  "StartStreamFake";
		desc = "Starts a new fake JPEG stream";
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
	}
}
