package jcu.sal.events;

public interface EventHandler {
	public String getName();
	public void handle(Event e);
}
