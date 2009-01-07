
package jcu.sal.comms;

import jcu.sal.xml.Argument;
import jcu.sal.xml.MessageDescription;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class MessageTest {

	private Message message1;
	private TransportMessage transportMessage1;

	private MessageDescription description1;

	// going to want to bulk load these from file / jar
	// use var names as command names and build 
	// a hashmap from command names to filestrings and
	// a hashmap from command names to expected error strings
	// try to do crazy resources opening stuff
	// otherwise use spring
	private String invalidXml;
	private String wrongType;
	private String nonArrayType;
	private String wrongArrayType;
	private String wrongArrayTypes;
	private String wrongArrayTypesAndNonArrayType;
	private String tooManyArguments;
	private String tooManyArgumentsWrongType;
	private String tooManyArgumentsNonArrayType;
	private String tooManyArgumentsWrongArrayType;
	private String tooManyArgumentsWrongArrayTypes;
	private String tooManyArgumentswrongArrayTypesAndNonArrayType;
	private String tooFewArguments;
	private String tooFewArgumentsWrongType;
	private String tooFewArgumentsNonArrayType;
	private String tooFewArgumentsWrongArrayType;
	private String tooFewArgumentsWrongArrayTypes;
	private String tooFewArgumentswrongArrayTypesAndNonArrayType;
	private String invalidInteger;
	private String invalidFloat;
	private String invalidBoolean;
	private String invalidBinary;
	private String valid;

	@Before
	public void setUp() {
		message1 = new Message();
		message1.setName("Message 1");

		Argument message1Argument1 = new Argument();
		message1Argument1.getValue().add("a");
		message1Argument1.getValue().add("b");

		Argument message1Argument2 = new Argument();
		message1Argument2.getValue().add("c");
		message1Argument2.getValue().add("d");
		message1Argument2.getValue().add("e");

		message1.getArgument().add(message1Argument1);
		message1.getArgument().add(message1Argument2);

		transportMessage1 = new TransportMessage();
		transportMessage1.setName("Transport Message 1");

		Argument transportMessage1Argument1 = new Argument();
		transportMessage1Argument1.getValue().add("k");
		transportMessage1Argument1.getValue().add("l");

		Argument transportMessage1Argument2 = new Argument();
		transportMessage1Argument2.getValue().add("m");
		transportMessage1Argument2.getValue().add("n");
		transportMessage1Argument2.getValue().add("o");

		transportMessage1.getArgument().add(transportMessage1Argument1);
		transportMessage1.getArgument().add(transportMessage1Argument2);
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
	public void testValidation() {
	}
}
