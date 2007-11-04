/**
 * 
 */
package jcu.sal.Managers;

import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;

import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * Creates manager classes, which create, delete and manage components (Endpoints, Protocol, ...)
 * @author gilles
 *
 */
public abstract class ManagerFactory<T> {
	
	private Logger logger = Logger.getLogger(ManagerFactory.class);
	private Hashtable<Identifier, T> ctable;
	
	public ManagerFactory() {
		Slog.setupLogger(this.logger);
		ctable = new Hashtable<Identifier, T>();
	}
	/**
	 * Creates the component from a DOM document
	 * @param doc the DOM document
	 * @return the component
	 * @throws InstantiationException 
	 */
	protected abstract T build(Document doc) throws InstantiationException;
	
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
	protected abstract Identifier getComponentIdentifier(Document doc) throws ParseException;
	
	/**
	 * returns the configuration directives for this component
	 * @param doc the DOM document
	 * @return the config directives in a hastable
	 */
	protected abstract Hashtable<String,String> getComponentConfig(Document doc) throws ParseException;
	
	/**
	 * Create a new instance of a fully configured component from its DOM document
	 * @param doc
	 * @return
	 */
	public T createComponent(Document doc) {
		T newc = null;
		Identifier id;
		try {
			id = getComponentIdentifier(doc);
			if(!ctable.containsKey(id)) {
				newc = build(doc);
				ctable.put(id, newc);
			} else 
				this.logger.error("Couldnt create component "+id.getName()+", already exist");
		} catch (Exception e) {
			this.logger.error("Couldnt create component from document: " + doc);
			e.printStackTrace();
		}

		return newc; 
	}
	
	/** 
	 * Removes a previoulsy creatd component
	 * @param id the component identifier
	 */
	public void destroyComponent(Identifier id) {
		remove(ctable.get(id));
		dumpTable();
		if(ctable.remove(id) == null)
			this.logger.debug("Cant remove element with key " + id.toString() +  ": No such element");
		else
			this.logger.debug("Element " + id.toString() + " Removed");
	}
	
	private void dumpTable() {
		Enumeration<Identifier> keys = ctable.keys();
		while ( keys.hasMoreElements() )
		   this.logger.debug("key: " + keys.nextElement().toString());
	}

}
