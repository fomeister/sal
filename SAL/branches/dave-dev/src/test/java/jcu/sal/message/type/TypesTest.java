
package jcu.sal.message.type;

import jcu.sal.xml.ValidType;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.apache.log4j.Logger;

public class TypesTest {

	Logger logger = Logger.getLogger(this.getClass());

	private ValidType vt;
	private TypeFactory typeFactory = null;

	@Before
	public void setUp() {
		typeFactory = new TypeFactory();
	}

	@After
	public void tearDown() {
		typeFactory = null;
	}

	@Test
	public void testGarbageType() {
		Type t = typeFactory.createType("asdfasdfasdf", false);
		assertTrue(t == null);
	}

	@Test
	public void testIntType() {
		IntType it1 = (IntType) typeFactory.createType("int", false);

		assertTrue(it1 != null);

		IntType it2 = (IntType) typeFactory.createType(vt.INT, false);

		assertTrue(it2 != null);

		assertTrue(it1.validString("123"));
		assertTrue(!it1.validString("onetwothree"));
		assertTrue(!it1.validString(null));

		assertTrue(it1.toString("123").equals("123"));
		assertTrue(it1.toString("onetwothree") == null);
		assertTrue(it1.toString(null) == null);

		assertTrue(it1.matchString("1asdf") == 1);
		assertTrue(it1.matchString("123asdf") == 3);
		assertTrue(it1.matchString("asdf") == -1);

		assertTrue(it1.fromString("1").equals("1"));
		assertTrue(it1.fromString("123").equals("123"));
	}

	@Test
	public void testFloatType() {
		FloatType ft1 = (FloatType) typeFactory.createType("float", false);

		assertTrue(ft1 != null);

		FloatType ft2 = (FloatType) typeFactory.createType(vt.FLOAT, false);

		assertTrue(ft2 != null);

		assertTrue(ft1.validString("123.0"));
		assertTrue(!ft1.validString("onetwothree"));
		assertTrue(!ft1.validString(null));

		assertTrue(ft1.toString("123.0").equals("123.0"));
		assertTrue(ft1.toString("65.43").equals("65.43"));
		assertTrue(ft1.toString("+65.43").equals("65.43"));
		assertTrue(ft1.toString("-65.43").equals("-65.43"));
		assertTrue(ft1.toString("65.43e21").equals("6.543E22"));
		assertTrue(ft1.toString("65.43e+21").equals("6.543E22"));
		assertTrue(ft1.toString("65.43e-21").equals("6.543E-20"));
		assertTrue(ft1.toString("onetwothree") == null);
		assertTrue(ft1.toString(null) == null);

		assertTrue(ft1.matchString("65.43asdf") == 5);
		assertTrue(ft1.matchString("+65.43asdf") == 6);
		assertTrue(ft1.matchString("-65.43asdf") == 6);
		assertTrue(ft1.matchString("65.43e21asdf") == 8);
		assertTrue(ft1.matchString("65.43e+21asdf") == 9);
		assertTrue(ft1.matchString("65.43e-21asdf") == 9);
		assertTrue(ft1.matchString("asdf") == -1);

		assertTrue(ft1.fromString("65.43").equals("65.43"));
		assertTrue(ft1.fromString("+65.43").equals("65.43"));
		assertTrue(ft1.fromString("-65.43").equals("-65.43"));
		assertTrue(ft1.fromString("65.43e21").equals("6.543E22"));
		assertTrue(ft1.fromString("65.43e+21").equals("6.543E22"));
		assertTrue(ft1.fromString("65.43e-21").equals("6.543E-20"));
	}

