package au.edu.jcu.haldbus;

import java.util.Hashtable;
import java.util.Map;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.match.HalMatchInterface;

/**
 * This class implements the basic features found in all HAL clients interested in receiving specific
 * DeviceAdded notifications, namely, the management of the Match list. It also provides generic
 * <code>getMinMatches()</code> and <code>getMaxMatches()</code>, which simply return the number of
 * elements in the match list. These can be overriden by subclasses.
 * @author gilles
 *
 */
public abstract class AbstractDeviceDetection implements HalFilterInterface {
	private Map<String, HalMatchInterface> list;
	
	/**
	 * This method creates the AbstractClient and initialises the map
	 */
	protected AbstractDeviceDetection(){
		list = new Hashtable<String,HalMatchInterface>();
	}
	
	/**
	 * This method adds a new HalMatch object to the map.
	 * @param name the name associated with the HalMatch
	 * @param m the HalMatch object
	 * @throws AddRemoveElemException if the given name is already associated with a match 
	 */
	protected final void addMatch(String name, HalMatchInterface m) throws AddRemoveElemException{
		if(list.containsKey(name))
			throw new AddRemoveElemException();
		list.put(name, m);
	}

	/**
	 * This method removes a HalMatch object from the map.
	 * @param name the name associated with the HalMatch to be removed
	 * @throws AddRemoveElemException if the given name is doesnt exist 
	 */
	protected final void removeMatch(String name) throws AddRemoveElemException{
		if(!list.containsKey(name))
			throw new AddRemoveElemException();
		list.remove(name);
	}	

	@Override
	public final Map<String, HalMatchInterface> getMatchList() {
		return list;
	}	

	@Override
	public int getMaxMatches() {
		return list.size();
	}

	@Override
	public int getMinMatches() {
		return list.size();
	}

}
