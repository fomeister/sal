package jcu.sal.Components.Protocols.v4l2;

import javax.naming.ConfigurationException;

import jcu.sal.Components.Protocols.CMLStore;


public class V4L2CML extends CMLStore {
	private static V4L2CML c;
	public static String V4L2CMLKey = "All";
	static {
		try {
			c = new V4L2CML();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} 
	}
	
	public static V4L2CML getStore() {
		return c;
	}
	
	private V4L2CML() throws ConfigurationException{
		StringBuffer b = new StringBuffer();
		CMLDoc c;
		
		/* 
		 * All famillies
		 * */
		addSensor(V4L2CMLKey);
//		generic 100 GetReading command
		b.append("<Command name=\"GetReading\">\n");
		b.append("\t<CID>100</CID>\n");
		b.append("\t<ShortDescription>Reads a single frame</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"1\">\n");
		b.append("\t\t<ReturnValue type=\"string\" quantity=\"none\" />\n");
		b.append("\t</returnValues>\n");
		b.append("</Command>\n");
		c = new CMLDoc(100, b.toString());
		addCML(V4L2CMLKey, c);
		b.delete(0, b.length());
//		101 startStream
		b.append("<Command name=\"startStream\">\n");
		b.append("\t<CID>101</CID>\n");
		b.append("\t<ShortDescription>Starts streaming frames from the device</ShortDescription>\n");
		b.append("\t<arguments count=\"1\">\n");
		b.append("\t\t<Argument type=\"StreamCallback\" />\n");
		b.append("\t</arguments>\n");
		b.append("\t<returnValues count=\"0\" />\n");
		b.append("</Command>\n");
		c = new CMLDoc(101, b.toString());
		addCML(V4L2CMLKey, c);
		b.delete(0, b.length());
//		102 startStream
		b.append("<Command name=\"stopStream\">\n");
		b.append("\t<CID>101</CID>\n");
		b.append("\t<ShortDescription>Stops streaming frames from the device</ShortDescription>\n");
		b.append("\t<arguments count=\"0\" />\n");
		b.append("\t<returnValues count=\"0\" />\n");
		b.append("</Command>\n");
		c = new CMLDoc(102, b.toString());
		addCML(V4L2CMLKey, c);
		b.delete(0, b.length());
	}
}
