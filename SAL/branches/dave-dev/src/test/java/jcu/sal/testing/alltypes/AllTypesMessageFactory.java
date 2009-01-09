
package jcu.sal.testing.alltypes;

import jcu.sal.message.Message;
import jcu.sal.message.InvalidMessageException;

import jcu.sal.xml.JaxbHelper;
import jcu.sal.xml.XmlException;

import jcu.sal.xml.MessageDescription;

public class AllTypesMessageFactory {

	public static final String ALL_TYPES_MESSAGE_NAME = "AllTypes";

	private static MessageDescription allTypesMessageDescription = null;

	private static MessageDescription getAllTypesMessageDescription() {
		if (allTypesMessageDescription == null) {

			String descriptionXml = "<?xml version='1.0' encoding='UTF-8'?>\n";
			descriptionXml += "<messageDescription name='" + ALL_TYPES_MESSAGE_NAME + "' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
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

			try {
				allTypesMessageDescription = (MessageDescription) JaxbHelper.fromXmlString(descriptionXml);
			} catch (XmlException xe) {
			}
		}
		return allTypesMessageDescription;
	}

	public static String[] getMessageNames() {
		String[] s = new String[1];
		s[0] = ALL_TYPES_MESSAGE_NAME;
		return s;
	}

	public static MessageDescription getDescription(String commandName) {
		if (commandName.equals(ALL_TYPES_MESSAGE_NAME)) {
			return getAllTypesMessageDescription();
		}

		return null;
	}

	public static Message createDefaultAllTypesMessage() {
		String messageXml = "<?xml version='1.0' encoding='UTF-8'?>\n";
		messageXml += "<messageContent name='" + ALL_TYPES_MESSAGE_NAME + "' xmlns='http://sal.jcu.edu.au/schemas/messages'>\n";
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
		messageXml += "    <value>AQID</value>\n";
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
		messageXml += "    <value>AQID</value>\n";
		messageXml += "    <value>BAUG</value>\n";
		messageXml += "    <value>BwgJ</value>\n";
		messageXml += "  </argument>\n";
		messageXml += "</messageContent>\n";

		Message message = null;
		try {
			message = new Message(messageXml);
			message.setDescription(getAllTypesMessageDescription());
		} catch(InvalidMessageException ime) {
			return null;
		}

		return message;
	}
}
