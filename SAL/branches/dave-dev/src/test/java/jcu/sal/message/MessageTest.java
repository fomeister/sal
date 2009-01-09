
package jcu.sal.message;

import jcu.sal.testing.alltypes.AllTypesMessageFactory;

import jcu.sal.comms.TransportMessage;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.apache.log4j.Logger;

public class MessageTest {

	Logger logger = Logger.getLogger(this.getClass());

	private Message message1;
	private TransportMessage transportMessage1;

	@Before
	public void setUp() {
		message1 = new Message();
		message1.setName("Message 1");

		message1.setValue(0, 0, "a", "");
		message1.setValue(0, 1, "b", "");
		message1.setValue(1, 0, "c", "");
		message1.setValue(1, 1, "d", "");
		message1.setValue(1, 2, "e", "");

		transportMessage1 = new TransportMessage();
		transportMessage1.setName("Transport Message 1");

		transportMessage1.setValue(0, 0, "k", "");
		transportMessage1.setValue(0, 1, "l", "");
		transportMessage1.setValue(1, 0, "m", "");
		transportMessage1.setValue(1, 1, "n", "");
		transportMessage1.setValue(1, 2, "o", "");
	}

	@After
	public void tearDown() {
		message1 = null;
		transportMessage1 = null;
	}

	@Test
	public void testMessage() throws InvalidMessageException {
		Message message2 = new Message(message1.toXmlString());
		assertTrue(message2.equals(message1));
	}

	@Test
	public void testTransportMessage() throws InvalidMessageException {
		TransportMessage transportMessage2 = new TransportMessage(transportMessage1.toXmlString(), 0);
		assertTrue(transportMessage2.equals(transportMessage1));

		TransportMessage transportMessage3 = new TransportMessage(message1, 0);
		assertFalse(transportMessage3.equals(message1));
		assertTrue(message1.equals(transportMessage3));
	}

	@Test
	public void testToString() throws InvalidMessageException {
		Message message = AllTypesMessageFactory.createDefaultAllTypesMessage();

		String withDescription = message.toString();

		String expected1 = "";
		expected1 += "AllTypes(";
		expected1 += "123, ";
		expected1 += "123.0, ";
		expected1 += "true, ";
		expected1 += "onetwothree, ";
		expected1 += "{0x01, 0x02, 0x03}, ";
		expected1 += "[123, 456, 789], ";
		expected1 += "[123.0, 456.0, 789.0], ";
		expected1 += "[true, false, true], ";
		expected1 += "[onetwothree, fourfivesix, seveneightnine], ";
		expected1 += "[{0x01, 0x02, 0x03}, {0x04, 0x05, 0x06}, {0x07, 0x08, 0x09}]";
		expected1 += ")";

		assertTrue(expected1.equals(message.toString()));

		message.setFinal(false);

		expected1 += "+";

		assertTrue(expected1.equals(message.toString()));

		message.setFinal(true);

		message.setDescription(null);

		String expected2 = "";
		expected2 += "AllTypes(";
		expected2 += "123, ";
		expected2 += "123.0, ";
		expected2 += "true, ";
		expected2 += "onetwothree, ";
		expected2 += "AQID, ";
		expected2 += "[123, 456, 789], ";
		expected2 += "[123.0, 456.0, 789.0], ";
		expected2 += "[true, false, true], ";
		expected2 += "[onetwothree, fourfivesix, seveneightnine], ";
		expected2 += "[AQID, BAUG, BwgJ]";
		expected2 += ")";

		assertTrue(expected2.equals(message.toString()));

		message.setFinal(false);

		expected2 += "+";

		assertTrue(expected2.equals(message.toString()));
	}

