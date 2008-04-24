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
		StringBuffer b = new StringBuffer();
		CMLDoc c;
		int id = PRIVATE_CID_START;
		
		/* 
		 * Family 10. 
		 * */
		addSensor("10.");
//		generic 100 GetReading command
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the temperature</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"temperature\">\n");
		b.append("\t\t\t<uom unit=\"degreeC\">degree C</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("10.", c);
		b.delete(0, b.length());
//		101 GetTemperature
		b.append("<Command name=\"GetTemperature\">\n");
		b.append("\t<CID>110</CID>\n");
		b.append("\t<ShortDescription>Reads the temperature</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"temperature\">\n");
		b.append("\t\t\t<uom unit=\"degreeC\">degree C</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(101, b.toString());
		addCML("10.", c);
		b.delete(0, b.length());

		
		/* 
		 * Family 26. 
		 * */
		addSensor("26.");
//		generic 100 GetReading command
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the humidity</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"humidity\">\n");
		b.append("\t\t\t<uom unit=\"%\">%</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		110 GetTemperature command
		b.append("<Command name=\"GetTemperature\">\n");
		b.append("\t<CID>110</CID>\n");
		b.append("\t<ShortDescription>Reads the temperature</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"temperature\">\n");
		b.append("\t\t\t<uom unit=\"degreeC\">degree C</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(110, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		111 GetHumidity command
		b.append("<Command name=\"GetHumidity\">\n");
		b.append("\t<CID>111/CID>\n");
		b.append("\t<ShortDescription>Reads the humidity</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"humidity\">\n");
		b.append("\t\t\t<uom unit=\"%\">%</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(111, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		1001 GetHumidityHIH4000 command
		b.append("<Command name=\"GetHumidityHIH4000\">\n");
		b.append("\t<CID>"+(++id)+"</CID>\n");
		b.append("\t<ShortDescription>Reads the humidity from a HIH4000 sensor</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"humidity\">\n");
		b.append("\t\t\t<uom unit=\"%\">%</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(id, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		1002 GetHumidityHTM1735 command
		b.append("<Command name=\"getHumidityHTM1735\">\n");
		b.append("\t<CID>"+(++id)+"</CID>\n");
		b.append("\t<ShortDescription>Reads the humidity from a HTM1735 sensor</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"humidity\">\n");
		b.append("\t\t\t<uom unit=\"%\">%</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(id, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		1003 GetVAD command
		b.append("<Command name=\"getVAD\">\n");
		b.append("\t<CID>"+(++id)+"</CID>\n");
		b.append("\t<ShortDescription>Reads the Vad voltage</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"voltage\">\n");
		b.append("\t\t\t<uom unit=\"v\">Volts</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(id, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		1004 GetVDD command
		b.append("<Command name=\"getVdd\">\n");
		b.append("\t<CID>"+(++id)+"</CID>\n");
		b.append("\t<ShortDescription>Reads the Vdd voltage</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"voltage\">\n");
		b.append("\t\t\t<uom unit=\"v\">Volts</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(id, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		1005 GetVDD command
		b.append("<Command name=\"getVis\">\n");
		b.append("\t<CID>"+(++id)+"</CID>\n");
		b.append("\t<ShortDescription>Reads the Vis voltage</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"voltage\">\n");
		b.append("\t\t\t<uom unit=\"v\">Volts</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(id, b.toString());
		addCML("26.", c);
	}
}
