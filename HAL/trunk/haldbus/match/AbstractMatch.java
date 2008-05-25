package haldbus.match;

import javax.naming.ConfigurationException;

public abstract class AbstractMatch implements HalMatchInterface {
	public final static String THIS_OBJECT = "#ShouldbeFine@@#";
	private String name;
	private String object;
	private String propName;
	private HalMatchInterface nextMatch;

	private AbstractMatch(String obj, String pName, HalMatchInterface n,
			String name) throws ConfigurationException {
		if (name == null)
			throw new ConfigurationException();
		this.name = name;
		if (obj != null && n != null) {
			// check that the object name is valid
			char c = obj.charAt(0);
			if (c == THIS_OBJECT.charAt(0) || c == '/' || c == '@') {
				object = obj;
				propName = null;
				nextMatch = n;
			} else {
				throw new ConfigurationException();
			}
		} else if (pName != null) {
			object = THIS_OBJECT;
			propName = pName;
			nextMatch = null;
		} else {
			throw new ConfigurationException();
		}
	}

	protected AbstractMatch(String obj, HalMatchInterface n, String name)
			throws ConfigurationException {
		this(obj, null, n, name);
	}

	protected AbstractMatch(String prop, String name)
			throws ConfigurationException {
		this(null, prop, null, name);
	}

	public String match(Object o) throws ConfigurationException {
		if (o == null)
			throw new ConfigurationException();

		return matchObject(o);
	}

	protected abstract String matchObject(Object o)
			throws ConfigurationException;


	public boolean matchThisObject() {
		return object.equals(THIS_OBJECT);
	}

	public boolean matchNextObjectLink() {
		return object.charAt(0) == '@';
	}

	public boolean matchNextObjectValue() {
		return object.charAt(0) == '/';
	}
	
	public HalMatchInterface getNextMatch() throws  ConfigurationException{
		if(matchNextObjectLink() || matchNextObjectValue())
			return nextMatch;
		throw new ConfigurationException();
	}

	public String getNextObjectLink() throws ConfigurationException {
		if (matchNextObjectLink())
			return object.substring(1);
		throw new ConfigurationException();
	}

	public String getNextObjectValue() throws ConfigurationException {
		if (matchNextObjectValue())
			return object;
		throw new ConfigurationException();
	}

	public String getPropName() throws ConfigurationException {
		if (matchThisObject())
			return propName;
		throw new ConfigurationException();
	}

	public String getName() {
		return name;
	}
}
