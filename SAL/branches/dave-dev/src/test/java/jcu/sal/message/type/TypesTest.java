
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
		assertTrue(ft1.toString("onetwothree") == null);
		assertTrue(ft1.toString(null) == null);
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
		assertTrue(bt1.toString("nottrue") == null);
		assertTrue(bt1.toString(null) == null);
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
	}

	@Test
	public void testArrayType() {
	}
}
