package au.edu.jcu.haldbus.match;


/**
 * This class defines an HAL match object which will always match the value of a given property.
 * @author gilles
 * @see AbstractMatch
 *
 */
public class AlwaysMatch extends AbstractMatch {
	/**
	 * This methods build a match object that will always match the value of the property <code>prop</code>.
	 * @param prop the name of the property.
	 * @throws InvalidArgumentsException if the property name is null 
	 */	
	public AlwaysMatch(String prop){
		super(prop, "AlwaysMatch");
	}

	@Override
	public String matchObject(Object o) {
		return o.toString();
	}

}