	@Test
	public void testDescriptionValidation() {
		ValidationTester t = null;
		// valid
		new ValidationTester().test();

		// bad integer
		new ValidationTester().setBadInteger().test();
		// bad float
		new ValidationTester().setBadFloat().test();
		// bad boolean
		new ValidationTester().setBadBoolean().test();
		// bad binary
		new ValidationTester().setBadBinary().test();

		// non array type
		new ValidationTester().setNonArrayType().test();
		// empty single value
		new ValidationTester().setEmptySingleValue().test();
		// wrong single type
		new ValidationTester().setWrongSingleType().test();
		// wrong array type
		new ValidationTester().setWrongArrayType().test();
		// wrong array types
		new ValidationTester().setWrongArrayTypes().test();
		// wrong array types and non array type
		new ValidationTester().setWrongArrayTypes().setNonArrayType().test();

		// too many arguments
		new ValidationTester().setTooManyArguments().test();
		// too many arguments and non array type
		new ValidationTester().setTooManyArguments().setNonArrayType().test();
		// too many arugments and empty single value
		new ValidationTester().setTooManyArguments().setEmptySingleValue().test();
		// too many arguments and wrong single type
		new ValidationTester().setTooManyArguments().setWrongSingleType().test();
		// too many arguments and wrong array type
		new ValidationTester().setTooManyArguments().setWrongArrayType().test();
		// too many arguments and wrong array types
		new ValidationTester().setTooManyArguments().setWrongArrayTypes().test();
		// too many arguments, wrong array types, non array type and empty single value
		new ValidationTester().setTooManyArguments().setWrongArrayTypes().setNonArrayType().setEmptySingleValue().test();

		// too few arguments
		new ValidationTester().setTooFewArguments().test();
		// too few arguments and non array type
		new ValidationTester().setTooFewArguments().setNonArrayType().test();
		// too few arugments and empty single value
		new ValidationTester().setTooFewArguments().setEmptySingleValue().test();
		// too few arguments and wrong single type
		new ValidationTester().setTooFewArguments().setWrongSingleType().test();
		// too few arguments and wrong array type
		new ValidationTester().setTooFewArguments().setWrongArrayType().test();
		// too few arguments and wrong array types
		new ValidationTester().setTooFewArguments().setWrongArrayTypes().test();
		// too few arguments, wrong array types, non array type and empty single value
		new ValidationTester().setTooFewArguments().setWrongArrayTypes().setNonArrayType().setEmptySingleValue().test();
	}

	private class ValidationTester {

		private Message message;

		private String argsError = "";
		private String[] typeErrors = new String[10];

		public ValidationTester() {
			message = AllTypesMessageFactory.createDefaultAllTypesMessage();

			for (int i = 0; i < 10; ++i) {
				typeErrors[i] = "";
			}
		}

		public ValidationTester setEmptySingleValue() {
			message.getArgument().get(1).getValue().remove(0);

			typeErrors[1] += "Argument SingleFloat invalid - No value set for single valued argument.\n";

			return this;
		}

		public ValidationTester setNonArrayType() {
			message.setValue(0, 1, "456", "");
			message.setValue(0, 2, "789", "");

			typeErrors[0] += "Argument SingleInt invalid - More than one value for non-array argument.\n";

			return this;
		}

		public ValidationTester setWrongSingleType() {
			message.setValue(0, "onetwothree");

			typeErrors[0] += "Argument SingleInt invalid - Value onetwothree is not of type int.\n";

			return this;
		}

		public ValidationTester setWrongArrayType() {
			message.setValue(5, 1, "fourfivesix", "");

			typeErrors[0] += "Argument ArrayInt invalid - Value at index 1 (fourfivesix) is not of type int.\n";

			return this;
		}

		public ValidationTester setWrongArrayTypes() {
			message.setValue(5, 0, "onetwothree", "");
			message.setValue(5, 2, "seveneightnine", "");
			message.setValue(7, 0, "nottrue", "");
			message.setValue(7, 1, "notfalse", "");
			message.setValue(7, 2, "nottrue", "");

			typeErrors[5] += "Argument ArrayInt invalid - Values at indices 0 and 2 (onetwothree and seveneightnine) are not of type int.\n";
			typeErrors[7] += "Argument ArrayBoolean invalid - Values at indices 0, 1 and 2 (nottrue, notfalse and nottrue) are not of type boolean.\n";

			return this;
		}

		public ValidationTester setTooManyArguments() {
			message.setValue(10, "123");

			argsError += "Incorrect number of arguments - Expected: 10 Found: 11.\n";

			return this;
		}

		public ValidationTester setTooFewArguments() {
			message.getArgument().remove(9);

			argsError += "Incorrect number of arguments - Expected: 10 Found: 9.\n";

			return this;
		}

		public ValidationTester setBadInteger() {
			message.setValue(0, "onetwothree");

			typeErrors[0] += "Argument SingleInt invalid - Value onetwothree is not of type int.\n";

			return this;
		}

		public ValidationTester setBadFloat() {
			message.setValue(1, "onetwothree");

			typeErrors[1] += "Argument SingleFloat invalid - Value onetwothree is not of type float.\n";

			return this;
		}

		public ValidationTester setBadBoolean() {
			message.setValue(2, "nottrue");

			typeErrors[2] += "Argument SingleBoolean invalid - Value nottrue is not of type boolean.\n";

			return this;
		}

		public ValidationTester setBadBinary() {
			message.setValue(4, "!AQID");

			typeErrors[4] += "Argument SingleBinary invalid - Value !AQID is not of type binary.\n";

			return this;
		}

		private String getExpectedError() {
			String error = argsError;

			for (int i = 0; i < 10; ++i) {
				error += typeErrors[i];
			}

			return error;
		}

		public void test() {
			String error = MessageValidator.reasonInvalid(message.getContent(), message.getDescription());
			String expectedError = getExpectedError();

			if(error == null) {
				assertTrue(expectedError.equals(""));
			} else {
				assertTrue(expectedError.equals(error));
			}
		}

	}

}
