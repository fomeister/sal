package jcu.sal.common;

import javax.naming.ConfigurationException;

public class ResponseParser {
	public static int getLength(Response r) throws ConfigurationException{
		return r.getBytes().length;
	}
	
	public static String toString(Response r) throws ConfigurationException {
		return new String(r.getBytes());
	}
	
	public static int toInt(Response r) throws NumberFormatException, ConfigurationException {
		return Integer.parseInt(toString(r));
	}
	
	public static float toFloat(Response r) throws ConfigurationException {
		return Float.parseFloat(toString(r));
	}
	
	public static byte[] toByteArray(Response r) throws ConfigurationException {
		return r.getBytes();
	}
}
