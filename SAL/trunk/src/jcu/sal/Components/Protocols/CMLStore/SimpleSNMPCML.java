package jcu.sal.Components.Protocols.CMLStore;

import javax.naming.ConfigurationException;


public class SimpleSNMPCML extends CMLStore{

	public SimpleSNMPCML() throws ConfigurationException{
		StringBuffer b = new StringBuffer();
		CMLDoc c;

		/* 
		 * ALL
		 * */
		addSensor("ALL");
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
		c = new CMLDoc(100, b.toString());
		addCML("ALL", c);
		b.delete(0, b.length());
		}
}
