
package jcu.sal.message.type;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class IntType implements SingleType {

	public static final Pattern pattern = Pattern.compile("(\\+|\\-)?[0-9]+");

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

		return String.valueOf(new Integer(value));
	}

	public int matchString(String text) {
		Matcher m = pattern.matcher(text);
		if (!m.lookingAt()) {
			return -1;
		}

		return m.end();
	}

	public String fromString(String text) {
		return String.valueOf(new Integer(text));
	}
}
