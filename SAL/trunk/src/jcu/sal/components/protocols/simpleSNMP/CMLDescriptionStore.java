package jcu.sal.components.protocols.simpleSNMP;

import javax.naming.ConfigurationException;

import jcu.sal.common.CMLConstants;
import jcu.sal.components.protocols.AbstractStore;
import jcu.sal.components.protocols.CMLDescription.ArgTypes;
import jcu.sal.components.protocols.CMLDescription.ReturnType;


public class CMLDescriptionStore extends AbstractStore{
	private static CMLDescriptionStore c;
	public static String SNMP_KEY="ALL";
	static {
		try {
			c = new CMLDescriptionStore();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} 
	}
	
	public static CMLDescriptionStore getStore() {
		return c;
	}
	

	private CMLDescriptionStore() throws ConfigurationException{
		int i;
		String key, name, mName, desc;
		String[] argNames;
		ArgTypes[] t;
		ReturnType r;
		
		/* 
		 * ALL
		 * */
		key = SNMP_KEY;
		mName = SSNMPProtocol.GET_READING_METHOD;
		name = "ReadValue";
		desc = "Reads the value of this sensor";
		t = new ArgTypes[0];
		argNames = new String[0];
		r = new ReturnType(CMLConstants.ARG_TYPE_STRING);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic GetReading command
		addGenericCMLDesc(SNMP_KEY, GENERIC_GETREADING, i);
		}
}
