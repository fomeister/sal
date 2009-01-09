
package jcu.sal.message.type;

public class StringType implements SingleType {

	public boolean validString(String value) {
		if (value == null) {
			return false;
		}

		return true;
	}

	public String toString(String value) {
		if (!validString(value)) {
			return null;
		}

		return value;
	}
}
