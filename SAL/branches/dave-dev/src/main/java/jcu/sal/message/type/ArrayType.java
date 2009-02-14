
package jcu.sal.message.type;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ArrayType implements Type {

	public static final Pattern openPattern = Pattern.compile("\\[\\s*");
	public static final Pattern commaPattern = Pattern.compile("\\s*,\\s*");
	public static final Pattern closePattern = Pattern.compile("\\s*\\]");

	private SingleType baseType;

	public ArrayType(SingleType baseType) {
		this.baseType = baseType;
	}

	public boolean validStrings(String[] values) {
		if (values == null) {
			return false;
		}

		int size = values.length;
		for (int i = 0; i < size; ++i) {
			if (!baseType.validString(values[i])) {
				return false;
			}
		}

		return true;
	}

	public String toString(String[] values) {
		if (!validStrings(values)) {
			return null;
		}

		String s = "";

		s += "[";

		int size = values.length;
		for (int i = 0; i < size; ++i) {
			s += baseType.toString(values[i]);

			if (i != size - 1) {
				s += ", ";
			}
		}

		s += "]";

		return s;
	}

	public int matchString(String text) {
		String oldText = text;

		Matcher openMatcher = openPattern.matcher(text);
		if (!openMatcher.lookingAt()) {
			return -1;
		}

		text = text.substring(openMatcher.end());

		Matcher commaMatcher = null;
		Matcher closeMatcher = null;

		String[] typeNames = TypeFactory.getTypeNames();
		int numTypes = typeNames.length;
		SingleType[] types = new SingleType[numTypes];

		for (int i = 0; i < numTypes; ++i) {
			types[i] = (SingleType) TypeFactory.createType(typeNames[i], false);
		}

		while (true) {

			boolean found = false;

			for (int i = 0; i < numTypes; ++i) {
				int m = types[i].matchString(text);

				if (m != -1) {
					text = text.substring(m);
					found = true;
					break;
				}
			}

			if (!found) {
				return -1;
			}

			commaMatcher = commaPattern.matcher(text);
			closeMatcher = closePattern.matcher(text);

			if (commaMatcher.lookingAt()) {
				text = text.substring(commaMatcher.end());
			} else if (closeMatcher.lookingAt()) {
				text = text.substring(closeMatcher.end());
				break;
			} else {
				return -1;
			}
		}

		return oldText.length() - text.length();
	}

	public String[] fromString(String text) {
		ArrayList<String> data = new ArrayList<String>();

		Matcher openMatcher = openPattern.matcher(text);
		openMatcher.lookingAt();
		text = text.substring(openMatcher.end());

		Matcher commaMatcher = null;
		Matcher closeMatcher = null;

		String[] typeNames = TypeFactory.getTypeNames();
		int numTypes = typeNames.length;
		SingleType[] types = new SingleType[numTypes];

		for (int i = 0; i < numTypes; ++i) {
			types[i] = (SingleType) TypeFactory.createType(typeNames[i], false);
		}

		while (true) {

			for (int i = 0; i < numTypes; ++i) {
				int m = types[i].matchString(text);
				if (m != -1) {
					data.add(text.substring(0, m));
					text = text.substring(m);
				}
			}

			commaMatcher = commaPattern.matcher(text);
			closeMatcher = closePattern.matcher(text);

			if (commaMatcher.lookingAt()) {
				text = text.substring(commaMatcher.end());
			} else if (closeMatcher.lookingAt()) {
				text = text.substring(closeMatcher.end());
				break;
			}
		}

		return data.toArray(new String[0]);

	}
}
