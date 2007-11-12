/**
 * 
 */
package jcu.sal.Components.Identifiers;

/**
 * @author gilles
 *
 */
abstract class AbstractIdentifier implements Identifier {
	
	/* the name of the component */
	protected String name;
	
	/* the type of the component */
	protected String type;
	
	public AbstractIdentifier(String name, String type) { this.name = name; this.type = type; }

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#getName()
	 */
	public final String getName() { return name; }
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#getType()
	 */
	public final String getType() {return type;}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#setName(java.lang.String)
	 */
	public final void setName(String name) { this.name = name; }
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#equals(java.lang.String)
	 */
	public boolean equals(Object id) {
		return (id.toString().equals(this.toString()));
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#hashCode(java.lang.String)
	 */
	public int hashCode(){
		return name.hashCode()+type.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#toString(java.lang.String)
	 */
	public String toString(){
		return name + "/" + type;
	}

}
