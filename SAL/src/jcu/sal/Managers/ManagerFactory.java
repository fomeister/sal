/**
 * 
 */
package jcu.sal.Managers;

import java.text.ParseException;
import java.util.Hashtable;

import jcu.sal.Components.Identifiers.Identifier;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * Creates manager classes, which create, delete and manage components (Endpoints, Protocol, ...)
 * @author gilles
 *
 */
public abstract class ManagerFactory<T> {
	
	static Logger logger = Logger.getLogger(ManagerFactory.class);
	private Hashtable<Identifier, T> ctable;
	
	public ManagerFactory() {
		ctable = new Hashtable<Identifier, T>();
	}
	/**
	 * Creates the component from an DOM document
	 * @param doc the DOM document
	 * @return the component
	 */
	protected abstract T build(Document doc);
	
	/**
	 * Deletes the component and give the subclass a chance to turn things off properly
	 * @param component the component
	 */
	protected abstract void remove(T component);
	
	/**
	 * returns the name of a component from its DOM document
	 * @param doc the DOM document
	 * @return the name of the component
	 */
	protected abstract Identifier componentIdentifier(Document doc) throws ParseException;
	
	/**
	 * Create a component
	 * @param doc
	 * @return
	 */
	public T create(Document doc) {
		T newc = null;
		Identifier id;
		try {
			id = componentIdentifier(doc);
			if(!ctable.containsKey(id)) {
				newc = build(doc);
				ctable.put(id, newc);
			}
		} catch (ParseException e1) {
			logger.error("Couldnt create component from document: " + doc);
			e1.printStackTrace();
		}

		return newc; 
	}
	
	/** 
	 * Removes a previoulsy creatd component
	 * @param id the component identifier
	 */
	public void destroy(Identifier id) {
		remove(ctable.get(id));
		if(ctable.remove(id) == null)
			System.out.println("Removing element with key " + id.toString() +  ": No such element");
	}

}
