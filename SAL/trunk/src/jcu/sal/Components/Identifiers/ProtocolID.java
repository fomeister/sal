/**
 * 
 */
package jcu.sal.Components.Identifiers;

/**
 * this class represents Endpoint names
 * @author gilles
 *
 */
public class ProtocolID implements Identifier{
	/* the name of the protocol */
	private String name;
	
	/**
	 * Creates a new ProtocolID with an initial name
	 * @param id
	 */
	public ProtocolID(String id) {
		name = id;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#equals(jcu.sal.Components.Identifiers.Identifier)
	 */
	public boolean equals(Object id) {
		return id.toString().equals(this.name);
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#hashCode(jcu.sal.Components.Identifiers.Identifier)
	 */
	public int hashCode() {
		return name.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#toString(java.lang.String)
	 */
	public String toString() {
		return name;
	}

	public void setName(String name) {
		this.name = name;		
	}

}
