package au.edu.jcu.haldbus.match;

import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.exceptions.InvalidMethodCall;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

public abstract class AbstractMatch implements HalMatchInterface {
	public final static String THIS_OBJECT = "#ShouldbeFine@@#";
	private String name;
	private String object;
	private String propName;
	private HalMatchInterface nextMatch;

	private AbstractMatch(String obj, String pName, HalMatchInterface n, String name) throws InvalidConstructorArgs {
		if(name==null)
			throw new InvalidConstructorArgs();
		this.name = name;
		if (obj != null && n != null) {
			// check that the object name is valid
			char c = obj.charAt(0);
			if (c == THIS_OBJECT.charAt(0) || c == '/' || c == '@') {
				object = obj;
				propName = null;
				nextMatch = n;
			} else {
				throw new InvalidConstructorArgs();
			}
		} else if (pName != null) {
			object = THIS_OBJECT;
			propName = pName;
			nextMatch = null;
		} else {
			throw new InvalidConstructorArgs();
		}
	}

	protected AbstractMatch(String obj, HalMatchInterface n, String name) throws InvalidConstructorArgs {
		this(obj, null, n, name);
	}

	protected AbstractMatch(String prop, String name) throws InvalidConstructorArgs {
		this(null, prop, null, name);
	}

	public String match(Object o) throws MatchNotFoundException {
		if (o == null)
			throw new MatchNotFoundException();

		return matchObject(o);
	}

	protected abstract String matchObject(Object o)	throws MatchNotFoundException;


	public boolean matchThisObject() {
		return object.equals(THIS_OBJECT);
	}

	public boolean matchNextObjectLink() {
		return object.charAt(0) == '@';
	}

	public boolean matchNextObjectValue() {
		return object.charAt(0) == '/';
	}
	
	public HalMatchInterface getNextMatch() throws  InvalidMethodCall{
		if(matchNextObjectLink() || matchNextObjectValue())
			return nextMatch;
		throw new InvalidMethodCall();
	}

	public String getNextObjectLink() throws InvalidMethodCall {
		if (matchNextObjectLink())
			return object.substring(1);
		throw new InvalidMethodCall();
	}

	public String getNextObjectValue() throws InvalidMethodCall {
		if (matchNextObjectValue())
			return object;
		throw new InvalidMethodCall();
	}

	public String getPropName() throws InvalidMethodCall {
		if (matchThisObject())
			return propName;
		throw new InvalidMethodCall();
	}

	public String getName() {
		return name;
	}
}
