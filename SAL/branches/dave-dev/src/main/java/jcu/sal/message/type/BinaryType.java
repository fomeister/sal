
package jcu.sal.message.type;

import org.apache.commons.codec.binary.Base64;

public class BinaryType implements SingleType {

	public boolean validString(String value) {
		try {
			return Base64.isArrayByteBase64(value.getBytes("UTF-8"));
		} catch (Exception e) {
			return false;
		}
	}

	public String toString(String value) {
		if (!validString(value)) {
			return null;
		}

		byte[] b = Base64.decodeBase64(value.getBytes());

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
}
