package jcu.sal.common;

import java.nio.channels.ClosedChannelException;

import javax.naming.ConfigurationException;

public class ResponseParser {
	public static int getLength(Response r) throws ConfigurationException, ClosedChannelException{
		return r.getBytes().length;
	}
	
	public static String toString(Response r) throws ConfigurationException, ClosedChannelException {
		return new String(r.getBytes());
	}
	
	public static int toInt(Response r) throws ConfigurationException, ClosedChannelException {
		return Integer.parseInt(toString(r));
	}
	
	public static float toFloat(Response r) throws ConfigurationException, ClosedChannelException {
		return Float.parseFloat(toString(r));
	}
	
	public static byte[] toByteArray(Response r) throws ConfigurationException, ClosedChannelException {
		return r.getBytes();
	}
}
