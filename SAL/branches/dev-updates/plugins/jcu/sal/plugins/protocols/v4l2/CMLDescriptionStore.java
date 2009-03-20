package jcu.sal.plugins.protocols.v4l2;

import java.util.List;
import java.util.Vector;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLArgument;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore {
	public static final String CCD_KEY = "CCD";
	public static final String CONTROL_VALUE_NAME="value";
	public static final String WIDTH_VALUE_NAME="width";
	public static final String HEIGHT_VALUE_NAME="height";
	public static final String CHANNEL_VALUE_NAME="channel";
	public static final String STANDARD_VALUE_NAME="standard";
	public static final String QUALITY_VALUE_NAME="quality";

	
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
		List<CMLArgument> args;
		ResponseType r;
		
		/* 
		 * CCD sensor
		 * */
		key = CCD_KEY;
		
		mName = V4L2Protocol.GET_JPEG_FRAME_METHOD;
		name = "GetJPEGFrame";
		desc = "Fetches a single JPEG-encoded frame";
		args = new Vector<CMLArgument>();
		args.add(new CMLArgument(WIDTH_VALUE_NAME, ArgumentType.IntegerArgument, false));
		args.add(new CMLArgument(HEIGHT_VALUE_NAME, ArgumentType.IntegerArgument, false));
		args.add(new CMLArgument(CHANNEL_VALUE_NAME, ArgumentType.IntegerArgument, false));
		args.add(new CMLArgument(STANDARD_VALUE_NAME, ArgumentType.IntegerArgument, false));
		args.add(new CMLArgument(QUALITY_VALUE_NAME, ArgumentType.IntegerArgument, false));
		r = new ResponseType(CMLConstants.RET_TYPE_BYTE_ARRAY,CMLConstants.CONTENT_TYPE_JPEG);
		i = addPrivateCMLDesc(key, mName, name, desc, args, r);
		//generic GetReading
		addGenericCMLDesc(CCD_KEY, GENERIC_GETREADING, i);
		
		
		
		mName = V4L2Protocol.START_STREAM_METHOD;
		name =  "StartStream";
		desc = "Starts a new JPEG stream";
		r = new ResponseType(CMLConstants.RET_TYPE_BYTE_ARRAY,CMLConstants.CONTENT_TYPE_JPEG);
		i = addPrivateCMLDesc(key, mName, name, desc, args, r);
		//generic startStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STARTSTREAM, i);

		
		
		mName = V4L2Protocol.STOP_STREAM_METHOD;
		name = "StopStream";
		desc = "Stops a JPEG stream";
		r = new ResponseType();
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic stopStream
		addGenericCMLDesc(CCD_KEY, GENERIC_STOPSTREAM, i);
		
		
		
		mName = V4L2Protocol.STOP_STREAM_FAKE_METHOD;
		name = "StopStreamFake";
		desc = "Stops a fake JPEG stream";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
				

		mName = V4L2Protocol.START_STREAM_FAKE_METHOD;
		name =  "StartStreamFake";
		desc = "Starts a new fake JPEG stream";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
	}
}
