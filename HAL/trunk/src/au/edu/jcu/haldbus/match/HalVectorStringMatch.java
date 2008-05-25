package au.edu.jcu.haldbus.match;


import java.util.Vector;

import javax.naming.ConfigurationException;

public class HalVectorStringMatch extends AbstractMatch {
	private String propValue;
	
	public HalVectorStringMatch(String prop, String val, String name) throws ConfigurationException{
		super(prop, name);
		propValue = val;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String matchObject(Object o) throws ConfigurationException {
		Vector<String> l = (Vector<String>) o;
		if(l.contains(propValue))
			return propValue;
		throw new ConfigurationException();
	}

}
