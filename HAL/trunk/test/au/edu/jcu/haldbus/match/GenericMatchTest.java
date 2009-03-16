package au.edu.jcu.haldbus.match;

import static org.junit.Assert.fail;

import org.junit.Test;

import au.edu.jcu.haldbus.exceptions.InvalidArgumentsException;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

public class GenericMatchTest {

	@Test(expected=InvalidArgumentsException.class)
	public void testMatchObject() throws InvalidArgumentsException {
		new GenericMatch<Integer>("name", new Integer(12), true, true);
	}
	
	@Test
	public void testMatchObject2() throws InvalidArgumentsException, MatchNotFoundException {
		GenericMatch<Integer> m = new GenericMatch<Integer>("name", new Integer(12));
		assert(m.match((Object) new Integer(12)).equals("12"));
	}
	
	@Test
	public void testMatchObject3() throws InvalidArgumentsException, MatchNotFoundException {
		GenericMatch<Integer> m = new GenericMatch<Integer>("name", new Integer(12));
		assert(m.match((Object) new Integer(12)).equals("1"));
	}
	
	@Test(expected=MatchNotFoundException.class)
	public void testMatchObject4() throws InvalidArgumentsException, MatchNotFoundException {
		GenericMatch<Integer> m = new GenericMatch<Integer>("name", new Integer(12));
		m.match((Object) new Integer(1));
	}	

	@Test
	public void testGenericMatchStringT() throws InvalidArgumentsException, MatchNotFoundException {
		GenericMatch<String> m = new GenericMatch<String>("name", "hello");
		assert(m.match((Object) "hello").equals("hello"));
		try {
			m.match((Object) "hello");
			m.match((Object) "helo");
			fail("Shouldnt be here");
		}
		catch (MatchNotFoundException e){}
		try {
			m.match((Object) "hell");
			fail("Shouldnt be here");
		}
		catch (MatchNotFoundException e){}
	}

	@Test
	public void testGenericMatchStringTBooleanBoolean() throws InvalidArgumentsException, MatchNotFoundException {
		GenericMatch<String> m = new GenericMatch<String>("name", "hello", true, true);
		m.match((Object) "hello");
		m.match((Object) "BonjourHellObjectDamnit");
		m.match((Object) "/dev/HeLlO");
		m.match((Object) "HeLLO");	
	}
	
	@Test
	public void testGenericMatchStringTBooleanBoolea2n() throws InvalidArgumentsException, MatchNotFoundException {
		GenericMatch<String> m = new GenericMatch<String>("name", "hello", true, false);
		m.match((Object) "hello");
		m.match((Object) "BonjourhellobjectDamnit");
		m.match((Object) "/dev/hello");
		m.match((Object) "/dev/hellolleh/dev");
	}
	
	@Test(expected=MatchNotFoundException.class)
	public void testGenericMatchStringTBooleanBoolean3() throws InvalidArgumentsException, MatchNotFoundException {
		GenericMatch<String> m = new GenericMatch<String>("name", "hello", false, true);
		try {
			m.match((Object) "hello");
			m.match((Object) "hellO");
			m.match((Object) "HELLO");
		} catch (MatchNotFoundException e) {
			fail("Shouldnt be here");
		}
		m.match((Object) "lLo");
	
	}

}
