package jcu.sal.Components.Protocols.CMLStore;

import javax.naming.ConfigurationException;


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
		b.append("\t<CID>101</CID>\n");
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
//		101 GetTemperature command
		b.append("<Command name=\"GetTemperature\">\n");
		b.append("\t<CID>101</CID>\n");
		b.append("\t<ShortDescription>Reads the temperature</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"temperature\">\n");
		b.append("\t\t\t<uom unit=\"degreeC\">degree C</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(101, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		112 GetHumidity command
		b.append("<Command name=\"GetHumidity\">\n");
		b.append("\t<CID>112</CID>\n");
		b.append("\t<ShortDescription>Reads the humidity</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"humidity\">\n");
		b.append("\t\t\t<uom unit=\"%\">%</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(112, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		113 GetHumidityHIH4000 command
		b.append("<Command name=\"GetHumidityHIH4000\">\n");
		b.append("\t<CID>113</CID>\n");
		b.append("\t<ShortDescription>Reads the humidity from a HIH4000 sensor</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"humidity\">\n");
		b.append("\t\t\t<uom unit=\"%\">%</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(113, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		114 GetHumidityHTM1735 command
		b.append("<Command name=\"getHumidityHTM1735\">\n");
		b.append("\t<CID>114</CID>\n");
		b.append("\t<ShortDescription>Reads the humidity from a HTM1735 sensor</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"humidity\">\n");
		b.append("\t\t\t<uom unit=\"%\">%</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(114, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		115 GetVAD command
		b.append("<Command name=\"getVAD\">\n");
		b.append("\t<CID>115</CID>\n");
		b.append("\t<ShortDescription>Reads the Vad voltage</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"voltage\">\n");
		b.append("\t\t\t<uom unit=\"v\">Volts</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(115, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		116 GetVDD command
		b.append("<Command name=\"getVdD\">\n");
		b.append("\t<CID>116</CID>\n");
		b.append("\t<ShortDescription>Reads the Vdd voltage</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"voltage\">\n");
		b.append("\t\t\t<uom unit=\"v\">Volts</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(116, b.toString());
		addCML("26.", c);
		b.delete(0, b.length());
//		117 GetVDD command
		b.append("<Command name=\"getVdD\">\n");
		b.append("\t<CID>117</CID>\n");
		b.append("\t<ShortDescription>Reads the Vis voltage</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"voltage\">\n");
		b.append("\t\t\t<uom unit=\"v\">Volts</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(117, b.toString());
		addCML("26.", c);
	}
}
