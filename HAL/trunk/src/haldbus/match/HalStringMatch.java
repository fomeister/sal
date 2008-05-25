package haldbus.match;


import javax.naming.ConfigurationException;

public class HalStringMatch extends AbstractMatch {
	private String propValue;
	
	public HalStringMatch(String prop, String val, String name) throws ConfigurationException{
		super(prop, name);
		propValue = val;
	}

	@Override
	public String matchObject(Object o) throws ConfigurationException {
		if(((String) o).equals(propValue))
			return propValue;
		 throw new ConfigurationException();
	}

}
