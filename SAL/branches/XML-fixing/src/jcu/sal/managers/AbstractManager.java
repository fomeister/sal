/**
 * 
 */
package jcu.sal.managers;

import java.text.ParseException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.components.HWComponent;
import jcu.sal.components.HWComponentConfiguration;
import jcu.sal.components.Identifier;
import jcu.sal.components.componentRemovalListener;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * Creates manager classes, which create, delete and manage components (Endpoints, AbstractProtocol, ...)
 * @author gilles
 *
 */
public abstract class AbstractManager<T extends HWComponent, U extends HWComponentConfiguration> implements componentRemovalListener {
	
	public static String COMPONENTPARAM_TAG = "Param";
	
	private static Logger logger = Logger.getLogger(AbstractManager.class);
	static { Slog.setupLogger(logger); }
	protected Map<Identifier, T> ctable;
	private Map<String, List<Identifier>> typeMap;
	
	public AbstractManager() {
		ctable = new Hashtable<Identifier, T>();
		typeMap = new Hashtable<String, List<Identifier>>();
	}

	/**
	 * returns the configuration directives for this component
	 * @param c the configuration object for this component
	 * @return the Parameters associated with this component
	 */
	protected Parameters getComponentConfig(U c){
		return c.getParameters();
	}
	
	/**
	 * Create a new instance of a fully configured component from its DOM document
	 * @param c the configuration object for the component
	 * @return an instance of the component
	 * @throws ConfigurationException if the component can not be created
	 */
	public T createComponent(U c) throws ConfigurationException {
		T newc = null;
		String type = c.getType();
		Identifier id;
		try {
			synchronized(ctable) {
				id = getComponentID(c);
				if(!ctable.containsKey(id)) {
					newc = build(c, id);
					if(newc!=null) {
							//store new component
							ctable.put(newc.getID(), newc);
							//store new type
							if(typeMap.get(type)==null)
								typeMap.put(type, new LinkedList<Identifier>());
							typeMap.get(type).add(id);
							
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
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	/** 
	 * Removes a previoulsy creatd component
	 * @param type the component type
	 */
	public void destroyComponent(Identifier i) throws ConfigurationException {
		synchronized(ctable) {
			T t = ctable.get(i);
			if(t != null) {
				typeMap.get(t.getType()).remove(i);
				remove(t);
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
	public void destroyComponents(List<T> l){
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
			Collection<T>  c = new Vector<T>(ctable.values()); 
			Iterator<T> e = c.iterator();
			while (e.hasNext()) {
				try { destroyComponent(e.next().getID());}
				catch (ConfigurationException e1) {}
			}
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
	 * Removes the specified component
	 */
	public void componentRemovable(Identifier i){
		synchronized(ctable){
			if(ctable.remove(i) == null)
				logger.error("Cant remove element with key " + i.toString() +  ": No such element");
			else
				logger.debug("Element " + i.toString()+ " Removed");
		}
	}
	
	/**
	 * This method returns a list of components Identifiers of the given type
	 * @param t the type
	 * @return the list of Identifiers
	 * @throws ConfigurationException if the given type is not found
	 */
	public List<Identifier> getComponentsOfType(String t) throws ConfigurationException{
		List<Identifier> l;
		synchronized(ctable){
			if((l=typeMap.get(t))==null)
				throw new ConfigurationException();
			return new LinkedList<Identifier>(l);
		}
	}
	
	/**
	 * Instanciates a component
	 * @param c the configuration object
	 * @param id the identifier for the new component
	 * @return the component
	 * @throws InstantiationException 
	 */
	protected abstract T build(U c, Identifier id) throws InstantiationException;
	
	/**
	 * Deletes the component and give the subclass a chance to turn things off properly
	 * @param component the component
	 */
	protected abstract void remove(T component);
	
	/**
	 * returns the ID of a component from its configuration object
	 * @param c the configuration object
	 * @return the ID of the component
	 */
	protected abstract Identifier getComponentID(U c) throws ParseException;
}
