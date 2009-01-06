
package jcu.sal.comms;

import jcu.sal.comms.transport.TransportCommand;
import jcu.sal.comms.transport.TransportResponse;
import jcu.sal.xml.Argument;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class MessageTest {

	private Command command1;
	private Response response1;
	private TransportCommand transportCommand1;
	private TransportResponse transportResponse1;

	@Before
	public void setUp() {
		command1 = new Command();
		command1.setName("Command 1");

		Argument command1Argument1 = new Argument();
		command1Argument1.getValue().add("a");
		command1Argument1.getValue().add("b");

		Argument command1Argument2 = new Argument();
		command1Argument2.getValue().add("c");
		command1Argument2.getValue().add("d");
		command1Argument2.getValue().add("e");

		command1.getArgument().add(command1Argument1);
		command1.getArgument().add(command1Argument2);

		response1 = new Response();
		response1.setName("Response 1");

		Argument response1Argument1 = new Argument();
		response1Argument1.getValue().add("f");
		response1Argument1.getValue().add("g");

		Argument response1Argument2 = new Argument();
		response1Argument2.getValue().add("h");
		response1Argument2.getValue().add("i");
		response1Argument2.getValue().add("j");

		response1.getArgument().add(response1Argument1);
		response1.getArgument().add(response1Argument2);

		transportCommand1 = new TransportCommand();
		transportCommand1.setName("Transport Command 1");

		Argument transportCommand1Argument1 = new Argument();
		transportCommand1Argument1.getValue().add("k");
		transportCommand1Argument1.getValue().add("l");

		Argument transportCommand1Argument2 = new Argument();
		transportCommand1Argument2.getValue().add("m");
		transportCommand1Argument2.getValue().add("n");
		transportCommand1Argument2.getValue().add("o");

		transportCommand1.getArgument().add(transportCommand1Argument1);
		transportCommand1.getArgument().add(transportCommand1Argument2);

		transportResponse1 = new TransportResponse();
		transportResponse1.setName("Transport Response 1");

		Argument transportResponse1Argument1 = new Argument();
		transportResponse1Argument1.getValue().add("p");
		transportResponse1Argument1.getValue().add("q");

		Argument transportResponse1Argument2 = new Argument();
		transportResponse1Argument2.getValue().add("r");
		transportResponse1Argument2.getValue().add("s");
		transportResponse1Argument2.getValue().add("t");

		transportResponse1.getArgument().add(transportResponse1Argument1);
		transportResponse1.getArgument().add(transportResponse1Argument2);
	}

	@After
	public void tearDown() {
		command1 = null;
		response1 = null;
		transportCommand1 = null;
		transportResponse1 = null;
	}

	@Test
	public void testCommand() throws JAXBException {
		Command command2 = new Command(command1.toXmlString());
		assertTrue(command2.equals(command1));
	}

	@Test
	public void testResponse() throws JAXBException {
		Response response2 = new Response(response1.toXmlString());
		assertTrue(response2.equals(response1));
	}

	@Test
	public void testTransportCommand() throws JAXBException {
		TransportCommand transportCommand2 = new TransportCommand(transportCommand1.toXmlString());
		assertTrue(transportCommand2.equals(transportCommand1));

		TransportCommand transportCommand3 = new TransportCommand(command1, 0);
		assertFalse(transportCommand3.equals(command1));
		assertTrue(command1.equals(transportCommand3));
	}

	@Test
	public void testTransportResponse() throws JAXBException {
		TransportResponse transportResponse2 = new TransportResponse(transportResponse1.toXmlString());
		assertTrue(transportResponse2.equals(transportResponse1));

		TransportResponse transportResponse3 = new TransportResponse(response1, 0);
		assertFalse(transportResponse3.equals(response1));
		assertTrue(response1.equals(transportResponse3));
	}
}
