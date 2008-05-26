package au.edu.jcu.haldbus.match;


import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

public class IntMatch extends AbstractMatch {
	private static String NAME="IntMatch";
	private int valMin;
	private int valMax;
	
	public IntMatch(String prop, int val) throws InvalidConstructorArgs{
		super(prop, NAME);
		valMax = valMin = val;	
	}
	
	public IntMatch(String prop, int min, int max) throws InvalidConstructorArgs{
		super(prop, NAME);
		if(max<min)
			throw new InvalidConstructorArgs();
		valMax = max; 
		valMin = min;		
	}

	@Override
	public String matchObject(Object o) throws MatchNotFoundException {
		int v = ((Integer) o).intValue();
		if(valMin<=v && v<=valMax)
			return String.valueOf(v);
		
		throw new MatchNotFoundException();
	}

}
