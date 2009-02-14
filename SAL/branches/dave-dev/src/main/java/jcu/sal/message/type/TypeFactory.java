
package jcu.sal.message.type;

import jcu.sal.xml.ValidType;

public class TypeFactory {

	public static String[] getTypeNames() {
		return new String[] {
			"int",
			"float",
			"boolean",
			"string",
			"binary"
		};
	}

	public static Type createType(String typeName, boolean array) {
		if (typeName == null || typeName.length() == 0) {
			return null;
		}

		ValidType type = null;
		try {
			type = ValidType.fromValue(typeName);
		} catch (IllegalArgumentException iae) {
			return null;
		}

		return createType(type, array);
	}

	public static Type createType(ValidType type, boolean array) {
		if (array) {
			SingleType st = (SingleType) createType(type, false);

			if (st == null) {
				return null;
			}

			return createArrayType(st);
		}

		switch (type) {
			case INT:
				return createIntType();
			case FLOAT:
				return createFloatType();
			case BOOLEAN:
				return createBooleanType();
			case STRING:
				return createStringType();
			case BINARY:
				return createBinaryType();
		}

		return null;
	}

	protected static Type createIntType() {
		return new IntType();
	}

	protected static Type createFloatType() {
		return new FloatType();
	}

	protected static Type createBooleanType() {
		return new BooleanType();
	}

	protected static Type createStringType() {
		return new StringType();
	}

	protected static Type createBinaryType() {
		return new BinaryType();
	}

	protected static Type createArrayType(SingleType st) {
		return new ArrayType(st);
	}
}
