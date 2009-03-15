package jcu.sal.plugins.protocols.owfs;

import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore {

	private static CMLDescriptionStore c; 
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
		String key, name, mName, desc;
		List<String> argNames;
		List<ArgumentType> t;
		ReturnType r;
		
		/* 
		 * Family 10. 
		 * */
		key = OWFSProtocol.DS_10_FAMILY;
		mName = OWFSProtocol.GET_TEMPERATURE_METHOD;
		name = "ReadTemperature";
		desc = "Reads the temperature";
		t = new Vector<ArgumentType>();
		argNames = new Vector<String>();
		r = new ReturnType(CMLConstants.RET_TYPE_FLOAT);
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic GetReading command
		addGenericCMLDesc(OWFSProtocol.DS_10_FAMILY, GENERIC_GETREADING, i);
		//generic GetTemp command
		addGenericCMLDesc(OWFSProtocol.DS_10_FAMILY, GENERIC_GETTEMP, i);
		
		/* 
		 * Family 26. 
		 * */
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_HUMIDITY_METHOD;
		name = "ReadHumidity";
		desc = "Reads the humidity";
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic GetReading command
		addGenericCMLDesc(OWFSProtocol.DS_26_FAMILY, GENERIC_GETREADING, i);
		//generic GetHUMIDITY command
		addGenericCMLDesc(OWFSProtocol.DS_26_FAMILY, GENERIC_GETHUM, i);
		
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_TEMPERATURE_METHOD;
		name = "ReadTemperature";
		desc = "Reads the temperature";
		i = addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		//generic GetTemp command
		addGenericCMLDesc(OWFSProtocol.DS_26_FAMILY, GENERIC_GETTEMP, i);

		//private GetHumidityHIH4000 command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_HIH400_METHOD;
		name = "ReadHIH400Humidity";
		desc = "Reads the humidity from a HIH4000 sensor";
		addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		
		//private GetHumidityHTM1735 command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_HTM1735_METHOD;
		name = "ReadHTM1735Humidity";
		desc = "Reads the humidity from a HTM1735 sensor";
		addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		
		//private GetVAD command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_VAD_METHOD;
		name = "ReadVAD";
		desc = "Reads the Vad voltage";
		addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		
		//private GetVDD command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_VDD_METHOD;
		name = "ReadVDD";
		desc = "Reads the Vdd voltage";
		addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
		
		//private GetVis command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_VIS_METHOD;
		name = "ReadVIS";
		desc = "Reads the Vis voltage";
		addPrivateCMLDesc(key, mName, name, desc, t, argNames, r);
	}
}
