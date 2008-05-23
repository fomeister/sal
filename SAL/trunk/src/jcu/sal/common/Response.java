package jcu.sal.common;

public class Response {
	private byte[] b;
	public Response(byte[] bb) {
		if(bb==null)
			b= new byte[0];
		else
			b = bb;
	}
	byte[] getBytes() {return b;}
}
