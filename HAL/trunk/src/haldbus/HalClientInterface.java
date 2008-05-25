package haldbus;

import haldbus.match.HalMatchInterface;

import java.util.List;
import java.util.Map;

/**
 * This interface must be implemented by all Hal Autodetection clients. The use case goes as follows:
 * <li>
 * <ul>The HAL AD helper queries the client (<code>getMatchList()</code>)to get a list of HAL match
 * objects. Each object specifies what HAL property is to be checked and the expected values.</ul>
 * <ul>The HAL AD helper then matches the properties of newly discovered devices against each HAL 
 * match object (returned by <code>getMatchList()</code>). For each correct match, a resulting string
 * is mapped to the name of the HAL match, and stored in a map object</ul>
 * <ul>After checking all the HAL match objects against the newly added HAL device, if there are 
 * at least <code>getMinMatches()</code>, <code>doAction(Map)</code> is invoked with the previously
 * created map object given a an argument. 
 * </li>
 * @author gilles
 *
 */
public interface HalClientInterface {
	/**
	 * This method returns a list of HalMatchInterfaces associated with this client
	 * @return a list of HalMatchInterfaces associated with this client
	 */
	public List<HalMatchInterface> getMatchList();
	
	/**
	 * This method must be executed if there is at least <code>getMinMatches()</code> and at most
	 * <code>getMaxMatches()</code> correct matches.
	 * @param l a map of matched strings identified by the HALMatchInterface's name
	 */
	public void doAction(Map<String,String> l);
	
	/**
	 * This method returns the name of this client
	 * @return <code>getMinMatches()</code>  
	 */
	public String getName();
	
	/**
	 * This method returns the minimum number of matches required
	 * @return the minimum number of matches required  
	 */
	public int getMinMatches();
	
	/**
	 * This method returns the maximum number of matches required
	 * @return the minimum number of matches required  
	 */
	public int getMaxMatches();
}
