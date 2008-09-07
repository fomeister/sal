package jcu.sal.common.pcml;

import static org.junit.Assert.*;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.Parameters;

import org.junit.Test;

public class ProtocolConfigurationTest {

	@Test
	public void testProtocolConfigurationDocument() throws ConfigurationException, ParserConfigurationException {
		String params = "<"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+">";
		
		//should pass
		String ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+params
			+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+params + "</" + PCMLConstants.ENDPOINT_NODE+">"
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		new ProtocolConfiguration(ep);
		
		//should pass
		ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" />"
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		new ProtocolConfiguration(ep);
		
		//should pass
		ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+params
			+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" />"
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		new ProtocolConfiguration(ep);
		
		//should pass
		ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
		+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
		+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\">"
		+params
		+"</"+PCMLConstants.ENDPOINT_NODE+">"
		+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		new ProtocolConfiguration(ep);

		
		//shoud fail, no name
		ep = "<"+PCMLConstants.PROTOCOL_NODE+" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" />"
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		try {new ProtocolConfiguration(ep); fail("shouldnt be here");}
		catch (Exception e){}
		
		
		//shoud fail, no type
		ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\">"
		+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" />"
		+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		try {new ProtocolConfiguration(ep); fail("shouldnt be here");}
		catch (Exception e){}
	}

	@Test
	public void testGetID() throws ConfigurationException, ParserConfigurationException {
		String params = "<"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+">";
		
		//should pass
		String ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+params
			+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+params + "</" + PCMLConstants.ENDPOINT_NODE+">"
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		ProtocolConfiguration pc = new ProtocolConfiguration(ep);
		assertTrue(pc.getID().equals("testProtocol"));
	}

	@Test
	public void testGetType() throws ConfigurationException, ParserConfigurationException {
		String params = "<"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+">";
		
		//should pass
		String ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+params
			+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+params + "</" + PCMLConstants.ENDPOINT_NODE+">"
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		ProtocolConfiguration pc = new ProtocolConfiguration(ep);
		assertTrue(pc.getType().equals("testTypeProtocol"));
	}

	@Test
	public void testGetParameter() throws ConfigurationException, ParserConfigurationException {
		String params = "<"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+">";
		
		Parameters pa = new Parameters(params);
		
		//should pass
		String ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+params
			+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+params + "</" + PCMLConstants.ENDPOINT_NODE+">"
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		ProtocolConfiguration pc = new ProtocolConfiguration(ep);
		assertTrue(pc.getParameters().equals(pa));
	}

	@Test
	public void testGetEPConfig() throws ConfigurationException, ParserConfigurationException {
		String params = "<"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+">";

		
		//should pass
		String eps = "<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
		+params + "</" + PCMLConstants.ENDPOINT_NODE+">";
		String ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+params
			+eps
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
 
		System.out.println(ep);
		EndPointConfiguration epconf = new EndPointConfiguration(eps);
		ProtocolConfiguration pc = new ProtocolConfiguration(ep);
		assertTrue(pc.getEPConfig().equals(epconf));
	}

	@Test
	public void testEqualsObject() throws ConfigurationException, ParserConfigurationException {
		String params = "<"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+">";
		
		//should pass
		String ep = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
			+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
			+params
			+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+params + "</" + PCMLConstants.ENDPOINT_NODE+">"
			+"</"+PCMLConstants.PROTOCOL_NODE+">";
		System.out.println(ep);
		ProtocolConfiguration pc = new ProtocolConfiguration(ep);
		ProtocolConfiguration pc1 = new ProtocolConfiguration(ep);
		assertTrue(pc.equals(pc1));
	}

}
