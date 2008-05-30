package jcu.sal.config;

import static org.junit.Assert.fail;
import jcu.sal.components.protocols.v4l2.HalClient;
import jcu.sal.config.deviceDetection.HalHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;

public class HalHelperTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=AddRemoveElemException.class)
	public void testHalHelper() throws AddRemoveElemException {
		HalHelper h = new HalHelper();
		try {
			h.addClient(new HalClient());
			fail("shouldnt be here");
		} catch (AddRemoveElemException e) {
			throw e;
		} catch (InvalidConstructorArgs e) {
			e.printStackTrace();
			fail("shouldnt be here");
		}
		fail("shouldnt be here");
	}

	@Test
	public void testStart() {
		fail("Not yet implemented");
	}

	@Test
	public void testStop() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddClient() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveClient() {
		fail("Not yet implemented");
	}

}
