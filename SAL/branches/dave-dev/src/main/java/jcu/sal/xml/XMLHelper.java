
package jcu.sal.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.OutputStream;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.SchemaOutputResolver;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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

	public static void toOutputStream(Object o, OutputStream os) throws JAXBException {
		prepareJAXB();
		marshaller.marshal(o, os);
	}

	public static Object fromInputStream(InputStream is) throws JAXBException {
		prepareJAXB();
		return unmarshaller.unmarshal(is);
	}

	public static void toFile(Object o, File f) throws IOException, JAXBException {
		marshaller.marshal(o, new FileOutputStream(f));
	}

	public static Object fromFile(File f) throws IOException, JAXBException {
		return unmarshaller.unmarshal(f);
	}

	public static void validate(Object o) throws ValidationException, JAXBException {
		validateString(o.getClass(), toXmlString(o));
	}

	public static void validateString(Class c, String s) throws ValidationException {
		Schema schema = getSchemaForClass(c);
		if (schema == null) {
			throw new ValidationException("Unable to find the schema for class " + c.getName());
		}

		ErrorCollector ec = new ErrorCollector();

		XMLReader r = null;

		try {
			r = XMLReaderFactory.createXMLReader();
		} catch (SAXException se) {
			throw new ValidationException("Validation failed: " + se.getMessage(), se);
		}

		r.setErrorHandler(ec);

		Validator validator = schema.newValidator();
		validator.setErrorHandler(ec);

		try {
			r.parse(new InputSource(new StringReader(s)));
			validator.validate(new StreamSource(new StringReader(s)));
		} catch (SAXParseException se) {
		} catch (SAXException se) {
			throw new ValidationException("Validation failed: " + se.getMessage(), se);
		} catch (IOException ioe) {
			throw new ValidationException("Validation failed: " + ioe.getMessage(), ioe);
		}

		ec.throwErrors();
	}

	private static Schema getSchemaForClass(Class c) {
		try {
			JAXBContext tmpContext = JAXBContext.newInstance(c);
			SchemaCollector sc = new SchemaCollector();
			tmpContext.generateSchema(sc);
			return sc.getSchema();
		} catch (Exception e) {
			return null;
		}
	}

	private static class ErrorCollector implements ErrorHandler {

		private ArrayList<SAXParseException> errors;

		public ErrorCollector() {
			errors = new ArrayList<SAXParseException>();
		}

		public void throwErrors() throws ValidationException {
			if (errors.size() != 0) {
				throw new ValidationException(errors.toArray(new SAXParseException[0]));
			}
		}

		public void error(SAXParseException e) {
			errors.add(e);
		}

		public void fatalError(SAXParseException e) {
			errors.add(e);
		}

		public void warning(SAXParseException e) {
			errors.add(e);
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
