
package jcu.sal.message.type;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class StringType implements SingleType {

//	public static final Pattern escapePattern = Pattern.compile("(\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|[0-7]|[0-7][0-7]|[0-3][0-7][0-7]|\\u[0-9a-fA-F]{4})");
//	public static final Pattern invalidPattern = Pattern.compile("\\");

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

	public int matchString(String text) {
		return -1;
	}

	public String fromString(String text) {
		return null;
	}
}
