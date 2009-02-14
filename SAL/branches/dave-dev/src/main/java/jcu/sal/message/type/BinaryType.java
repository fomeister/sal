
package jcu.sal.message.type;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BinaryType implements SingleType {

	private static final String STRING_ENCODING = "UTF-8";

//	public static final Pattern wholePattern = Pattern.compile("\\{\\s*(0x[0-9A-F]{1,2}\\s*,\\s*)*0x[0-9A-F]{1,2}\\s*\\}", Pattern.CASE_INSENSITIVE);

	public static final Pattern openPattern = Pattern.compile("\\{\\s*");
	public static final Pattern dataPattern = Pattern.compile("0[xX][0-9A-Fa-f]{1,2}");
	public static final Pattern commaPattern = Pattern.compile("\\s*,\\s*");
	public static final Pattern closePattern = Pattern.compile("\\s*\\}");

	public boolean validString(String value) {
		try {
			return Base64.isArrayByteBase64(value.getBytes(STRING_ENCODING));
		} catch (Exception e) {
			return false;
		}
	}

	public String toString(String value) {
		if (!validString(value)) {
			return null;
		}

		byte[] b = new byte[0];

		try {
			b = Base64.decodeBase64(value.getBytes(STRING_ENCODING));
		} catch (java.io.UnsupportedEncodingException uee) {
			return null;
		}

		String s = "{";

		int size = b.length;
		for (int i = 0; i < size; ++i) {

			s += "0x";

			String h = Integer.toHexString(new Byte(b[i]).intValue()).toUpperCase();
			while (h.length() != 2) {
				h = "0" + h;
			}

			s += h;

			if (i != size - 1) {
				s += ", ";
			}
		}

		s += "}";

		return s;
	}

	public int matchString(String text) {
		/*
		Matcher m = wholePattern.match(text);
		if (!m.lookingAt() {
			return -1;
		}

		return m.end();
		*/

		String oldText = text;

		Matcher openMatcher = openPattern.matcher(text);
		if (!openMatcher.lookingAt()) {
			return -1;
		}

		text = text.substring(openMatcher.end());

		Matcher dataMatcher = null;
		Matcher commaMatcher = null;
		Matcher closeMatcher = null;

		while (true) {
			dataMatcher = dataPattern.matcher(text);

			if (!dataMatcher.lookingAt()) {
				return -1;
			}

			text = text.substring(dataMatcher.end());

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

	public String fromString(String text) {
		ArrayList<Integer> data = new ArrayList<Integer>();

		Matcher openMatcher = openPattern.matcher(text);
		openMatcher.lookingAt();
		text = text.substring(openMatcher.end());

		Matcher dataMatcher = null;
		Matcher commaMatcher = null;
		Matcher closeMatcher = null;

		while (true) {
			dataMatcher = dataPattern.matcher(text);
			dataMatcher.lookingAt();

			data.add(Integer.parseInt(text.substring(2, dataMatcher.end()), 16));

			text = text.substring(dataMatcher.end());

			commaMatcher = commaPattern.matcher(text);
			closeMatcher = closePattern.matcher(text);

			if (commaMatcher.lookingAt()) {
				text = text.substring(commaMatcher.end());
			} else if (closeMatcher.lookingAt()) {
				text = text.substring(closeMatcher.end());
				break;
			}
		}

		byte[] b = new byte[data.size()];

		for (int i = 0; i < data.size(); ++i) {
			b[i] = data.get(i).byteValue();
		}

		String s = null;

		try {
			s = new String(Base64.encodeBase64(b), STRING_ENCODING);
		} catch (UnsupportedEncodingException uee) {
			return null;
		}

		return s;
	}
}
