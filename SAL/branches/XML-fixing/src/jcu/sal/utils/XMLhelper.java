package jcu.sal.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

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
     * Duplicates a document and put it in its own document
     * @param d the XML document to be duplicated
     * @return the document in its new document
     * @throws ParserConfigurationException 
     */   
    public static Document duplicateDocument(Document d) 
    	throws ParserConfigurationException {
    	Document tmp = createDocument(toString(d));
    	return tmp;
    }
    
    /**
     * Duplicates a node and put it in its own document
     * @param node the XML node to be duplicated
     * @return the node (in its new document)
     * @throws ParserConfigurationException 
     */   
    public static Node duplicateNode(Node node) 
    	throws ParserConfigurationException {
    	Document d = createEmptyDocument();
    	return d.appendChild(d.importNode(node,true));
    }
    
    /**
     * Duplicates a node and put it in its own document
     * @param node the XML node to be duplicated
     * @return the node (in its new document)
     * @throws ParserConfigurationException 
     */   
    public static Document isolateNode(Node node) 
    	throws ParserConfigurationException {
    	Document d = createEmptyDocument();
    	d.appendChild(d.importNode(node,true));
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
     * Adds a child node to a parent node. The child node is given as a string, imported in the parent's document as a node,
     * and appended to the parent node 
     * @param parent the parent node
     * @param xml the child node
     * @return the new appended node
     * @throws ParserConfigurationException if the child node (the xml string) can not be parsed
     */
    public static Node addChild(Node parent, String xml) throws ParserConfigurationException {
		Document d = XMLhelper.createDocument(xml);
		return addChild(parent, d.getFirstChild());
    }
    
    /**
     * Adds a child containing text to the parent node inside the DOM document
     * @param parent the node under which to add the child
     * @param child the node to be added
     * @return the new appended node
     */
    public static Node addChild(Node parent, Node child) {
    	Document d = parent.getOwnerDocument();
    	if(child.getNodeType()==Node.DOCUMENT_NODE) child=child.getFirstChild();
    	Node n = d.importNode(child, true);
        return parent.appendChild(n);
    }
    
    /**
     * Adds an attribute to a node
     * @param parent the node to which the attribue will be attached
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return the new added element
     */
    public static void addAttribute(Node parent, String name, String value) {
    	Attr attrib = parent.getOwnerDocument().createAttribute(name);
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
     * Removes a node from its document
     * @param n the node to be removed
     */
    public static void deleteNode(Node n) {
    	n.getParentNode().removeChild(n);
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
        if(newdoc && node!=null)
        	node = duplicateNode(node);

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
        if(newdoc && node!=null)
        	node = duplicateNode(node);
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
        Document d = null;
		try {
			if(node!=null)
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
    	if(list!=null) {
			for(int i = 0; i < list.getLength(); i++) {
				NamedNodeMap nnp = list.item(i).getAttributes();
				for (int j = 0; j < nnp.getLength(); j++) { 
					table.add(nnp.item(j).getNodeName());
					table.add(nnp.item(j).getNodeValue());
				}
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
    public static List<String> getAttributeListFromElements(String xpath_expression, Node n) throws XPathExpressionException {
    	ArrayList<String> table = new ArrayList<String>();
    	
    	NodeList list = getNodeList(xpath_expression, n);
    	if(list!=null) {
			for(int i = 0; i < list.getLength(); i++) {
				NamedNodeMap nnp = list.item(i).getAttributes();
				for (int j = 0; j < nnp.getLength(); j++) { 
					table.add(nnp.item(j).getNodeName());
					table.add(nnp.item(j).getNodeValue());
				}
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
    public static List<String> getAttributeListFromElement(String xpath_expression, Document doc) throws XPathExpressionException, DOMException, ParserConfigurationException {
    	ArrayList<String> table = new ArrayList<String>();
    	
		Node node = getNode(xpath_expression, doc, false);
    	if(node!=null) {
			NamedNodeMap nnp = node.getAttributes();
			for (int i = 0; i < nnp.getLength(); i++) {
				table.add(nnp.item(i).getNodeName());
				table.add(nnp.item(i).getNodeValue());
			}
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
    public static ArrayList<String> getAttributeListFromElement(String xpath_expression, Node n) throws XPathExpressionException, ParserConfigurationException {
    	ArrayList<String> table = new ArrayList<String>();
    	
		Node node = getNode(xpath_expression, n, false);
    	if(node!=null) {
			NamedNodeMap nnp = node.getAttributes();
			for (int i = 0; i < nnp.getLength(); i++) {
				table.add(nnp.item(i).getNodeName());
				table.add(nnp.item(i).getNodeValue());
			}
    	}
		return table;	
    }
    
    /**
     * Returns a node's attribute using its name
     * @param attr_name the name of the attribute whose value is to be returned
     * @param n the node
     * @return the value from the attribute
     * @throws XPathExpressionException if there is an error in the XPATH expression, or the attribute cannot be found
     */
    public static String getAttributeFromName(String attr_name, Node n) throws XPathExpressionException {
    	NamedNodeMap nm;
    	Node node;
    	
    	if((nm = n.getAttributes()) !=null)
    		if((node = nm.getNamedItem(attr_name))!=null)
    			return node.getNodeValue();
    	
    	throw new XPathExpressionException("Cannot find attribute "+attr_name);
    }
    
    /**
     * Returns an element's attribute using an XPATH query and the attribute's name 
     * @param xpath_expression the XPATH expression
     * @param attr_name the name of the attribute whose value is to be returned
     * @param doc the DOM document
     * @return the value of the attribute
     * @throws XPathExpressionException if there is an error in the XPATH expression, or the attribute cannot be found
     * @throws ParserConfigurationException if there is an error in the XPATH expression
     * @throws DOMException if there is an error in the XPATH expression
     */
    public static String getAttributeFromName(String xpath_expression, String attr_name, Document doc) throws XPathExpressionException, DOMException, ParserConfigurationException {
    	Node n1,n2;
    	NamedNodeMap nm;
    	if((n1=getNode(xpath_expression, doc, false))!=null)
    		if((nm=n1.getAttributes())!=null)
    			if((n2=nm.getNamedItem(attr_name))!=null)
    				return n2.getNodeValue();
    	throw new XPathExpressionException("Cannot find attribute "+attr_name);
    }
    
    /**
     * Returns an element's attribute using an XPATH query and the attribute's name 
     * @param xpath_expression the XPATH expression
     * @param attr_name the name of the attribute whose value is to be returned
     * @param n the node
     * @return the value of the attribute
     * @throws XPathExpressionException if there is an error in the XPATH expression, or the attribute cannot be found 
     * @throws ParserConfigurationException if there is an error in the XPATH expression
     * @throws DOMException if there is an error in the XPATH expression
     */
    public static String getAttributeFromName(String xpath_expression, String attr_name, Node n) throws XPathExpressionException, DOMException, ParserConfigurationException {
    	Node n1,n2;
    	NamedNodeMap nm;
    	if((n1=getNode(xpath_expression, n, false))!=null)
    		if((nm=n1.getAttributes())!=null)
    			if((n2=nm.getNamedItem(attr_name))!=null)
    				return n2.getNodeValue();
    	throw new XPathExpressionException("Cannot find attribute "+attr_name);
    }
    
    /**
     * Set an element's existing attribute using an XPATH query and the attribute's name 
     * @param xpath_expression the XPATH expression
     * @param attr_name the name of the attribute whose value is to be set
     * @param val the value to be set for this attribute
     * @param n the node
     * @return the value of the attribute
     * @throws XPathExpressionException if there is an error in the XPATH expression, or the attribute cannot be found 
     * @throws ParserConfigurationException if there is an error in the XPATH expression
     * @throws DOMException if there is an error in the XPATH expression
     */
    public static void setAttributeFromName(String xpath_expression, String attr_name, String val, Node n) throws XPathExpressionException, DOMException, ParserConfigurationException {
    	Node n1,n2;
    	NamedNodeMap nm;
    	if((n1=getNode(xpath_expression, n, false))!=null)
    		if((nm=n1.getAttributes())!=null)
    			if((n2=nm.getNamedItem(attr_name))!=null) {
    				n2.setNodeValue(val);
    				return;
    			}
    	throw new XPathExpressionException("Cannot find attribute "+attr_name);
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
     * @param doc the document
     * @return the value of the node corresponding to the given xpath expression or null if there is no
     * such node.
     * @throws XPathExpressionException 
     */
    public static String getTextValue(String xpath_expression, Document doc)
    throws XPathExpressionException {
        if(evaluate(xpath_expression, doc)==true)
        	return (String) XPathFactory.newInstance().newXPath().evaluate(xpath_expression, doc, XPathConstants.STRING);
        else
        	throw new XPathExpressionException("The xpath expression doesnt resolve to anything");
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
    	if(doc==null) return "";
    	
    	StringWriter writer = new StringWriter();
    	OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(4);
        XMLSerializer serializer = new XMLSerializer(writer, format);
        try {
			serializer.serialize(doc);
		} catch (IOException e) {
			System.out.println("Cant toString() this DOM doc !");;
			e.printStackTrace();
		}
        return writer.getBuffer().toString();
    }
    
    /**
     * @see java.lang.Object#toString()
     * @param n the node
     * @throws TransformerFactoryConfigurationError 
     * @Returns a String representation of the DOM
     */
    public static String toString(Node n) {
    	if(n==null) return "";
    	try {
			return toString(createDocument(n));
		} catch (ParserConfigurationException e) {
			System.out.println("Cant toString() this DOM doc !");;
			e.printStackTrace();		
			return "";
		}
    }
    
    /**
     * This method takes an XML document as a string and formats it nicely
     * @param n the XML string
     * @throws TransformerFactoryConfigurationError 
     * @Returns a nicely formatted XML document as a string
     */
    public static String toString(String n) {
    	if(n==null) return "";
    	try {
			return toString(createDocument(n));
		} catch (ParserConfigurationException e) {
			System.out.println("Cant toString() this DOM doc !");;
			e.printStackTrace();		
			return "";
		}
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
	throws ParserConfigurationException, XPathExpressionException {
//		Document d;
//		String xpath = "//parameters[Param[@name=\""+Sensor.SENSORADDRESSATTRIBUTE_TAG+"\" and @value=\"10.1C5AC9000800\"] and "
//		+"Param[@name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\" and @value=\"1wtree\"]]/parent::*";
//		//String xpath = "//Param[@name=\""+Sensor.SENSORADDRESSATTRIBUTE_TAG+"\" and @value=\"10.1C5AC9000800\"]/parent::*/parent::* and //Param[@name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\" and @value=\"1wtree\"]/parent::*/parent::*";
//		//String xpath = "//Param[@name=\""+Sensor.SENSORADDRESSATTRIBUTE_TAG+"\" and @value=\"10.1C5AC9000800\"]/parent::*/parent::* and //Param[@name=\""+Sensor.PROTOCOLATTRIBUTE_TAG+"\" and @value=\"1wtree\"]/parent::*/parent::*";
//		try {
//			d = XMLhelper.createDocument(new File("/home/gilles/workspace/SAL/src/sensors.xml"));
//			System.out.println("Xpath: "+xpath);
//			System.out.println("Result:");
//			
//			NodeList nl = XMLhelper.getNodeList(xpath, d);
//			for (int i = 0; i < nl.getLength(); i++) {
//				System.out.println(XMLhelper.toString(nl.item(i)));
//			}
//			Node n = XMLhelper.getNode("//Sensor[@sid=\"1\"]", d, false);
//			XMLhelper.setAttributeFromName("//Sensor[@sid=\"1\"]", "sid", "ABCDEF", d);
//			System.out.println(XMLhelper.toString(n));
//			
//		} catch (Exception e) {
//			System.out.println(e.toString());
//			e.printStackTrace();
//			throw new ParserConfigurationException();
//		}
//		
		String s = "<Sensor sid=\"15\">\t\t<parameters>      <Param name=\"ProtocolName\" value=\"osData\" />\t"
				+"<Param name=\"Address\" value=\"NiceTime\" >test\n"
				+"<subParam1>test</subParam1>"
				+"<subParam2><subsubParam1/>test</subParam2></Param>"
				+"<Param name=\"SamplingInterval\" value=\"30\" />"
				+"</parameters></Sensor>";
		
		Document d = XMLhelper.createDocument(s);
		System.out.println(XMLhelper.toString(d));
		System.out.println("nb param tags: " + XMLhelper.getTextValue("count(//Param)", d));
		System.out.println(XMLhelper.toString(XMLhelper.getNode("/Sensor/parameters/Param[@name=\"Address\"]/*", d, true)));
		//System.out.println("param1: " + XMLhelper.getTextValue("/Sensor/parameters/Param[@name=\"Address\"]/*", d));
    	
	}
}
