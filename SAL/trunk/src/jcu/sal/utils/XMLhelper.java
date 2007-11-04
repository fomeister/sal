package jcu.sal.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

	/**
	 * Set of methods to manipulate XML documents
	 * @author gilles
	 * 
	 */

public class XMLhelper {
	
	public static void main(String[] args) 
	throws ParserConfigurationException, SAXException, IOException,
			DOMException, XPathExpressionException {
		//Document d = XMLhelper.createDocument("<EndPoint name='usb' type='usb' />");
		Document d = XMLhelper.createDocument(new File("/home/gilles/workspace/SALv1/src/sensors.xml"));
		XMLhelper.getAttributeListFromElements("//EndPoint[@name='serial0']/parameters/Param", d);
		
	}
	
	/**
     * Creates an empty DOM document
     * @return the empty DOM document
	 * @throws ParserConfigurationException 
     */
    public static Document createEmptyDocument() throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        return document;
        
    }
    
    /**
     * Creates a DOM document from an Input Stream
     * @param in the input stream
     * @return 
     * @return the DOM document
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public static Document createDocument(InputStream in) 
    	throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(in);
        return document;
    }

    /**
     * Creates a DOM document from a file
     * @param f the file 
     * @return 
     * @return the DOM document
     * @throws ParserConfigurationException 
     * @throws IOException
     * @throws SAXException
     */
    public static Document createDocument(File f) 
    	throws ParserConfigurationException, SAXException, IOException {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(f);
            return document;
    }
    
    /**
     * Creates a DOM document from an XML string
     * @param xmlstrthe XML string
     * @return the DOM document
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */   
    public static Document createDocument(String xmlstr) 
    	throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlstr)));
        return document;
    }

    /**
     * Adds a child to the parent node in the DOM document
     * @param parent the node for which to add the child
     * @param child the node to be added
     * @param doc the DOM document
     * @return the new added element
     */
    public static Element addChild(Node parent, String child, Document doc) {
        return addChild(parent, child, null, doc);
    }

    /**
     * Adds a child containing text to the parent node inside the DOM document
     * @param parent the node for which to add the child
     * @param child the node to be added
     * @param value the text node to add to the child node
     * @param doc the DOM document
     * @return the new added element
     */
    public static Element addChild(Node parent, String child, String value, Document doc) {
        Element childElement = doc.createElement(child);
        
        if (value == null) {
            parent.appendChild(childElement);
        } else {
            parent.appendChild(childElement);
            addTextChild(childElement, value, doc);
        }

        doc.getDocumentElement().normalize();

        return childElement;
    }
    
    /**
     * Adds an attribute to a node
     * @param parent the node to which the attribue will be attached
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @param doc the DOM document
     * @return the new added element
     */
    public static void addAttribute(Node parent, String name, String value, Document doc) {
    	Attr attrib = doc.createAttribute(name);
    	attrib.setValue(value);
    	parent.getAttributes().setNamedItem(attrib);
    }

    /**
     * Adds text child to the parent node
     * @param parent the node for which to add the text node
     * @param value the next node to add to parent node
     * @param doc the DOM document
     */
    public static void addTextChild(Node parent, String value, Document doc) {
        Node textNode = doc.createTextNode(value);
        parent.appendChild(textNode);
    }

    /**
     * Returns the root element of the XML DOM.
     * This method does not check if there are many root elements
     * @param doc the DOM document
     * @return the root element of the XML DOM.
     */
    public static Element getRootElement(Document doc) {
        return doc.getDocumentElement();
    }

    /**
     * Returns the node corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the node corresponding to the given xpath expression or null if there is no
     * such node
     * @throws XPathExpressionException 
     */
    public static Node getNode(String xpath_expression, Document doc) throws XPathExpressionException {
        Node node = null;
        XPath xpath = XPathFactory.newInstance().newXPath();

        node = (Node) xpath.evaluate(xpath_expression, doc, XPathConstants.NODE);

        return node;
    }

    /**
     * Returns the combined attributes of multiple elements retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the attributes and their values in a hashtable
     * @throws XPathExpressionException 
     */
    public static Hashtable<String,String> getAttributeListFromElements(String xpath_expression, Document doc) throws XPathExpressionException {
    	Hashtable<String, String> table = new Hashtable<String, String>();
    	
    	NodeList list = getNodeList(xpath_expression, doc);
		for(int i = 0; i < list.getLength(); i++) {
			NamedNodeMap nnp = list.item(i).getAttributes();
			for (int j = 0; j < nnp.getLength(); j++) 
				table.put(nnp.item(j).getNodeName(), nnp.item(j).getNodeValue());
		}
		return table;	
    }
    
    /**
     * Returns the attributes of a single element retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the attributes and their values in a hashtable
     * @throws XPathExpressionException 
     */
    public static Hashtable<String,String> getAttributeListFromElement(String xpath_expression, Document doc) throws XPathExpressionException {
    	Hashtable<String, String> table = new Hashtable<String, String>();
    	
		Node node = getNode(xpath_expression, doc);
		NamedNodeMap nnp = node.getAttributes();
		for (int i = 0; i < nnp.getLength(); i++) {
			table.put(nnp.item(i).getNodeName(), nnp.item(i).getNodeValue());
		}
		
		return table;	
    }
    
    /**
     * Returns a node's attribute using its name
     * @param attr_name the name of the attribute whose value is to be returned
     * @param n the node
     * @return the value from the attribute
     * @throws XPathExpressionException 
     */
    public static String getAttributeFromName(String attr_name, Node n) throws XPathExpressionException {
        return n.getAttributes().getNamedItem(attr_name).getNodeValue();
    }
    
    /**
     * Returns an element's attribute using an XPATH query and the attribute's name 
     * @param xpath_expression the XPATH expression
     * @param attr_name the name of the attribute whose value is to be returned
     * @param doc the DOM document
     * @return the value of the attribute
     * @throws XPathExpressionException 
     */
    public static String getAttributeFromName(String xpath_expression, String attr_name, Document doc) throws XPathExpressionException {
        return getNode(xpath_expression, doc).getAttributes().getNamedItem(attr_name).getNodeValue();
    }
    
    /**
     * Returns the node set corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the node set corresponding to the given xpath expression or null if there is no
     * such node
     * @throws XPathExpressionException 
     */
    public static NodeList getNodeList(String xpath_expression, Document doc)
    throws XPathExpressionException {
        NodeList nodelist = null;
        XPath xpath = XPathFactory.newInstance().newXPath();

        nodelist = (NodeList) xpath.evaluate(xpath_expression, doc, XPathConstants.NODESET);

        return nodelist;
    }

    /**
     * Returns the value of the node corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression the XPATH expression
     * @return the value of the node corresponding to the given xpath expression or null if there is no
     * such node.
     * @throws XPathExpressionException 
     */
    public static String getTextValue(String xpath_expression, Document doc)
    throws XPathExpressionException {
        String value = null;
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        value = (String) xpath.evaluate(xpath_expression, doc, XPathConstants.STRING);

        return value;
    }

    /**
     * Evaluates the given xpath expression.
     * Returns true if the given xpath expression can be found on this document. False if not.
     * This method is mainly used when we don't know if an element is present in the document.
     * @param xpath_expression the expression to evaluate
     * @param doc the DOM document
     * @return true if the exression can be evaluated, false if not.
     * @throws XPathExpressionException 
     */
    public static boolean evaluate(String xpath_expression, Document doc) 
    	throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        String result = (String) xpath.evaluate(xpath_expression, doc, XPathConstants.STRING);

        if (result.length() == 0)
        	return false;

        return true;
    }

    /**
     * @see java.lang.Object#toString()
     * @param doc the DOM document
     * @throws TransformerFactoryConfigurationError 
     * @throws TransformerException 
     * @Returns a String representation of the DOM
     */
    public static String toString(Document doc)
    	throws TransformerFactoryConfigurationError, TransformerException {
        Source source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
        StringBuffer buffer = writer.getBuffer();

        return buffer.toString();
    }


}
