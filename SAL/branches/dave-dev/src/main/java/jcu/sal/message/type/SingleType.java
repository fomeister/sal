
package jcu.sal.message.type;

public interface SingleType extends Type {

	boolean validString(String value);

	String toString(String value);

	public int matchString(String text);

	public String fromString(String text);
}
