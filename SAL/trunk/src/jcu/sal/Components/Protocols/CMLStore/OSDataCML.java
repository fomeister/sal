package jcu.sal.Components.Protocols.CMLStore;

import java.util.Hashtable;

public class OSDataCML implements CMLStore{
	private static Hashtable<String, String> cmls = new Hashtable<String, String>();
	
	public String getCML(String f){
		if(cmls.containsKey(f))
			return cmls.get(f);
		return null;
	}
	
	public OSDataCML(){
		StringBuffer b = new StringBuffer();

		/* 
		 * FreeMem
		 * */
//		generic 100 GetReading command
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
		cmls.put("FreeMem", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * UserTime
		 * */
//		generic 100 GetReading command
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
		cmls.put("UserTime", b.toString());
		b.delete(0, b.length());

		/* 
		 * NiceTime
		 * */
//		generic 100 GetReading command
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
		cmls.put("NiceTime", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * SystemTime
		 * */
//		generic 100 GetReading command
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
		cmls.put("SystemTime", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * IdleTime
		 * */
//		generic 100 GetReading command
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
		cmls.put("IdleTime", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * LoadAvg1
		 * */
//		generic 100 GetReading command
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the 1-minute load average</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"none\" />\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		cmls.put("LoadAvg1", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * LoadAvg5
		 * */
//		generic 100 GetReading command
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the 5-minute load average</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"none\" />\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		cmls.put("LoadAvg5", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * LoadAvg1
		 * */
//		generic 100 GetReading command
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the 15-minute load average</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"float\" quantity=\"none\" />\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		cmls.put("LoadAvg15", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * CPUTemp
		 * */
//		generic 100 GetReading command
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
		cmls.put("CPUTemp", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * NBTemp
		 * */
//		generic 100 GetReading command
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
		cmls.put("NBTemp", b.toString());
		b.delete(0, b.length());
		
		/* 
		 * SBTemp
		 * */
//		generic 100 GetReading command
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
		cmls.put("SBTemp", b.toString());
		b.delete(0, b.length());
	}
}