	@Test
	public void testBooleanType() {
		BooleanType bt1 = (BooleanType) typeFactory.createType("boolean", false);

		assertTrue(bt1 != null);

		BooleanType bt2 = (BooleanType) typeFactory.createType(vt.BOOLEAN, false);

		assertTrue(bt2 != null);

		assertTrue(bt1.validString("true"));
		assertTrue(bt1.validString("True"));
		assertTrue(bt1.validString("TRUE"));
		assertTrue(bt1.validString("tRue"));
		assertTrue(bt1.validString("false"));
		assertTrue(bt1.validString("False"));
		assertTrue(bt1.validString("FALSE"));
		assertTrue(bt1.validString("fAlse"));
		assertTrue(!bt1.validString("nottrue"));
		assertTrue(!bt1.validString(null));

		assertTrue(bt1.toString("true").equals("true"));
		assertTrue(bt1.toString("True").equals("true"));
		assertTrue(bt1.toString("TRUE").equals("true"));
		assertTrue(bt1.toString("tRue").equals("true"));
		assertTrue(bt1.toString("false").equals("false"));
		assertTrue(bt1.toString("False").equals("false"));
		assertTrue(bt1.toString("FALSE").equals("false"));
		assertTrue(bt1.toString("fAlse").equals("false"));
		assertTrue(bt1.toString("nottrue") == null);
		assertTrue(bt1.toString(null) == null);

		assertTrue(bt1.matchString("trueasdf") == 4);
		assertTrue(bt1.matchString("Trueasdf") == 4);
		assertTrue(bt1.matchString("TRUEasdf") == 4);
		assertTrue(bt1.matchString("tRueasdf") == 4);
		assertTrue(bt1.matchString("falseasdf") == 5);
		assertTrue(bt1.matchString("Falseasdf") == 5);
		assertTrue(bt1.matchString("FALSEasdf") == 5);
		assertTrue(bt1.matchString("fAlseasdf") == 5);
		assertTrue(bt1.matchString("asdf") == -1);

		assertTrue(bt1.fromString("true").equals("true"));
		assertTrue(bt1.fromString("True").equals("true"));
		assertTrue(bt1.fromString("TRUE").equals("true"));
		assertTrue(bt1.fromString("tRue").equals("true"));
		assertTrue(bt1.fromString("false").equals("false"));
		assertTrue(bt1.fromString("False").equals("false"));
		assertTrue(bt1.fromString("FALSE").equals("false"));
		assertTrue(bt1.fromString("fAlse").equals("false"));
	}

	@Test
	public void testStringType() {
		StringType st1 = (StringType) typeFactory.createType("string", false);

		assertTrue(st1 != null);

		StringType st2 = (StringType) typeFactory.createType(vt.STRING, false);

		assertTrue(st2 != null);

		assertTrue(st1.validString("123"));
		assertTrue(st1.validString("onetwothree"));
		assertTrue(!st1.validString(null));

		assertTrue(st1.toString("123").equals("123"));
		assertTrue(st1.toString("onetwothree").equals("onetwothree"));
		assertTrue(st1.toString(null) == null);
	}

