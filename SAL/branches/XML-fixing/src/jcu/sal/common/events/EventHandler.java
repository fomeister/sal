package jcu.sal.common.events;


public interface EventHandler<T extends Event>{
	public void handle(T e);
}
