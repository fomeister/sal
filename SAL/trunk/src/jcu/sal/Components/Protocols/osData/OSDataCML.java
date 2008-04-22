package jcu.sal.Components.Protocols.osData;

import javax.naming.ConfigurationException;

import jcu.sal.Components.Protocols.CMLStore;


public class OSDataCML extends CMLStore{
	private static OSDataCML c; 
	static {
		try {
			c = new OSDataCML();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} 
	}
	
	public static OSDataCML getStore() {
		return c;
	}
	
	private OSDataCML() throws ConfigurationException{
		super();
		StringBuffer b = new StringBuffer();
		CMLDoc c;

		/* 
		 * FreeMem
		 * */
//		generic 100 GetReading command
		addSensor("FreeMem");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the amount of free memory</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"memory\">\n");
		b.append("\t\t\t<uom unit=\"kilobytes\">kB</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("FreeMem", c);
		b.delete(0, b.length());
		
		/* 
		 * UserTime
		 * */
//		generic 100 GetReading command
		addSensor("UserTime");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the amount of time spent in user mode</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"time\">\n");
		b.append("\t\t\t<uom unit=\"milliseconds\">ms</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("UserTime", c);
		b.delete(0, b.length());

		/* 
		 * NiceTime
		 * */
//		generic 100 GetReading command
		addSensor("NiceTime");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the amount of time spent in nice mode</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"time\">\n");
		b.append("\t\t\t<uom unit=\"milliseconds\">ms</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("NiceTime", c);		
		b.delete(0, b.length());
		
		/* 
		 * SystemTime
		 * */
//		generic 100 GetReading command
		addSensor("SystemTime");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the amount of time spent in system mode</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"time\">\n");
		b.append("\t\t\t<uom unit=\"milliseconds\">ms</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("SystemTime", c);		
		b.delete(0, b.length());
		
		/* 
		 * IdleTime
		 * */
//		generic 100 GetReading command
		addSensor("IdleTime");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the amount of time spent in idle mode</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"time\">\n");
		b.append("\t\t\t<uom unit=\"milliseconds\">ms</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("IdleTime", c);
		b.delete(0, b.length());
		
		/* 
		 * LoadAvg1
		 * */
//		generic 100 GetReading command
		addSensor("LoadAvg1");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the 1-minute load average</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"none\" />\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("LoadAvg1", c);
		b.delete(0, b.length());
		
		/* 
		 * LoadAvg5
		 * */
//		generic 100 GetReading command
		addSensor("LoadAvg5");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the 5-minute load average</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"none\" />\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("LoadAvg5", c);
		b.delete(0, b.length());
		
		/* 
		 * LoadAvg15
		 * */
//		generic 100 GetReading command
		addSensor("LoadAvg15");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the 15-minute load average</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"none\" />\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("LoadAvg15", c);
		b.delete(0, b.length());
		
		/* 
		 * CPUTemp
		 * */
//		generic 100 GetReading command
		addSensor("CPUTemp");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the CPU temperature</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"temperature\">\n");
		b.append("\t\t\t<uom unit=\"degreeC\">degree C</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("CPUTemp", c);
		b.delete(0, b.length());
		
		/* 
		 * NBTemp
		 * */
//		generic 100 GetReading command
		addSensor("NBTemp");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the North Bridge temperature</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"temperature\">\n");
		b.append("\t\t\t<uom unit=\"degreeC\">degree C</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("NBTemp", c);
		b.delete(0, b.length());
		
		/* 
		 * SBTemp
		 * */
//		generic 100 GetReading command
		addSensor("SBTemp");
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the South Bridge temperature</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"temperature\">\n");
		b.append("\t\t\t<uom unit=\"degreeC\">degree C</uom>\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML("SBTemp", c);
		b.delete(0, b.length());
	}
}
