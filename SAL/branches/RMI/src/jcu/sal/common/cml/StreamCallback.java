package jcu.sal.common.cml;

import java.io.IOException;

import jcu.sal.common.Response;

/**
 * Object implementing this interface can be used as callback objects to collect streaming data from a sensor which supports it. 
 * @author gilles
 *
 */
public interface StreamCallback {
	/**
	 * This method is called by a Protocol when streaming data from a sensor is available. 
	 * @param r the streaming data to be collected
	 * @throws IOException if the callback object wont accept calls anymore and the stream must stop now. This exception is thrown by the RMI SAL client stub
	 * (on the SAL agent side) to tell the agent to stop streaming data, as the callback object is not available anymore to collect data. 
	 */
	public void collect(Response r) throws IOException;
}
