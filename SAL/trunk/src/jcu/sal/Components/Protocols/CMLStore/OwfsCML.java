package jcu.sal.Components.Protocols.CMLStore;

import java.util.Hashtable;

public class OwfsCML implements CMLStore {
	private static Hashtable<String, String> cmls = new Hashtable<String, String>();
	
	public String getCML(String f){
		if(cmls.containsKey(f))
			return cmls.get(f);
		return null;
	}
	
	public OwfsCML(){
		StringBuffer b = new StringBuffer();
		
		/* 
		 * Family 10. 
		 * */
		
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
		cmls.put("10.", b.toString());
		b.delete(0, b.length());

		
		/* 
		 * Family 26. 
		 * */
		
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
		cmls.put("26.", b.toString());
		b.delete(0, b.length());
	}
}
