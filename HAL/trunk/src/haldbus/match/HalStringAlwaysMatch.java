package haldbus.match;

import javax.naming.ConfigurationException;

public class HalStringAlwaysMatch extends AbstractMatch {
	public HalStringAlwaysMatch(String prop, String name) throws ConfigurationException{
		super(prop, name);
	}

	@Override
	public String matchObject(Object o) {
		return o.toString();
	}

}
