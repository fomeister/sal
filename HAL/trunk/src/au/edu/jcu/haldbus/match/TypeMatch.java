package au.edu.jcu.haldbus.match;

import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

public class TypeMatch<T> extends AbstractMatch {
	private T propValue;
	public TypeMatch(String prop, T val) throws InvalidConstructorArgs{
		super(prop, "TypeMatch<"+val.getClass().getName()+">");
		propValue = val;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected String matchObject(Object o) throws MatchNotFoundException {
		if ( ((T) o).equals(propValue) )
			return propValue.toString();
		throw new MatchNotFoundException();	
	}

}
