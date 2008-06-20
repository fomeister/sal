package jcu.sal.events;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.ConfigurationException;

import jcu.sal.common.events.Event;
import jcu.sal.common.events.EventHandler;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

public class EventDispatcher implements Runnable{

	private static EventDispatcher ev = new EventDispatcher();

	/**
	 * This map associates an event producer name to a map of names and event handlers. 
	 */
	private Map<String, List<EventHandler>> eventHandlers;
	private Thread dispatcher;
	private BlockingQueue<Event> eventQueue;
	private Set<String> producers; 
	private Logger logger = Logger.getLogger(EventDispatcher.class);
	
	private int QUEUE_SIZE = 500;
	
	private EventDispatcher() {
		eventHandlers = new Hashtable<String, List<EventHandler>>();
		dispatcher = new Thread(this, "SALEventDispatcher");
		eventQueue = new LinkedBlockingQueue<Event>(QUEUE_SIZE);
		producers = new HashSet<String>();
		Slog.setupLogger(logger);
		dispatcher.start();
	}
	
	public static EventDispatcher getInstance(){
		return ev;
	}
	
	public void stop() {
		dispatcher.interrupt();
	}
	
	public boolean addProducer(String p) {
		logger.debug("Adding producer "+p);
		synchronized(producers) { return producers.add(p); }
	}
	
	public boolean removeProducer(String p) {
		logger.debug("Removing producer "+p);
		synchronized(producers) { 
			synchronized(eventHandlers) { eventHandlers.remove(p); }
			return producers.remove(p);
		}
	}
	
	public void registerEventHandler(EventHandler e, String producer) throws ConfigurationException{
		synchronized (producers) {
			if(producers.contains(producer)) {
				synchronized(eventHandlers) {
					if(!eventHandlers.containsKey(producer))
						eventHandlers.put(producer, new Vector<EventHandler>());
					eventHandlers.get(producer).add(e);
				}
				logger.debug("Registered event handler "+e+" with producer: "+producer);
			} else {
				logger.error("No registered event producer with this name: "+producer);
				throw new ConfigurationException();
			}
		}
	}
	
	public void unregisterEventHandler(EventHandler e, String producer) throws ConfigurationException{
		synchronized (producers) {
			if(producers.contains(producer)) {
				synchronized(eventHandlers) {
					if(!eventHandlers.get(producer).remove(e)) {
						logger.error("Unregistering event handler "+e+" from producer: "+producer+" failed");
						throw new ConfigurationException("No such event handler");
					}
					else
						logger.debug("Unregistered event handler "+e+" from producer: "+producer);
				}				
			} else {
				logger.error("No registered event producer with this name: "+producer);
				throw new ConfigurationException();
			}
		}
	}
	
	public void queueEvent(Event e) throws ConfigurationException{
			if(!eventQueue.offer(e)){
				logger.error("Cant queue event, queue full");
				throw new ConfigurationException();
			}
			logger.debug("Queued "+e);
	}
	
	public void run() {
		logger.debug("Event dispatcher thread starting");
		Event e;
		List<EventHandler> l;
		Iterator<EventHandler> iterh;
		EventHandler ev;
		try {
			while(!Thread.interrupted()) {
				e = eventQueue.take();
				synchronized(eventHandlers) {
					l = eventHandlers.get(e.getProducer());
					if(l!=null) {
						iterh = l.iterator();
						while(iterh.hasNext()) {
							ev = iterh.next();
							logger.debug("Dispatching "+e.toString()+" to handler "+ev);
							ev.handle(e);
						}
					}
				}
				
				
			}
		}
		catch (InterruptedException e1) {}
		logger.debug("Event dispatcher thread exiting");
	}
}
