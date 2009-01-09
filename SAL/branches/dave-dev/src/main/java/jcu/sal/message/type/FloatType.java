
package jcu.sal.message.type;

public class FloatType implements SingleType {

	public boolean validString(String value) {
		if (value == null) {
			return false;
		}

		try {
			Float f = Float.parseFloat(value);
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
