package au.edu.jcu.haldbus.match;

import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

public class GenericMatch<T> extends AbstractMatch {
	private T propValue;
	private boolean subString, noCase;
	
	/**
	 * This constructor builds a HAL match object against a property of type T. If T is a String, the comparisson will be
	 * case sensitive and against the entire string by default, unless subsequent calls to <code>setMatchSubString()</code>
	 * and/or <code>setMatchNoCase()</code>.
	 * @param prop the property name 
	 * @param val the expected value
	 * @throws InvalidConstructorArgs if either of the arguments are invalid (for example, an empty property name) 
	 */
	public GenericMatch(String prop, T val, boolean negate) throws InvalidConstructorArgs{
		super(prop, "GenericMatch<"+val.getClass().getName()+">", negate);
		propValue = val;
		subString = false;
		noCase = false;
	}
	public GenericMatch(String prop, T val) throws InvalidConstructorArgs{
		super(prop, "GenericMatch<"+val.getClass().getName()+">");
		propValue = val;
		subString = false;
		noCase = false;
	}
	
	/**
	 * This constructor builds a HAL match object against a property of type String. the comparisson can be made
	 * case insensitive and against a substring of the property by setting <code>substring</code> and <code>nocase</code. to true.
	 * @param prop the property name 
	 * @param val the expected value
	 * @param substring if set, match if <code>val</code> is a substring tof the property value
	 * @param nocase if set, comparisson is case insensitive
	 * @param negate whether to negate the match, ie if the value is found return MatchNotFound and vice-versa.
	 * @throws InvalidConstructorArgs if one of the arguments is invalid (for example, val is not a string) 
	 */
	public GenericMatch(String prop, T val, boolean substring, boolean nocase, boolean negate) throws InvalidConstructorArgs{
		super(prop, "GenericMatch<"+val.getClass().getName()+">", negate);
		if(!(val instanceof String))
			throw new InvalidConstructorArgs();

		propValue = val;
		subString = substring;
		noCase = nocase;
	}
	
	public GenericMatch(String prop, T val, boolean substring, boolean nocase) throws InvalidConstructorArgs{
		super(prop, "GenericMatch<"+val.getClass().getName()+">");
		if(!(val instanceof String))
			throw new InvalidConstructorArgs();

		propValue = val;
		subString = substring;
		noCase = nocase;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected String matchObject(Object o) throws MatchNotFoundException {
		if(!(propValue instanceof String)) {
			if ( ((T) o).equals(propValue) )
				return propValue.toString();
		} else if(!subString && !noCase){
			if ( ((String) o).equals(propValue) )
				return propValue.toString();
		} else if(!subString && noCase){
			if ( ((String) o).equalsIgnoreCase((String) propValue) )
				return propValue.toString();
		} else if(subString && !noCase){
			if ( ((String) o).indexOf((String) propValue)!=-1 )
				return propValue.toString();
		} else {
			if ( ((String) o).toLowerCase().indexOf(((String) propValue).toLowerCase())!=-1 )
				return propValue.toString();
		}
		throw new MatchNotFoundException();	
	}

}
