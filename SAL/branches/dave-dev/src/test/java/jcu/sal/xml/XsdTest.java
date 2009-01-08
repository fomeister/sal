
package jcu.sal.xml;

import jcu.sal.xml.JaxbHelper;
import jcu.sal.xml.XsdHelper;
import jcu.sal.xml.ValidationException;

import jcu.sal.xml.MessageContent;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.apache.log4j.Logger;

public class XsdTest {

	Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void testValidation() {
		String[] messages = new String[4];

		messages[0] = "<?xml version='1.0' encoding='UTF-8'?>\n";
		messages[0] += "<messageContent name='testMessage'>\n";
		messages[0] += "  <argument>\n";
		messages[0] += "    <value>123</value>\n";
		messages[0] += "  </argument>\n";
		messages[0] += "  <argument>\n";
		messages[0] += "    <value>123.0</value>\n";
		messages[0] += "  </argument>\n";
		messages[0] += "</messageContent>\n";

		messages[1] = "<?xml version='1.0' encoding='UTF-8'?>\n";
		messages[1] += "<messageContent name='testMessage' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
		messages[1] += "  <argument>\n";
		messages[1] += "    <value>123</value>\n";
		messages[1] += "  </argument>\n";
		messages[1] += "  <argument>\n";
		messages[1] += "    <value>123.0</value>\n";
		messages[1] += "  </argument>\n";
		messages[1] += "  <random-new-tag />\n";
		messages[1] += "</messageContent>\n";

		messages[2] = "<?xml version='1.0' encoding='UTF-8'?>\n";
		messages[2] += "<messageContent xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
		messages[2] += "  <argument>\n";
		messages[2] += "    <value>123</value>\n";
		messages[2] += "  </argument>\n";
		messages[2] += "  <argument>\n";
		messages[2] += "    <value>123.0</value>\n";
		messages[2] += "  </argument>\n";
		messages[2] += "</messageContent>\n";

		messages[3] = "<?xml version='1.0' encoding='UTF-8'?>\n";
		messages[3] += "<messageContent name='testMessage' final='yes' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
		messages[3] += "  <argument>\n";
		messages[3] += "    <value>123</value>\n";
		messages[3] += "  </argument>\n";
		messages[3] += "  <argument>\n";
		messages[3] += "    <value>123.0</value>\n";
		messages[3] += "  </argument>\n";
		messages[3] += "</messageContent>\n";

		String[] errors = new String[messages.length];

		errors[0] = "[2,36]: Cannot find the declaration of element 'messageContent'.\n";

		errors[1] = "[9,21]: Invalid content was found starting with element 'random-new-tag'. One of '{\"http://sal.jcu.edu.au/schemas/messages\":argument}' is expected.\n";

		errors[2] = "[2,64]: Attribute 'name' must appear on element 'messageContent'.\n";

		errors[3] = "[2,95]: 'yes' is not a valid value for 'boolean'.\n";
		errors[3] += "[2,95]: The value 'yes' of attribute 'final' on element 'messageContent' is not valid with respect to its type, 'boolean'.\n";

		for (int i = 0; i < messages.length; ++i) {
			try {
				XsdHelper.validateString(JaxbHelper.getSchema(MessageContent.class), messages[i]);
				assertTrue(errors[i].equals(""));
			} catch (ValidationException e) {
				assertTrue(errors[i].equals(e.getMessage()));
			}
		}
	}

}
