
package jcu.sal.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

public class XMLHelper {

	private static final String COMMAND_PACKAGE = "jcu.sal.xml";

	private static JAXBContext context = null;
	private static Marshaller marshaller = null;
	private static Unmarshaller unmarshaller = null;

	private static void prepareJAXB() throws JAXBException {
		if (context == null) {
			context = JAXBContext.newInstance(COMMAND_PACKAGE);
			marshaller = context.createMarshaller();
			unmarshaller = context.createUnmarshaller();
		}
	}

	public static String toXmlString(Object o) throws JAXBException {
		prepareJAXB();
		StringWriter writer = new StringWriter();
		marshaller.marshal(o, new StreamResult(writer));
		return writer.toString();
	}

	public static Object fromXmlString(String string) throws JAXBException {
		prepareJAXB();
		return unmarshaller.unmarshal(new StreamSource(new StringReader(string)));
	}

	public static void toFile(Object o, File f) throws IOException, JAXBException {
		marshaller.marshal(o, new FileOutputStream(f));
	}

	public static Object fromFile(File f) throws IOException, JAXBException {
		return unmarshaller.unmarshal(f);
	}
}
