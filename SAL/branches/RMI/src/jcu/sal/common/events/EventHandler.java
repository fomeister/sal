package jcu.sal.common.events;

import java.io.Serializable;

import jcu.sal.events.Event;

public interface EventHandler extends Serializable{
	public String getName();
	public void handle(Event e);
}
