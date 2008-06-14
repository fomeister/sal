package jcu.sal.common;

import java.io.Serializable;

import javax.naming.ConfigurationException;

public class Response implements Serializable {
	private static final long serialVersionUID = -4090794353146528167L;
	private byte[] b;
	private String sid;
	private boolean error;
	private long timeStamp;
	
	public Response(byte[] bb, String sid) {
		timeStamp = System.currentTimeMillis();
		this.sid = sid;
		error = false;
		if(bb==null)
			b= new byte[0];
		else
			b = bb;
	}
	
	public Response(String sid){
		this(null,sid);
		error = true;
	}
	
	public String getSID() {
		return sid;
	}
	
	public byte[] getBytes() throws ConfigurationException {
		if(error)
			throw new ConfigurationException();
		return b;
	}
	
	public long getTimeStamp(){
		return timeStamp;
	}
}
