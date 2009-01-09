
package jcu.sal.message.type;

public class IntType implements SingleType {

	public boolean validString(String value) {
		if (value == null) {
			return false;
		}

		try {
			Integer i = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
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
