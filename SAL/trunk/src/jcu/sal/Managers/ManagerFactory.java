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
	 * returns the type of a component from its DOM document
	 * @param doc the DOM document
	 * @return the type of the component
	 */
	protected abstract String getComponentType(Document doc) throws ParseException;
	
	/**
	 * returns the name of a component from its DOM document
	 * @param doc the DOM document
	 * @return the ID of the component
	 */
	protected abstract Identifier getComponentID(Document doc) throws ParseException;
	
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
		String type;
		Identifier id;
		try {
			type = getComponentType(doc);
			id = getComponentID(doc);
			this.logger.debug("About to create a component of type " + type + " named " + id.getName());
			if(!ctable.containsKey(id)) newc = build(doc);
			else this.logger.error("Couldnt create component "+type+", it already exist");
			
			if(newc!=null) ctable.put(id, newc);
			else this.logger.error("Couldnt create component "+type);
		} catch (Exception e) {
			this.logger.error("Couldnt create component from XML doc");
			e.printStackTrace();
		}

		return newc; 
	}
	
	/** 
	 * Removes a previoulsy creatd component
	 * @param type the component type
	 */
	public void destroyComponent(Identifier i) {
		if(ctable.containsKey(i)) {
			remove(ctable.get(i));
			dumpTable();
			if(ctable.remove(i) == null)
				this.logger.debug("Cant remove element with key " + i.toString() +  ": No such element");
			else
				this.logger.debug("Element " + i.toString()+ " Removed");
		} else
			this.logger.error("Element " + i.toString()+ " doesnt exist and can NOT be removed");
	}
	
	/** 
	 * Removes a previoulsy creatd component
	 * @param component the component to be removed
	 */
/*	public void destroyComponent(T component) {
		if(ctable.containsValue(component))
		remove(component);
		if(ctable.remove(component.) == null)
			this.logger.debug("Cant remove element with key " + type +  ": No such element");
		else
			this.logger.debug("Element " + type + " Removed");
*/	
	
	private void dumpTable() {
		Enumeration<Identifier> keys = ctable.keys();
		while ( keys.hasMoreElements() )
		   this.logger.debug("key: " + keys.nextElement().toString());
	}

}
