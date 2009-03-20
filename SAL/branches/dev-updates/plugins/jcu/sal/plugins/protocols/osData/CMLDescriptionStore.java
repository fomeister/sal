package jcu.sal.plugins.protocols.osData;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore{
	private static CMLDescriptionStore c; 
	static {
		try {
			c = new CMLDescriptionStore();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static CMLDescriptionStore getStore() {
		return c;
	}
	
	private CMLDescriptionStore() throws ConfigurationException, NotFoundException, AlreadyPresentException{
		super();
		int i;
		String key, name, mName, desc;
		ResponseType r;

		/* 
		 * FreeMem
		 * */
		key = OSDataConstants.FreeMem;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.FreeMem;
		desc = "Reads the amount of free memory";
		r = new ResponseType(CMLConstants.RET_TYPE_INT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_MBYTES);
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.FreeMem, GENERIC_GETREADING, i);
		
		/* 
		 * UserTime
		 * */
		key = OSDataConstants.UserTime;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.UserTime;
		desc = "Reads the percentage of time spent on user mode processes";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT,CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_PERCENT);
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.UserTime, GENERIC_GETREADING, i);

		/* 
		 * NiceTime
		 * */
		key = OSDataConstants.NiceTime;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.NiceTime;
		desc = "Reads the precentage of time spent on nice'd processes";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.NiceTime, GENERIC_GETREADING, i);

		/* 
		 * SystemTime
		 * */
		key = OSDataConstants.SystemTime;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.SystemTime;
		desc = "Reads the percentage of time spent in system mode";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.SystemTime, GENERIC_GETREADING, i);

		/* 
		 * IdleTime
		 * */
		key = OSDataConstants.IdleTime;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.SystemTime;
		desc = "Reads the percentage of time spent in idle mode";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.IdleTime, GENERIC_GETREADING, i);
		
		/* 
		 * LoadAvg1
		 * */
		key = OSDataConstants.LoadAvg1;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.LoadAvg1;
		desc = "Reads the 1-minute load average";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT);
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.LoadAvg1, GENERIC_GETREADING, i);

		/* 
		 * LoadAvg5
		 * */
		key = OSDataConstants.LoadAvg5;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.LoadAvg5;
		desc = "Reads the 5-minute load average";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.LoadAvg5, GENERIC_GETREADING, i);
		
		/* 
		 * LoadAvg15
		 * */
		key = OSDataConstants.LoadAvg15;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.LoadAvg15;
		desc = "Reads the 15-minute load average";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.LoadAvg15, GENERIC_GETREADING, i);

		/* 
		 * CPUTemp
		 * */
		key = OSDataConstants.Temp1;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.Temp1;
		desc = "Reads the CPU temperature";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_DEGREE_C);
		i = addPrivateCMLDesc(key, mName, name, desc,null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.Temp1, GENERIC_GETREADING, i);
		
		/* 
		 * NBTemp
		 * */
		key = OSDataConstants.Temp2;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.Temp2;
		desc = "Reads the north bridge temperature";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.Temp2, GENERIC_GETREADING, i);
	
		/* 
		 * SBTemp
		 * */
		key = OSDataConstants.Temp3;
		mName = OSDataProtocol.GET_READING_METHOD;
		name = "Get"+OSDataConstants.Temp3;
		desc = "Reads the south bridge temperature";
		i = addPrivateCMLDesc(key, mName, name, desc, null, r);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.Temp3, GENERIC_GETREADING, i);
	}
}
