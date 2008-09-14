package au.edu.jcu.haldbus.match;

import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;

public class AlwaysMatch extends AbstractMatch {
	public AlwaysMatch(String prop, boolean negate) throws InvalidConstructorArgs{
		super(prop, "AlwaysMatch", negate);
	}
	
	public AlwaysMatch(String prop) throws InvalidConstructorArgs{
		super(prop, "AlwaysMatch");
	}

	@Override
	public String matchObject(Object o) {
		return o.toString();
	}

}
