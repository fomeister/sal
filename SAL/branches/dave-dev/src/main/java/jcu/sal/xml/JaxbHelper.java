
package jcu.sal.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.SchemaOutputResolver;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class JaxbHelper {

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

	public static String toXmlString(Object o) throws XmlException {
		try {
			if (o == null) {
				throw new XmlException("Cannot serialize null to an xml string.");
			}

			validate(o);
			prepareJAXB();

			StringWriter writer = new StringWriter();
			marshaller.marshal(o, new StreamResult(writer));
			return writer.toString();
		} catch (JAXBException je) {
			throw new JaxbException(je);
		}
	}

	public static Object fromXmlString(String xmlString) throws XmlException {
		try {
			if (xmlString == null) {
				throw new XmlException("Cannot deserialize null to an object.");
			}

			if (xmlString.length() == 0) {
				throw new XmlException("Cannot deserialize an empty string.");
			}

			validateString(xmlString);
			prepareJAXB();

			Object o = unmarshaller.unmarshal(new StreamSource(new StringReader(xmlString)));
			XsdHelper.validateString(getSchema(o.getClass()), xmlString);

			return o;
		} catch (JAXBException je) {
			throw new JaxbException(je);
		}
	}

	public static void toOutputStream(Object o, OutputStream os) throws XMLException {
		stringToOutputStream(toXmlString(o), os);
	}

	public static Object fromInputStream(InputStream is) throws XmlException {
		return fromXmlString(inputStreamToString(is));
	}

	private static void stringToOutputStream(String s, OutputStream os) throws XmlException {
	}

	private static String inputStreamToString(InputStream is) throws XmlException {
	}

	public static void toFile(Object o, File f) throws XmlException {
		stringToFile(toXmlString(o), f);
	}

	public static Object fromFile(File f) throws XmlException {
		return fromXmlString(fileToString(f));
	}

	private static stringToFile(String s, File f) throws XmlException {
	}

	private static fileToString(File f) throws XmlException {
	}

	public static void validate(Object o) throws ValidationException {
		Schema schema = getSchema(o.getClass());

		if (schema == null) {
			throw new ValidationException("Unable to find schema for class " +  o.getClass().getName());
		}

		validateString(schema, toXmlString(o));
	}

	public static void validateString(Schema schema, String xmlString) throws ValidationException {
		validateString(xmlString);
		XsdHelper.validateString(schema, xmlString);
	}

	public static void validateString(String xmlString) throws ValidationException {
		ErrorCollector ec = new ErrorCollector();

		try {
			XMLReader r = XMLReaderFactory.createXMLReader();
			r.setErrorHandler(ec);
			r.parse(new InputSource(new StringReader(xmlString)));
		} catch (SAXParseException se) {
		} catch (SAXException se) {
			throw new ValidationException("Validation failed: " + se.getMessage(), se);
		} catch (IOException ioe) {
			throw new ValidationException("Validation failed: " + ioe.getMessage(), ioe);
		}

		ec.throwErrors();
	}

	public static Schema getSchema(Class c) throws XmlException {
		try {
			JAXBContext tmpContext = JAXBContext.newInstance(c);
			SchemaCollector sc = new SchemaCollector();
			tmpContext.generateSchema(sc);
			return sc.getSchema();
		} catch (Exception e) {
			return new XmlException(e);
		}
	}

	private static class SchemaCollector extends SchemaOutputResolver {

		private StringWriter writer;

		public SchemaCollector() {
			writer = new StringWriter();
		}

		public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
			StreamResult sr = new StreamResult(writer);
			sr.setSystemId("memory");
			return sr;
		}

		public Schema getSchema() throws SAXException {
			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			return sf.newSchema(new StreamSource(new StringReader(writer.toString())));
		}
	}
}
