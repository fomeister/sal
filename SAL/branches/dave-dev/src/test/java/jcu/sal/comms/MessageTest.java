
package jcu.sal.comms;

import jcu.sal.xml.Argument;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class MessageTest {

	private Message message1;
	private TransportMessage transportMessage1;

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
}
