package jcu.sal.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jcu.sal.Components.EndPoints.EndPoint;
import jcu.sal.Components.Protocols.Protocol;

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
	
	/**
     * Creates an empty DOM document
     * @return the empty DOM document
	 * @throws ParserConfigurationException 
     */
    public static Document createEmptyDocument() throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        document.createElement("root");
        return document;
        
    }
    
    /**
     * Creates a DOM document from an Input Stream
     * @param in the input stream
     * @return 
     * @return the DOM document
     * @throws ParserConfigurationException 
     */
    public static Document createDocument(InputStream in) 
    	throws ParserConfigurationException {
    	Document document = null;
    	try {
	        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        document = builder.parse(in);
    	} catch (Exception e) {
    		throw new ParserConfigurationException();
    	}
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
    	throws ParserConfigurationException, IOException {
    	Document document = null;
    	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	try {
			document = builder.parse(f);
		} catch (SAXException e) {
			throw new ParserConfigurationException();
		}
        return document;
    }
    
    /**
     * Creates a DOM document from an XML string
     * @param xmlstrthe XML string
     * @return the DOM document
     * @throws ParserConfigurationException  
     */   
    public static Document createDocument(String xmlstr) 
    	throws ParserConfigurationException {
    	Document document = null;
    	try {
	        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        document = builder.parse(new InputSource(new StringReader(xmlstr)));
    	} catch (Exception e) {
    		throw new ParserConfigurationException();
    	}
        return document;
    }
    
    /**
     * Creates a DOM document from a Node
     * @param node the XML node
     * @return the DOM document
     * @throws ParserConfigurationException 
     */   
    public static Document createDocument(Node node) 
    	throws ParserConfigurationException {
    	Document d = XMLhelper.createEmptyDocument();
    	try { XMLhelper.transform(new DOMSource(node), new DOMResult(d)); }
    	catch (Exception e) { throw new ParserConfigurationException(); }
        return d;
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
     * @param newdoc if set to true, the resulting node will be duplicated and put in its own document
     * @return the node corresponding to the given xpath expression or null if there is no
     * such node
     * @throws XPathExpressionException 
     * @throws ParserConfigurationException 
     * @throws DOMException 
     */
    public static Node getNode(String xpath_expression, Document doc, boolean newdoc) throws XPathExpressionException, ParserConfigurationException {
        Node node = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        node = (Node) xpath.evaluate(xpath_expression, doc, XPathConstants.NODE);
        if(newdoc) {
        	Document d =createEmptyDocument();
        	Element r = d.getDocumentElement();
        	Node n = d.importNode(node,true);
        	r.appendChild(n);
        	
        	
        }

       	return node;
    }
    
    /**
     * Returns the node corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression the XPATH expression
     * @param n the node to search
     * @param newdoc if set to true, the resulting node will be duplicated and put in its own document
     * @return the node corresponding to the given xpath expression or null if there is no
     * such node
     * @throws XPathExpressionException 
     * @throws ParserConfigurationException 
     * @throws DOMException 
     */
    public static Node getNode(String xpath_expression, Node n, boolean newdoc) throws XPathExpressionException, ParserConfigurationException {
        Node node = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        node = (Node) xpath.evaluate(xpath_expression, n, XPathConstants.NODE);
        if (newdoc) {
        	Document d =createDocument("<SAL></SAL>"); 
        	node = d.importNode(node,true);
        	d.getDocumentElement().appendChild(node);
        }


        return node;
    }
    
    /**
     * Returns a new document corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the document corresponding to the given xpath expression or null if there is no
     * such node
     * @throws XPathExpressionException 
     */
    public static Document getSubDocument(String xpath_expression, Document doc) throws XPathExpressionException {
        Node node = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        node = (Node) xpath.evaluate(xpath_expression, doc, XPathConstants.NODE);
        Document d;
		try {
			d = createDocument(node);
		} catch (ParserConfigurationException e) {
			throw new XPathExpressionException("");
		}
        return d;
    }

    /**
     * Returns the combined attributes of multiple elements retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the attributes and their values in a ArrayList: 0:1st Attr name, 1:1st attr value, 2:2nd attr name,3:2nd attr value...
     * @throws XPathExpressionException 
     */
    public static ArrayList<String> getAttributeListFromElements(String xpath_expression, Document doc) throws XPathExpressionException {
    	ArrayList<String> table = new ArrayList<String>();
    	
    	NodeList list = getNodeList(xpath_expression, doc);
		for(int i = 0; i < list.getLength(); i++) {
			NamedNodeMap nnp = list.item(i).getAttributes();
			for (int j = 0; j < nnp.getLength(); j++) { 
				table.add(nnp.item(j).getNodeName());
				table.add(nnp.item(j).getNodeValue());
			}
		}
		return table;	
    }
    
    /**
     * Returns the combined attributes of multiple elements retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param n the node to be searched
     * @return the attributes and their values in a ArrayList: 0:1st Attr name, 1:1st attr value, 2:2nd attr name,3:2nd attr value...
     * @throws XPathExpressionException 
     */
    public static ArrayList<String> getAttributeListFromElements(String xpath_expression, Node n) throws XPathExpressionException {
    	ArrayList<String> table = new ArrayList<String>();
    	
    	NodeList list = getNodeList(xpath_expression, n);
		for(int i = 0; i < list.getLength(); i++) {
			NamedNodeMap nnp = list.item(i).getAttributes();
			for (int j = 0; j < nnp.getLength(); j++) { 
				table.add(nnp.item(j).getNodeName());
				table.add(nnp.item(j).getNodeValue());
			}
		}
		return table;	
    }    
    
    /**
     * Returns the attributes of a single element retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the attributes and their values in a ArrayList: 0:1st Attr name, 1:1st attr value, 2:2nd attr name,3:2nd attr value...  
     * @throws XPathExpressionException 
     * @throws ParserConfigurationException 
     * @throws DOMException 
     */
    public static ArrayList getAttributeListFromElement(String xpath_expression, Document doc) throws XPathExpressionException, DOMException, ParserConfigurationException {
    	ArrayList<String> table = new ArrayList<String>();
    	
		Node node = getNode(xpath_expression, doc, false);
		NamedNodeMap nnp = node.getAttributes();
		for (int i = 0; i < nnp.getLength(); i++) {
			table.add(nnp.item(i).getNodeName());
			table.add(nnp.item(i).getNodeValue());
		}
		
		return table;	
    }
    
    /**
     * Returns the attributes of a single element retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param n the node to be search
     * @return the attributes and their values in a ArrayList: 0:1st Attr name, 1:1st attr value, 2:2nd attr name,3:2nd attr value...  
     * @throws XPathExpressionException 
     * @throws ParserConfigurationException 
     */
    public static ArrayList getAttributeListFromElement(String xpath_expression, Node n) throws XPathExpressionException, ParserConfigurationException {
    	ArrayList<String> table = new ArrayList<String>();
    	
		Node node = getNode(xpath_expression, n, false);
		NamedNodeMap nnp = node.getAttributes();
		for (int i = 0; i < nnp.getLength(); i++) {
			table.add(nnp.item(i).getNodeName());
			table.add(nnp.item(i).getNodeValue());
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
     * @throws ParserConfigurationException 
     * @throws DOMException 
     */
    public static String getAttributeFromName(String xpath_expression, String attr_name, Document doc) throws XPathExpressionException, DOMException, ParserConfigurationException {
        return getNode(xpath_expression, doc, false).getAttributes().getNamedItem(attr_name).getNodeValue();
    }
    
    /**
     * Returns an element's attribute using an XPATH query and the attribute's name 
     * @param xpath_expression the XPATH expression
     * @param attr_name the name of the attribute whose value is to be returned
     * @param doc the DOM document
     * @return the value of the attribute
     * @throws XPathExpressionException 
     * @throws ParserConfigurationException 
     * @throws DOMException 
     */
    public static String getAttributeFromName(String xpath_expression, String attr_name, Node n) throws XPathExpressionException, DOMException, ParserConfigurationException {
        return getNode(xpath_expression, n, false).getAttributes().getNamedItem(attr_name).getNodeValue();
    }
    
    /**
     * Returns the node set corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression the XPATH expression
     * @param n the ndoe to be searched
     * @return the node set corresponding to the given xpath expression or null if there is no
     * such node
     * @throws XPathExpressionException 
     */
    public static NodeList getNodeList(String xpath_expression, Node n)
    throws XPathExpressionException {
        NodeList nodelist = null;
        XPath xpath = XPathFactory.newInstance().newXPath();

        nodelist = (NodeList) xpath.evaluate(xpath_expression, n, XPathConstants.NODESET);

        return nodelist;
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
     * @Returns a String representation of the DOM
     */
    public static String toString(Document doc) {
        Source source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);

        Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		} catch (TransformerException e) {
			System.out.println("Cant toString() this DOM doc !");
		}
        
        StringBuffer buffer = writer.getBuffer();

        return buffer.toString();
    }
    
    /**
     * @see java.lang.Object#toString()
     * @param n the node
     * @throws TransformerFactoryConfigurationError 
     * @Returns a String representation of the DOM
     */
    public static String toString(Node n) {
        Source source = new DOMSource(n);
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);

        Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		} catch (TransformerException e) {
			System.out.println("Cant toString() this DOM doc !");
		}
        
        StringBuffer buffer = writer.getBuffer();

        return buffer.toString();
    }

    /**
     * This method is used to generate a result from a source.
     * @param source
     * @param result
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static void transform(Source source, Result result)
        throws TransformerFactoryConfigurationError, TransformerException {
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
    }
    
    public static void main(String[] args) 
	throws ParserConfigurationException {
		Document d;
		try {
			d = XMLhelper.createDocument(new File("/home/gilles/workspace/SALv1/src/sensors1.xml"));
			Node n = XMLhelper.getNode("//Protocol", d, true);
			System.out.println("Found node : " + toString(n));
			n = XMLhelper.getNode("/" + Protocol.PROTOCOL_TAG + "/" + EndPoint.ENPOINT_TAG, n, true);
			System.out.println("Found node : " + toString(n));
			/*NodeList nl = XMLhelper.getNodeList("//Protocol", d);
			d = XMLhelper.createDocument(nl);
			System.out.println(toString(d));*/
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			throw new ParserConfigurationException();
		}
		
		
	}
}
