
package jcu.sal.comms;

import jcu.sal.xml.JaxbHelper;
import jcu.sal.xml.ValidationException;
import jcu.sal.xml.Argument;
import jcu.sal.xml.MessageContent;
import jcu.sal.xml.MessageDescription;

import javax.xml.bind.JAXBException;

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
	public void testMessage() throws JAXBException {
		Message message2 = new Message(message1.toXmlString());
		assertTrue(message2.equals(message1));
	}

	@Test
	public void testTransportMessage() throws JAXBException {
		TransportMessage transportMessage2 = new TransportMessage(transportMessage1.toXmlString(), 0);
		assertTrue(transportMessage2.equals(transportMessage1));

		TransportMessage transportMessage3 = new TransportMessage(message1, 0);
		assertFalse(transportMessage3.equals(message1));
		assertTrue(message1.equals(transportMessage3));
	}

	@Test
	public void testDescriptionValidation() throws JAXBException {
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
		// wrong single type
		new ValidationTester().setWrongSingleType().test();
		// wrong array type
		new ValidationTester().setWrongArrayType().test();
		// wrong array types
		new ValidationTester().setWrongArrayTypes().test();
		// wrong array types and non array type
		new ValidationTester().setNonArrayType().setWrongArrayTypes().test();
		// too many arguments
		new ValidationTester().setTooManyArguments().test();
		// too many arguments and non array type
		new ValidationTester().setTooManyArguments().setNonArrayType().test();
		// too many arguments and wrong single type
		new ValidationTester().setTooManyArguments().setWrongSingleType().test();
		// too many arguments and wrong array type
		new ValidationTester().setTooManyArguments().setWrongArrayType().test();
		// too many arguments and wrong array types
		new ValidationTester().setTooManyArguments().setWrongArrayTypes().test();
		// too many arguments and wrong array types and non array type
		new ValidationTester().setTooManyArguments().setNonArrayType().setWrongArrayTypes().test();
		// too few arguments
		new ValidationTester().setTooFewArguments().test();
		// too few arguments and non array type
		new ValidationTester().setTooFewArguments().setNonArrayType().test();
		// too few arguments and wrong single type
		new ValidationTester().setTooFewArguments().setWrongSingleType().test();
		// too few arguments and wrong array type
		new ValidationTester().setTooFewArguments().setWrongArrayType().test();
		// too few arguments and wrong array types
		new ValidationTester().setTooFewArguments().setWrongArrayTypes().test();
		// too few arguments and wrong array types and non array type
		new ValidationTester().setTooFewArguments().setNonArrayType().setWrongArrayTypes().test();
	}

	private class ValidationTester {

		private Message message;
		private MessageDescription description;

		private String argsError = "";
		private String[] typeErrors = new String[10];

		public ValidationTester() throws JAXBException {
			String messageXml = "<?xml version='1.0' encoding='UTF-8'?>\n";
			messageXml += "<messageContent name='testMessage' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>123</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>123.0</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>true</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>onetwothree</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>b25ldHdvdGhyZWU=</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>123</value>\n";
			messageXml += "    <value>456</value>\n";
			messageXml += "    <value>789</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>123.0</value>\n";
			messageXml += "    <value>456.0</value>\n";
			messageXml += "    <value>789.0</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>true</value>\n";
			messageXml += "    <value>false</value>\n";
			messageXml += "    <value>true</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>onetwothree</value>\n";
			messageXml += "    <value>fourfivesix</value>\n";
			messageXml += "    <value>seveneightnine</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "  <argument>\n";
			messageXml += "    <value>b25ldHdvdGhyZWU=</value>\n";
			messageXml += "    <value>Zm91cmZpdmVzaXg=</value>\n";
			messageXml += "    <value>c2V2ZW5laWdodG5pbmU=</value>\n";
			messageXml += "  </argument>\n";
			messageXml += "</messageContent>\n";

			String descriptionXml = "<?xml version='1.0' encoding='UTF-8'?>\n";
			descriptionXml += "<messageDescription name='testMessage' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
			descriptionXml += "  <description>A message description used to test the validation</description>\n";
			descriptionXml += "  <argument name='SingleInt' type='int'>A single integer</argument>\n";
			descriptionXml += "  <argument name='SingleFloat' type='float'>A single float</argument>\n";
			descriptionXml += "  <argument name='SingleBoolean' type='boolean'>A single boolean</argument>\n";
			descriptionXml += "  <argument name='SingleString' type='string'>A single string</argument>\n";
			descriptionXml += "  <argument name='SingleBinary' type='binary'>A single binary</argument>\n";
			descriptionXml += "  <argument name='ArrayInt' type='int' array='true'>A array of integers</argument>\n";
			descriptionXml += "  <argument name='ArrayFloat' type='float' array='true'>A array of floats</argument>\n";
			descriptionXml += "  <argument name='ArrayBoolean' type='boolean' array='true'>A array of booleans</argument>\n";
			descriptionXml += "  <argument name='ArrayString' type='string' array='true'>A array of strings</argument>\n";
			descriptionXml += "  <argument name='ArrayBinary' type='binary' array='true'>A array of binarys</argument>\n";
			descriptionXml += "</messageDescription>\n";

			message = new Message(messageXml);
			description = (MessageDescription) JaxbHelper.fromXmlString(descriptionXml);

			for (int i = 0; i < 10; ++i) {
				typeErrors[i] = "";
			}
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
			message.setValue(4, "!onetwothree");

			typeErrors[4] += "Argument SingleBinary invalid - Value !onetwothree is not of type binary.\n";

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
			String error = MessageValidator.reasonInvalid(message.getContent(), description);
			String expectedError = getExpectedError();

			if(error == null) {
				assertTrue(expectedError.equals(""));
			} else {
				assertTrue(expectedError.equals(error));
			}
		}

	}

}
