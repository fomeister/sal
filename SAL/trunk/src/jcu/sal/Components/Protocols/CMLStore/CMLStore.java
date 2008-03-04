package jcu.sal.Components.Protocols.CMLStore;

/**
 * Defines the interface of CMLStores objects
 * Each CML store (class which stores the CML docs for sensors of a protocol) must
 * implement this interface
 * @author gilles
 *
 */

public interface CMLStore {
	/**
	 * Retrieves the CML document for the given key f (native address, sensor family, ...)
	 * @param f the key
	 * @return the CML doc or null if the key can not be found
	 */
	public String getCML(String f);
}
