package jcu.sal.components.protocols.osData;

import javax.naming.ConfigurationException;

import jcu.sal.components.protocols.CMLStore;


public class OSDataCML extends CMLStore{
	private static OSDataCML c; 
	static {
		try {
			c = new OSDataCML();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} 
	}
	
	public static OSDataCML getStore() {
		return c;
	}
	
	private OSDataCML() throws ConfigurationException{
		super();
		int i;

		/* 
		 * FreeMem
		 * */
		i = addPrivateCMLDesc(OSDataConstants.FreeMem, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.FreeMem, "Reads the amount of free memory", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.FreeMem, GENERIC_GETREADING, new Integer(i));
		
		/* 
		 * UserTime
		 * */
		i = addPrivateCMLDesc(OSDataConstants.UserTime, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.UserTime, "Reads the amount of time spent on user mode processes", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.UserTime, GENERIC_GETREADING, new Integer(i));

		/* 
		 * NiceTime
		 * */
		i = addPrivateCMLDesc(OSDataConstants.NiceTime, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.NiceTime, "Reads the amount of time spent on nice'd processes", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.NiceTime, GENERIC_GETREADING, new Integer(i));

		/* 
		 * SystemTime
		 * */
		i = addPrivateCMLDesc(OSDataConstants.SystemTime, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.SystemTime, "Reads the amount of time spent in system mode", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.SystemTime, GENERIC_GETREADING, new Integer(i));

		/* 
		 * IdleTime
		 * */
		i = addPrivateCMLDesc(OSDataConstants.IdleTime, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.SystemTime, "Reads the amount of time spent in idle mode", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.IdleTime, GENERIC_GETREADING, new Integer(i));
		
		/* 
		 * LoadAvg1
		 * */
		i = addPrivateCMLDesc(OSDataConstants.LoadAvg1, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.LoadAvg1, "Reads the 1-minute load average", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.LoadAvg1, GENERIC_GETREADING, new Integer(i));

		/* 
		 * LoadAvg5
		 * */
		i = addPrivateCMLDesc(OSDataConstants.LoadAvg5, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.LoadAvg5, "Reads the 5-minute load average", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.LoadAvg5, GENERIC_GETREADING, new Integer(i));
		
		/* 
		 * LoadAvg15
		 * */
		i = addPrivateCMLDesc(OSDataConstants.LoadAvg15, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.LoadAvg15, "Reads the 15-minute load average", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.LoadAvg15, GENERIC_GETREADING, new Integer(i));

		/* 
		 * CPUTemp
		 * */
		i = addPrivateCMLDesc(OSDataConstants.CPUTemp, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.CPUTemp, "Reads the CPU temperature", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.CPUTemp, GENERIC_GETREADING, new Integer(i));
		
		/* 
		 * NBTemp
		 * */
		i = addPrivateCMLDesc(OSDataConstants.NBTemp, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.NBTemp, "Reads the north bridge temperature", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.NBTemp, GENERIC_GETREADING, new Integer(i));
	
		/* 
		 * SBTemp
		 * */
		i = addPrivateCMLDesc(OSDataConstants.SBTemp, OSDataProtocol.GET_READING_METHOD, "Get"+OSDataConstants.SBTemp, "Reads the south bridge temperature", new String[0], new String[0]);
		//generic 100 GetReading command
		addGenericCMLDesc(OSDataConstants.SBTemp, GENERIC_GETREADING, new Integer(i));
	}
}
