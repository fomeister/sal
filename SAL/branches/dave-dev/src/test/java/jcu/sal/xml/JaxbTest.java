
package jcu.sal.comms;

import jcu.sal.xml.JaxbHelper;
import jcu.sal.xml.ValidationException;
import jcu.sal.xml.MessageContent;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.apache.log4j.Logger;

public class JaxbTest {

	Logger logger = Logger.getLogger(this.getClass());

	private Message message1;

	@Before
	public void setUp() {
		message1 = new Message();
		message1.setName("Message 1");

		message1.setValue(0, 0, "a", "");
		message1.setValue(0, 1, "b", "");
		message1.setValue(1, 0, "c", "");
		message1.setValue(1, 1, "d", "");
		message1.setValue(1, 2, "e", "");
	}

	@After
	public void tearDown() {
		message1 = null;
	}

	@Test
	public void testMessage() throws JAXBException {
		Message message2 = new Message(message1.toXmlString());
		assertTrue(message2.equals(message1));
	}

	@Test
	public void testValidation() {
		String[] messages = new String[3];

		messages[0] = "<?xml version='1.0' encoding='UTF-8'?>\n";
		messages[0] += "<messageContent name='testMessage' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
		messages[0] += "  <argument>\n";
		messages[0] += "    <value>123</value>\n";
		messages[0] += "  </argument>\n";
		messages[0] += "  <argument>\n";
		messages[0] += "    <value>123.0</value>\n";
		messages[0] += "  </argument>\n";
		messages[0] += "</messageContent>\n";

		messages[1] = "<messageContent name='testMessage' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
		messages[1] += "  <argument>\n";
		messages[1] += "    <value>123</value>\n";
		messages[1] += "  </argument>\n";
		messages[1] += "  <argument>\n";
		messages[1] += "    <value>123.0</value>\n";
		messages[1] += "  </argument>\n";
		messages[1] += "</messageContent>\n";

		messages[2] = "<?xml version='1.0' encoding='UTF-8'?>\n";
		messages[2] += "<messageContent name='testMessage' final='yes' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
		messages[2] += "  <argument>\n";
		messages[2] += "    <value<123</value>\n";
		messages[2] += "  </argument>\n";
		messages[2] += "  <argument>\n";
		messages[2] += "    <value>123.0</value>\n";
		messages[2] += "  </argument>\n";
		messages[2] += "</messageContent>\n";

		String[] errors = new String[messages.length];

		errors[0] = "";

		errors[1] = "";

		errors[2] = "[4,11]: Element type \"value\" must be followed by either attribute specifications, \">\" or \"/>\".\n";

		for (int i = 0; i < messages.length; ++i) {
			try {
				JaxbHelper.validateString(messages[i]);
				assertTrue(errors[i].equals(""));
			} catch (ValidationException e) {
				assertTrue(errors[i].equals(e.getMessage()));
			}
		}
	}
}

