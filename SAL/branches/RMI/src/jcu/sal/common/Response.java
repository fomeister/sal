package jcu.sal.common;

import java.io.Serializable;
import java.nio.channels.ClosedChannelException;

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
	
	/**
	 * This constructor builds a response object with either the errror or the end flag set. It is only intended to be used
	 * by streaming threads returning a response via a callback method 
	 * @param sid the sensor id
	 * @param e whether there was an error or not (normal end of stream)
	 */
	public Response(String sid, boolean e){
		this(null,sid);
		b=null;
		error = e;
	}
	
	public String getSID() {
		return sid;
	}
	
	public byte[] getBytes() throws ConfigurationException, ClosedChannelException {
		if(b==null && error)
			throw new ConfigurationException();
		else if(b==null && !error)
			throw new ClosedChannelException();
		return b;
	}
	
	public long getTimeStamp(){
		return timeStamp;
	}
}
