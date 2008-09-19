package jcu.sal.components.protocols.simpleSNMP;

import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore{
	private static CMLDescriptionStore c;
	public static String SNMP_KEY="ALL";
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
	

	private CMLDescriptionStore() throws ConfigurationException, AlreadyPresentException, NotFoundException{
		int i;
		String key, name, mName, desc;
		List<String> argNames;
		List<ArgumentType> t;
		ReturnType r;
		
		/* 
		 * ALL
		 * */
		key = SNMP_KEY;
		mName = SSNMPProtocol.GET_READING_METHOD;
		name = "ReadValue";
		desc = "Reads the value of this sensor";
		t = new Vector<ArgumentType>();
		argNames = new Vector<String>();
		r = new ReturnType(CMLConstants.ARG_TYPE_STRING);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic GetReading command
		addGenericCMLDesc(SNMP_KEY, GENERIC_GETREADING, i);
		}
}
