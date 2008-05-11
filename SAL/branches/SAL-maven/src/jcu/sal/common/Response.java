package jcu.sal.common;

public class Response {
	private byte[] b;
	public Response(byte[] bb) {
		b = bb;
	}
	byte[] getBytes() {return b;}
}
