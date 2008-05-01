package jcu.sal.components.protocols.snmp;

import javax.naming.ConfigurationException;

import jcu.sal.components.protocols.CMLStore;


public class SimpleSNMPCML extends CMLStore{
	private static SimpleSNMPCML c;
	public static String SNMP_KEY="ALL";
	static {
		try {
			c = new SimpleSNMPCML();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} 
	}
	
	public static SimpleSNMPCML getStore() {
		return c;
	}
	

	private SimpleSNMPCML() throws ConfigurationException{
		int i;
		/* 
		 * ALL
		 * */
		i = addPrivateCMLDesc(SNMP_KEY, SimpleSNMPProtocol.GET_READING_METHOD, "ReadValue", "Reads the value of this sensor", new String[0], new String[0]);
		//generic GetReading command
		addGenericCMLDesc(SNMP_KEY, GENERIC_GETREADING, new Integer(i));
		}
}
