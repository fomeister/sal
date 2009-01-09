
package jcu.sal.message.type;

public class ArrayType implements Type {

	SingleType baseType;

	public ArrayType(SingleType baseType) {
		this.baseType = baseType;
	}

	public boolean validStrings(String[] values) {
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
}
