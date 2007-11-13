/**
 * 
 */
package jcu.sal.Components.Identifiers;

/**
 * @author gilles
 *
 */
abstract class AbstractIdentifier implements Identifier {
	
	/** the name of the component */
	protected String name;
	
	/** the type of the component */
	protected String type;
	
	/**
	 * Construct a new Identifier with the specified name and type 
	 * @param name the name
	 * @param type the type
	 */
	public AbstractIdentifier(String name, String type) { 
		if(name.equals(ANY_NAME) && type.equals(ANY_TYPE)) {name = ""; type="";}
		this.name = name; this.type = type;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#getName()
	 */
	public final String getName() { return name; }
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#getType()
	 */
	public final String getType() {return type;}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#equals(java.lang.String)
	 */
	public boolean equals(Object id) {
		/* sanity check */
		if(id == null) return false;
		else if(id == this) return true;
		else {
			if(id.getClass().getSuperclass().getName().equals("jcu.sal.Components.Identifiers.AbstractIdentifier")) {
				Identifier i = (Identifier) id;
				if(type.equals(ANY_TYPE) || i.getType().equals(ANY_TYPE))
					return (i.getName().equals(name));
				else if(name.equals(ANY_NAME) || i.getName().equals(ANY_NAME))
					return (i.getType().equals(type));
				else
					return (id.toString().equals(this.toString()));
			}
			else return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#hashCode(java.lang.String)
	 */
	public int hashCode(){
		// TODO improve this !
		return 1;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#toString(java.lang.String)
	 */
	public String toString(){
		return name + "/" + type;
	}

}
