package jcu.sal.Components.EndPoints;

/**
 * Classes implementing this interface will have their deviceChange() method called whenever the devices they
 * registered for are plugged in or unplugged.
 * @author gilles
 *
 */
public interface DeviceListener {
	/**
	 * this method is called whenever watched-devices are unplugged / plugged
	 * @param n the number of watched-devices currently plugged in
	 */
	public void adapterChange(int n);

}
