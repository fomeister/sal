package jcu.sal.config.deviceDetection;


/**
 * This interface is implemented by all classes used to probe hardware devices. This interface
 * specifies only two methods (start() and stop()). All hardware probes are implemented as threads
 * waiting for specific events (new device connected (HAL), new file appeared somewhere in filesystem 
 * (inotify) ). This interface makes it easier to start and stop these threads. 
 * @author gilles
 *
 */
public interface HwProbeInterface {

	/**
	 * This method starts the threads
	 * @throws Exception if the thread cant be started
	 */
	public void start() throws Exception;

	/**
	 * This method stops the threads and returned when all have exited 
	 */
	public void stop();

}