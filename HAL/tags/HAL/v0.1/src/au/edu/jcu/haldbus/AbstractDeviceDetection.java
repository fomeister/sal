package au.edu.jcu.haldbus;

import java.util.Map;
import java.util.TreeMap;

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
	private int whenFlags;
	public static int INITIAL_RUN_FLAG = 1;
	public static int SUBSEQUENT_RUN_FLAG = 2;
	public static int ALWAYS_RUN_FLAG = (INITIAL_RUN_FLAG | SUBSEQUENT_RUN_FLAG);
	

	/**
	 * This method creates the AbstractDeviceDetection, initialises the map and set the execution flags
	 */
	protected AbstractDeviceDetection(int when){
		list = new TreeMap<String,HalMatchInterface>();
		whenFlags = when;
	}
	
	/**
	 * This method creates the AbstractDeviceDetection and initialises the map. The execution flag is set to ALWAYS
	 * (both in the initial and subsequent runs)
	 */
	protected AbstractDeviceDetection(){
		this(ALWAYS_RUN_FLAG); 
	}
	
	/**
	 * This method adds a new HalMatch object to the map. The ordering of HalMatch objects is important. Matches most likely
	 * to fail should be first, while those unlikely to fail should be last. Infulencing the ordering of an HalMatch object
	 * is done by adjusting the name associated with this HalMatch object. A naming convention such as the following should
	 * be applied: Start each name with a two-digit number, then a dash, followed by any name. That way changing the number
	 * will change the rank of a HalMatch object. 
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
	
	@Override
	public final int countMatches() {
		return list.size();
	}
	
	@Override
	public final boolean initialMatch(){
		return (whenFlags & INITIAL_RUN_FLAG)!=0;
	}
	
	@Override
	public final boolean subsequentMatch(){
		return (whenFlags & SUBSEQUENT_RUN_FLAG)!=0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((list == null) ? 0 : list.hashCode());
		result = PRIME * result + whenFlags;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AbstractDeviceDetection other = (AbstractDeviceDetection) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		if (whenFlags != other.whenFlags)
			return false;
		return true;
	}
}
