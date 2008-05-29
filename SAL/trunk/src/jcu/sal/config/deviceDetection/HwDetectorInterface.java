package jcu.sal.config.deviceDetection;


public interface HwDetectorInterface {

	/* (non-Javadoc)
	 * @see jcu.sal.config.deviceDetection.DeviceDetection#start()
	 */
	public abstract void start() throws Exception;

	/* (non-Javadoc)
	 * @see jcu.sal.config.deviceDetection.DeviceDetection#stop()
	 */
	public abstract void stop();

}