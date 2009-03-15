package jcu.sal.plugins.protocols.dummy;

import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore{
	private static CMLDescriptionStore c;
	public static String DUMMY_KEY="Dummy";
	static {
		try {
			c = new CMLDescriptionStore();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static CMLDescriptionStore getStore() {
		return c;
	}
	

	private CMLDescriptionStore() throws ConfigurationException, NotFoundException, AlreadyPresentException{
		int i;
		String name, mName, desc;
		List<String> argNames;
		List<ArgumentType> t;
		ReturnType r;
		
		/* 
		 * 
		 * */
		mName = DummyProtocol.GET_READING_METHOD;
		name = "ReadValue";
		desc = "Reads the value of this sensor";
		t = new Vector<ArgumentType>();
		argNames = new Vector<String>();
		r = new ReturnType(CMLConstants.ARG_TYPE_INT);
		i = addPrivateCMLDesc(DUMMY_KEY, mName, name, desc, t, argNames, r);
		//generic GetReading command
		addGenericCMLDesc(DUMMY_KEY, GENERIC_GETREADING, i);
		}
}
