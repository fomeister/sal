package jcu.sal.components.protocols.owfs;

import javax.naming.ConfigurationException;

import jcu.sal.components.protocols.CMLStore;


public class OwfsCML extends CMLStore {

	private static OwfsCML c; 
	static {
		try {
			c = new OwfsCML();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} 
	}
	
	public static OwfsCML getStore() {
		return c;
	}
	
	private OwfsCML() throws ConfigurationException{
		int i;
		
		/* 
		 * Family 10. 
		 * */
		i = addPrivateCMLDesc(OwfsProtocol.DS_10_FAMILY, OwfsProtocol.GET_TEMPERATURE_METHOD, "ReadTemperature", "Reads the temperature", new String[0], new String[0]);
		//generic GetReading command
		addGenericCMLDesc(OwfsProtocol.DS_10_FAMILY, GENERIC_GETREADING, new Integer(i));
		//generic GetTemp command
		addGenericCMLDesc(OwfsProtocol.DS_10_FAMILY, GENERIC_GETTEMP, new Integer(i));
		
		/* 
		 * Family 26. 
		 * */
		i = addPrivateCMLDesc(OwfsProtocol.DS_26_FAMILY, OwfsProtocol.GET_HUMIDITY_METHOD, "ReadHumidity", "Reads the humidity", new String[0], new String[0]);
		//generic GetReading command
		addGenericCMLDesc(OwfsProtocol.DS_26_FAMILY, GENERIC_GETREADING, new Integer(i));
		//generic GetHUMIDITY command
		addGenericCMLDesc(OwfsProtocol.DS_26_FAMILY, GENERIC_GETHUM, new Integer(i));
		
		i = addPrivateCMLDesc(OwfsProtocol.DS_26_FAMILY, OwfsProtocol.GET_TEMPERATURE_METHOD, "ReadTemperature", "Reads the temperature", new String[0], new String[0]);
		//generic GetTemp command
		addGenericCMLDesc(OwfsProtocol.DS_26_FAMILY, GENERIC_GETTEMP, new Integer(i));

		//private GetHumidityHIH4000 command
		addPrivateCMLDesc(OwfsProtocol.DS_26_FAMILY, OwfsProtocol.GET_HIH400_METHOD, "ReadHIH400Humidity", "Reads the humidity from a HIH4000 sensor", new String[0], new String[0]);
		//private GetHumidityHTM1735 command
		addPrivateCMLDesc(OwfsProtocol.DS_26_FAMILY, OwfsProtocol.GET_HTM1735_METHOD, "ReadHTM1735Humidity", "Reads the humidity from a HTM1735 sensor", new String[0], new String[0]);
		//private GetVAD command
		addPrivateCMLDesc(OwfsProtocol.DS_26_FAMILY, OwfsProtocol.GET_VAD_METHOD, "ReadVAD", "Reads the Vad voltage", new String[0], new String[0]);
		//private GetVDD command
		addPrivateCMLDesc(OwfsProtocol.DS_26_FAMILY, OwfsProtocol.GET_VDD_METHOD, "ReadVDD", "Reads the Vdd voltage", new String[0], new String[0]);
		//private GetVis command
		addPrivateCMLDesc(OwfsProtocol.DS_26_FAMILY, OwfsProtocol.GET_VIS_METHOD, "ReadVIS", "Reads the Vis voltage", new String[0], new String[0]);
	}
}
