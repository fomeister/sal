/**
 * 
 */
package jcu.sal.Components.Identifiers;

/**
 * this class represents Endpoint names
 * @author gilles
 *
 */
public class ProtocolID extends AbstractIdentifier{

	/**
	 * Creates a new ProtocolID with an initial name
	 * @param id
	 */
	public ProtocolID(String id, String type) {
		super(id, type);
	}
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#toString(java.lang.String)
	 */
	public String toString() {
		return name;
	}
}
