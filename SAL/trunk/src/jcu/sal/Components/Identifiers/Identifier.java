package jcu.sal.Components.Identifiers;

public interface Identifier {
	
	/**
	 * Returns the name of a component from its identifier
	 * @return a string representation of the identifier
	 */
	public String getName();
	
	/**
	 * Test whether two identifiers are the same
	 * @param id the identifier to be tested
	 * @return ture or false
	 */
	public boolean equals(Object id);
	
	/**
	 * Returns a hash value of this object
	 * @return the has code
	 */
	public int hashCode();
	
	/**
	 * returns a string representation of the identifier
	 * @return the string representation of the identifier
	 */
	public String toString();
}
