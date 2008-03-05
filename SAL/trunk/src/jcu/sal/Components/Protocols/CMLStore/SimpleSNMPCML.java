package jcu.sal.Components.Protocols.CMLStore;

import java.util.Hashtable;

public class SimpleSNMPCML implements CMLStore{
	private static Hashtable<String, String> cmls = new Hashtable<String, String>();
	
	public String getCML(String f){
		return cmls.get("ALL");
	}
	
	public SimpleSNMPCML(){
		StringBuffer b = new StringBuffer();

		/* 
		 * ALL
		 * */
//		generic 100 GetReading command
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads the value of this sensor</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"string\" quantity=\"none\">\n");
		b.append("\t\t</ReturnValue>\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		cmls.put("ALL", b.toString());
		b.delete(0, b.length());
		}
}
