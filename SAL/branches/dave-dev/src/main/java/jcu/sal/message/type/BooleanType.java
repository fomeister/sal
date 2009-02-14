
package jcu.sal.message.type;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BooleanType implements SingleType {

	public static final Pattern pattern = Pattern.compile("(true|false)", Pattern.CASE_INSENSITIVE);

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

		return value.toLowerCase();
	}

	public int matchString(String text) {
		Matcher m = pattern.matcher(text);
		if (!m.lookingAt()) {
			return -1;
		}

		return m.end();
	}

	public String fromString(String text) {
		return text.toLowerCase();
	}
}
