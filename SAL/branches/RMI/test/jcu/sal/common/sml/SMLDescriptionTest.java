/**
 * 
 */
package jcu.sal.common.sml;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.ConfigurationException;

import jcu.sal.utils.XMLhelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author gilles
 *
 */
public class SMLDescriptionTest {
	private Hashtable<String, String> p1, p2, p3;
	
	//missing sid attribute
	private String sml1 = "<"+SMLConstants.SENSOR_TAG+">\n" +
							"<"+SMLConstants.PARAMETERS_NODE+">\n"+
							"<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocol\" />\n"+
							"<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MySensorAddress\" />\n"+
							"</"+SMLConstants.PARAMETERS_NODE+">\n"+
							"</"+SMLConstants.SENSOR_TAG+">";
	
	//has an extra parameter
	private String sml2 = "<"+SMLConstants.SENSOR_TAG+" "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\"10\">\n" +
							"<"+SMLConstants.PARAMETERS_NODE+">\n"+
							"<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocol\" />\n"+
							"<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MySensorAddress\" />\n"+
							"<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+
							"=\"shouldNOT\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"goTHROUGH\" />\n"+
							"</"+SMLConstants.PARAMETERS_NODE+">\n"+
							"</"+SMLConstants.SENSOR_TAG+">";
	
	//missing one required parameter
	private String sml3 = "<"+SMLConstants.SENSOR_TAG+" "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\"10\">\n" +
							"<"+SMLConstants.PARAMETERS_NODE+">\n"+
							"<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocol\" />\n"+
							"</"+SMLConstants.PARAMETERS_NODE+">\n"+
							"</"+SMLConstants.SENSOR_TAG+">";
	
	//shoud be OK
	private String sml4 = "<"+SMLConstants.SENSOR_TAG+" "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\"10\">\n" +
							"<"+SMLConstants.PARAMETERS_NODE+">\n"+
							"<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocol\" />\n"+
							"<"+SMLConstants.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MySensorAddress\" />\n"+
							"</"+SMLConstants.PARAMETERS_NODE+">\n"+
							"</"+SMLConstants.SENSOR_TAG+">";
	
	
	Document d1, d2, d3, d4;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		p1 = new Hashtable<String, String>();
		p2 = new Hashtable<String, String>();
		p3 = new Hashtable<String, String>();
		
		p1.put(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, "MyProtocol");
		p1.put(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE, "MySensorAddress");
		p1.put("shoudNOT", "goTHROUGH");
		
		p2.put(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, "MyProtocol");
		p2.put("shoudNOT", "goTHROUGH");
		
		p3.put(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, "MyProtocol");
		p3.put(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE, "MySensorAddress");
		
		try {
			d1 = XMLhelper.createDocument(sml1);
			d2 = XMLhelper.createDocument(sml2);
			d3 = XMLhelper.createDocument(sml3);
			d4 = XMLhelper.createDocument(sml4);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#SMLDescription(java.lang.Integer, java.util.Hashtable)}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testSMLDescriptionIntegerHashtableOfStringString() throws ConfigurationException{
		try {
			new SMLDescription(new Integer(10), p1);
			fail("shoudnt be here");
		} catch (ConfigurationException e) {}
		
		try {
			new SMLDescription(new Integer(10), p2);
			fail("shoudnt be here");
		} catch (ConfigurationException e) {}
		
		try {
			new SMLDescription(new Integer(SMLConstants.SENSOR_ID_MAX+1), p3);
			fail("shoudnt be here");
		} catch (ConfigurationException e) {}
		
		new SMLDescription(new Integer(1), p3);
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#SMLDescription(org.w3c.dom.Document)}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testSMLDescriptionDocument() throws ConfigurationException {
		try {
			new SMLDescription(d1);
			fail("shoudnt be here");
		} catch (ConfigurationException e) {}
		try {
			new SMLDescription(d2);
			fail("shoudnt be here");
		} catch (ConfigurationException e) {}
		try {
			new SMLDescription(d3);
			fail("shoudnt be here");
		} catch (ConfigurationException e) {}
		System.out.println("Checking d4:"+XMLhelper.toString(d4));
		new SMLDescription(d4);
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#getSID()}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testGetSID() throws ConfigurationException {
		SMLDescription s = new SMLDescription(d4);
		assertTrue(s.getSID()==10);
		
		s = new SMLDescription(new Integer(1), p3);
		assertTrue(s.getSID()==1);
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#getParameter(java.lang.String)}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testGetParameter() throws ConfigurationException {
		SMLDescription s = new SMLDescription(d4);
		assertEquals(s.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE), "MyProtocol");
		assertEquals(s.getParameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE), "MySensorAddress");
		
		s = new SMLDescription(new Integer(1), p3);
		assertEquals(s.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE), "MyProtocol");
		assertEquals(s.getParameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE), "MySensorAddress");
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#getParameterNames()}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testGetParameterNames() throws ConfigurationException {
		SMLDescription s = new SMLDescription(d4);
		Set<String> set = new HashSet<String>();
		Set<String> set1 = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
		Set<String> set3 = new HashSet<String>();
		set.add(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		set.add(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE);
		
		set1.add(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		set1.add(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE);
		set1.add("DONTWORK");
		
		set2.add(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);

		set3.add(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		set3.add("DONTWORK");
		
		assertEquals(s.getParameterNames(), set);
		assertFalse(s.getParameterNames().equals(set1));
		assertFalse(s.getParameterNames().equals(set2));
		assertFalse(s.getParameterNames().equals(set3));
		
		s = new SMLDescription(new Integer(1), p3);
		assertEquals(s.getParameterNames(), set);
		assertFalse(s.getParameterNames().equals(set1));
		assertFalse(s.getParameterNames().equals(set2));
		assertFalse(s.getParameterNames().equals(set3));
	}

}
