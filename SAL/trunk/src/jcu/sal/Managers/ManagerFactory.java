/**
 * 
 */
package jcu.sal.Managers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.ConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.HWComponent;
import jcu.sal.Components.Identifier;
import jcu.sal.Components.componentRemovalListener;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Creates manager classes, which create, delete and manage components (Endpoints, Protocol, ...)
 * @author gilles
 *
 */
public abstract class ManagerFactory<T extends HWComponent> implements componentRemovalListener {
	
	public static String COMPONENTPARAM_TAG = "Param";
	
	private Logger logger = Logger.getLogger(ManagerFactory.class);
	private Hashtable<Identifier, T> ctable;
	
	public ManagerFactory() {
		Slog.setupLogger(this.logger);
		ctable = new Hashtable<Identifier, T>();
	}

	/**
	 * returns the configuration directives for this component
	 * @param n the DOM document
	 * @return the config directives in a hastable
	 */
	protected Hashtable<String, String> getComponentConfig(Node n){
		ArrayList<String> xml = null;
		Hashtable<String, String> config = new Hashtable<String, String>();
		String name = null, value = null;
		
		try {
			xml = XMLhelper.getAttributeListFromElements("//" + COMPONENTPARAM_TAG, n);			
			Iterator<String> iter = xml.iterator();
			while(iter.hasNext()) {
				iter.next();
				name = iter.next();
				iter.next();
				value = iter.next();
				config.put(name,value);
			}
		} catch (XPathExpressionException e) {
			logger.error("Did not find any parameters for this Component");
		}
		return config;
	}
	
	/**
	 * Create a new instance of a fully configured component from its DOM document
	 * @param n the XML configuration node
	 * @return an instance of the component
	 * @throws ConfigurationException if the component's XML configuration is invalid and the component can not be created
	 */
	public T createComponent(Node n) throws ConfigurationException {
		T newc = null;
		try {
			Identifier id;
			synchronized(ctable) {
				 id = getComponentID(n);
				if(!ctable.containsKey(id)) {
					newc = build(n, id);
					if(newc!=null) {
							ctable.put(newc.getID(), newc);
							return newc;						
					} else {
							logger.error("Couldnt create component");
							throw new ConfigurationException();
					}
				}
			}
			//if we re here the table already has a component with this name 
			logger.error("There is already a component named " + id.toString());
			throw new ConfigurationException();
		} catch (InstantiationException e) {
			logger.error("Couldnt instanciate component from XML doc");
			throw new ConfigurationException();
		} catch (ParseException e) {
			logger.error("Couldnt retrieve the component ID from XML doc");
			throw new ConfigurationException();
		}
	}
	
	/** 
	 * Removes a previoulsy creatd component
	 * @param type the component type
	 */
	public void destroyComponent(Identifier i) throws ConfigurationException {
		synchronized(ctable) {
			if(ctable.containsKey(i)) {
				remove(ctable.get(i));
			} else {
				logger.error("Element " + i.toString()+ " doesnt exist and can NOT be removed");
				throw new ConfigurationException();
			}
		}
	}
	
	/** 
	 * Removes previoulsy created components
	 * @param l the list of compoenents to be deleted
	 */
	public void destroyComponents(ArrayList<T> l){
		synchronized(ctable) {
			for (int i = 0; i < l.size(); i++) {
				try{destroyComponent(l.get(i).getID());}
				catch (ConfigurationException e) {
					logger.error("Cant destroy "+l.get(i).toString());
				}
			}
		}
	}
	
	/** 
	 * Removes all previoulsy creatd components
	 *
	 */
	public void destroyAllComponents() {
		synchronized(ctable){
			Enumeration<T> e = ctable.elements();
			while (e.hasMoreElements())
				try { destroyComponent(e.nextElement().getID());}
				catch (ConfigurationException e1) {}
		}
	}
	
	/** 
	 * Get a component based on its identifer
	 * @param i the identifier
	 * @return the component or null if the identifier does not map to anything
	 *
	 */
	public T getComponent(Identifier i) {
		return ctable.get(i);
	}
	
	/** 
	 * Get an iterator on all components. MUST BE SYNCHRONIZED & 
	 * MUST NOT ALTER THE CONTENTS OTHER THAN WITH THE ITERATOR ITSELF!!!
	 * @return the iterator
	 *
	 */
	protected Iterator<T> getIterator() {
		return ctable.values().iterator();
	}
	
	/** 
	 * Get an enumeration of all keys. Need not be synchronized.
	 * @return the enumeration
	 *
	 */
	protected Enumeration<Identifier> getKeys() {
		return ctable.keys();
	}
	
	/** 
	 * Returns the number of components managed by this manager.
	 * @return the number of components
	 *
	 */
	public int getSize() {
		return ctable.size();
	}
	
	/**
	 * Prints the content of this manager's component table
	 *
	 */	
	public void dumpTable() {
		this.logger.debug("current table contents:" );
		synchronized(ctable){
			Enumeration<Identifier> keys = ctable.keys();
			Collection<T> cvalues = ctable.values();
			Iterator<T> iter = cvalues.iterator();
			while ( keys.hasMoreElements() &&  iter.hasNext())
			   this.logger.debug("key: " + keys.nextElement().toString() + " - "+iter.next().toString());
		}
	}
	
	/**
	 * Removes the specified component
	 */
	public void componentRemovable(Identifier i){
		synchronized(ctable){
			if(ctable.remove(i) == null)
				this.logger.error("Cant remove element with key " + i.toString() +  ": No such element");
			else
				this.logger.debug("Element " + i.toString()+ " Removed");
		}
	}
	
	/**
	 * Creates the component from a DOM document
	 * @param n the DOM document
	 * @param id the identifier for the new component
	 * @return the component
	 * @throws InstantiationException 
	 */
	protected abstract T build(Node n, Identifier id) throws InstantiationException;
	
	/**
	 * Deletes the component and give the subclass a chance to turn things off properly
	 * @param component the component
	 */
	protected abstract void remove(T component);
	
	/**
	 * returns the name of a component from its DOM document
	 * @param n the DOM document
	 * @return the ID of the component
	 */
	protected abstract Identifier getComponentID(Node n) throws ParseException;
	
	/**
	 * returns the type of a component from its DOM document
	 * @param n the DOM document
	 * @return the type of the component
	 */
	protected abstract String getComponentType(Node n) throws ParseException;

}
