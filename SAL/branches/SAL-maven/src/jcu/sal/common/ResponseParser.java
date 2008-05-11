package jcu.sal.common;

public class ResponseParser {
	public static int getLength(Response r){
		return r.getBytes().length;
	}
	
	public static String toString(Response r) {
		return new String(r.getBytes());
	}
	
	public static int toInt(Response r) {
		return Integer.parseInt(toString(r));
	}
	
	public static byte[] toByteArray(Response r) {
		return r.getBytes();
	}
}
