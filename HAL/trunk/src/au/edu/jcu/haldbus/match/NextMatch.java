package au.edu.jcu.haldbus.match;


import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

public class NextMatch extends AbstractMatch {

	public NextMatch(String obj, HalMatchInterface n)
			throws InvalidConstructorArgs {
		super(obj, n, "NextMatch");
	}

	@Override
	public String matchObject(Object o) throws MatchNotFoundException {
		 throw new MatchNotFoundException();
	}

}
