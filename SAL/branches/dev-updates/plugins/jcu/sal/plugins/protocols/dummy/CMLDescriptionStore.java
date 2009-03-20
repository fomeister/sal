package jcu.sal.plugins.protocols.dummy;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ResponseType;
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
		ResponseType r;
		
		/* 
		 * 
		 * */
		mName = DummyProtocol.GET_READING_METHOD;
		name = "ReadValue";
		desc = "Reads the value of this sensor";
		r = new ResponseType(CMLConstants.ARG_TYPE_INT);
		i = addPrivateCMLDesc(DUMMY_KEY, mName, name, desc, null, r);
		//generic GetReading command
		addGenericCMLDesc(DUMMY_KEY, GENERIC_GETREADING, i);
		}
}
