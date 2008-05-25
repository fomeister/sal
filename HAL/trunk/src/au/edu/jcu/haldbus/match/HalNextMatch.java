package au.edu.jcu.haldbus.match;


import javax.naming.ConfigurationException;

public class HalNextMatch extends AbstractMatch {

	public HalNextMatch(String obj, HalMatchInterface n, String name)
			throws ConfigurationException {
		super(obj, n, name);
	}

	@Override
	public String matchObject(Object o) throws ConfigurationException {
		 throw new ConfigurationException();
	}

}