	@Test
	public void testBinaryType() {
		BinaryType bt1 = (BinaryType) typeFactory.createType("binary", false);

		assertTrue(bt1 != null);

		BinaryType bt2 = (BinaryType) typeFactory.createType(vt.BINARY, false);

		assertTrue(bt2 != null);

		assertTrue(bt1.validString("AQID"));
		assertTrue(bt1.validString("BAUG"));
		assertTrue(bt1.validString("BwgJ"));
		assertTrue(!bt1.validString("!AQID"));
		assertTrue(!bt1.validString("!BAUG"));
		assertTrue(!bt1.validString("!BwgJ"));
		assertTrue(!bt1.validString(null));

		assertTrue(bt1.toString("AQID").equals("{0x01, 0x02, 0x03}"));
		assertTrue(bt1.toString("BAUG").equals("{0x04, 0x05, 0x06}"));
		assertTrue(bt1.toString("BwgJ").equals("{0x07, 0x08, 0x09}"));
		assertTrue(bt1.toString("!AQID") == null);
		assertTrue(bt1.toString("!BAUG") == null);
		assertTrue(bt1.toString("!BwgJ") == null);
		assertTrue(bt1.toString(null) == null);

		assertTrue(bt1.matchString("{}asdf") == -1);
		assertTrue(bt1.matchString("{  }asdf") == -1);
		assertTrue(bt1.matchString("{0x01}asdf") == 6);
		assertTrue(bt1.matchString("{0x01  }asdf") == 8);
		assertTrue(bt1.matchString("{  0x01}asdf") == 8);
		assertTrue(bt1.matchString("{  0x01  }asdf") == 10);
		assertTrue(bt1.matchString("{0x01,0x02}asdf") == 11);
		assertTrue(bt1.matchString("{0x01,0x02  }asdf") == 13);
		assertTrue(bt1.matchString("{  0x01,0x02}asdf") == 13);
		assertTrue(bt1.matchString("{  0x01,0x02  }asdf") == 15);
		assertTrue(bt1.matchString("{0x01,  0x02}asdf") == 13);
		assertTrue(bt1.matchString("{0x01,  0x02  }asdf") == 15);
		assertTrue(bt1.matchString("{  0x01,  0x02}asdf") == 15);
		assertTrue(bt1.matchString("{  0x01,  0x02  }asdf") == 17);
		assertTrue(bt1.matchString("{0x01  ,0x02}asdf") == 13);
		assertTrue(bt1.matchString("{0x01  ,0x02  }asdf") == 15);
		assertTrue(bt1.matchString("{  0x01  ,0x02}asdf") == 15);
		assertTrue(bt1.matchString("{  0x01  ,0x02  }asdf") == 17);
		assertTrue(bt1.matchString("{0x01  ,  0x02}asdf") == 15);
		assertTrue(bt1.matchString("{0x01  ,  0x02  }asdf") == 17);
		assertTrue(bt1.matchString("{  0x01  ,  0x02}asdf") == 17);
		assertTrue(bt1.matchString("{  0x01  ,  0x02  }asdf") == 19);

		assertTrue(bt1.fromString("{0x01}").equals("AQ=="));
		assertTrue(bt1.fromString("{0x01  }").equals("AQ=="));
		assertTrue(bt1.fromString("{  0x01}").equals("AQ=="));
		assertTrue(bt1.fromString("{  0x01  }").equals("AQ=="));
		assertTrue(bt1.fromString("{0x01,0x02}").equals("AQI="));
		assertTrue(bt1.fromString("{0x01,0x02  }").equals("AQI="));
		assertTrue(bt1.fromString("{  0x01,0x02}").equals("AQI="));
		assertTrue(bt1.fromString("{  0x01,0x02  }").equals("AQI="));
		assertTrue(bt1.fromString("{0x01,  0x02}").equals("AQI="));
		assertTrue(bt1.fromString("{0x01,  0x02  }").equals("AQI="));
		assertTrue(bt1.fromString("{  0x01,  0x02}").equals("AQI="));
		assertTrue(bt1.fromString("{  0x01,  0x02  }").equals("AQI="));
		assertTrue(bt1.fromString("{0x01  ,0x02}").equals("AQI="));
		assertTrue(bt1.fromString("{0x01  ,0x02  }").equals("AQI="));
		assertTrue(bt1.fromString("{  0x01  ,0x02}").equals("AQI="));
		assertTrue(bt1.fromString("{  0x01  ,0x02  }").equals("AQI="));
		assertTrue(bt1.fromString("{0x01  ,  0x02}").equals("AQI="));
		assertTrue(bt1.fromString("{0x01  ,  0x02  }").equals("AQI="));
		assertTrue(bt1.fromString("{  0x01  ,  0x02}").equals("AQI="));
		assertTrue(bt1.fromString("{  0x01  ,  0x02  }").equals("AQI="));
	}

	@Test
	public void testArrayType() {
		ArrayType at1 = (ArrayType) typeFactory.createType("int", true);

		assertTrue(at1 != null);

		ArrayType at2 = (ArrayType) typeFactory.createType(vt.INT, true);

		assertTrue(at2 != null);

		assertTrue(at1.validStrings(new String[] {}));
		assertTrue(at1.validStrings(new String[] {"1"}));
		assertTrue(at1.validStrings(new String[] {"1", "2"}));
		assertTrue(at1.validStrings(new String[] {"1", "2", "3"}));
		assertTrue(!at1.validStrings(new String[] {"one"}));
		assertTrue(!at1.validStrings(new String[] {"1", "two"}));
		assertTrue(!at1.validStrings(new String[] {"1", "2", "three"}));
		assertTrue(!at1.validStrings(null));

		assertTrue(at1.toString(new String[] {}).equals("[]"));
		assertTrue(at1.toString(new String[] {"1"}).equals("[1]"));
		assertTrue(at1.toString(new String[] {"1", "2"}).equals("[1, 2]"));
		assertTrue(at1.toString(new String[] {"1", "2", "3"}).equals("[1, 2, 3]"));
		assertTrue(at1.toString(new String[] {"one"}) == null);
		assertTrue(at1.toString(new String[] {"1", "two"}) == null);
		assertTrue(at1.toString(new String[] {"1", "2", "three"}) == null);
		assertTrue(at1.toString(null) == null);
	}
}
