
package jcu.sal.message.type;

public class BooleanType implements SingleType {

	public boolean validString(String value) {
		if (value == null) {
			return false;
		}

		String lower = value.toLowerCase();
		if (lower.equals("true") || lower.equals("false")) {
			return true;
		}

		return false;
	}

	public String toString(String value) {
		if (!validString(value)) {
			return null;
		}

		return value;
	}
}
