package au.edu.jcu.haldbus.match;


import java.util.Vector;

import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

public class VectorMatch<T> extends AbstractMatch {
	private T propValue;
	
	public VectorMatch(String prop, T val) throws InvalidConstructorArgs{
		super(prop, "VectorMatch<"+val.getClass().getName()+">");
		propValue = val;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String matchObject(Object o) throws MatchNotFoundException {
		Vector<T> l = (Vector<T>) o;
		if(l.contains(propValue))
			return propValue.toString();
		throw new MatchNotFoundException();
	}

}
