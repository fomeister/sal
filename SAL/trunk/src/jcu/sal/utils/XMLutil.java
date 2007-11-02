/**
 * 
 */
package jcu.sal.utils;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

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

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author gilles
 *
 */
public class XMLutil {
	static Logger logger = Logger.getLogger(XMLutil.class);
	
	/** The DOM document **/
	protected Document document;

	/*public static void main(String[] args) throws Exception{
    	XMLutil x = new XMLutil(new File("/home/gilles/workspace/SALv1/src/sensors.xml"));
    	NodeList l = x.getNodeList("//Sensor[LogicalPortID='1wtree']");
    	for(int i=0; i<l.getLength();i++) {
    		Source source = new DOMSource(l.item(i));
	        StringWriter writer = new StringWriter();
	        Result result = new StreamResult(writer);
	
	        try {
	            x.transform(source, result);
	        } catch (TransformerFactoryConfigurationError e) {
	            logger.error(
	                "Cannot build the String representation of the XML DOM Document");
	            e.printStackTrace();
	        } catch (TransformerException e) {
	            logger.error(
	                "Cannot build the String representation of the XML DOM Document");
	            e.printStackTrace();
	        }
	
	        StringBuffer buffer = writer.getBuffer();
	
	        System.out.println(buffer.toString());
    	}
    	
	}*/
	
	/**
     * Creates an empty DOM document
     * @return the empty DOM document
     */
    public XMLutil() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.newDocument();
        } catch (ParserConfigurationException e) {
            logger.error("Cannot build the XML DOM Document");
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a DOM document from an Input Stream
     * @param in the input stream
     * @return the DOM document
     */
    public XMLutil(InputStream in) {
    	try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(in);
	    } catch (Exception e) {
	        logger.error("Cannot build the XML DOM Document");
	        e.printStackTrace();
	    }
    }

    /**
     * Creates a DOM document from a file
     * @param f the file 
     * @return the DOM document
     */
    public XMLutil(File f) {
    	try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(f);
	    } catch (Exception e) {
	        logger.error("Cannot build the XML DOM Document");
	        e.printStackTrace();
	    }
    }
    
    /**
     * Creates a DOM document from an XML string
     * @param xmlstrthe XML string
     * @return the DOM document
     */   
    public XMLutil(String xmlstr) {
    	try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlstr)));
	    } catch (Exception e) {
	        logger.error("Cannot build the XML DOM Document");
	        e.printStackTrace();
	    }
    }

    /**
     * Adds a child to the parent node in the DOM document
     * @param parent the node for which to add the child
     * @param child the node to be added
     * @return the new added element
     */
    public Element addChild(Node parent, String child) {
        return this.addChild(parent, child, null);
    }

    /**
     * Adds a child containing text to the parent node inside the DOM document
     * @param parent the node for which to add the child
     * @param child the node to be added
     * @param value the text node to add to the child node
     * @return the new added element
     */
    public Element addChild(Node parent, String child, String value) {
        Element childElement = document.createElement(child);
        //childElement.setAttribute(name, value)
        
        if (value == null) {
            parent.appendChild(childElement);
        } else {
            parent.appendChild(childElement);
            addTextChild(childElement, value);
        }

        document.getDocumentElement().normalize();

        return childElement;
    }
    
    /**
     * Adds an attribute to a node
     * @param parent the node to which the attribue will be attached
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return the new added element
     */
    public void addAttribute(Node parent, String name, String value) {
    	Attr attrib = document.createAttribute(name);
    	attrib.setValue(value);
    	parent.getAttributes().setNamedItem(attrib);
    }

    /**
     * Adds text child to the parent node
     * @param parent the node for which to add the text node
     * @param value the next node to add to parent node
     */
    public void addTextChild(Node parent, String value) {
        Node textNode = document.createTextNode(value);
        parent.appendChild(textNode);
    }

    /**
     * Returns the root element of the XML DOM.
     * This method does not check if there are many root elements
     * @return the root element of the XML DOM.
     */
    public Element getRootElement() {
        return document.getDocumentElement();
    }

    /**
     * Returns the node corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression
     * @return the node corresponding to the given xpath expression or null if there is no
     * such node
     */
    public Node getNode(String xpath_expression) {
        Node node = null;
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            node = (Node) xpath.evaluate(xpath_expression, document,
                    XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            logger.error("Cannot evaluate XPATH expression: " +
                xpath_expression);
            e.printStackTrace();
        }

        return node;
    }
    
    /**
     * Returns the node set corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression
     * @return the node set corresponding to the given xpath expression or null if there is no
     * such node
     */
    public NodeList getNodeList(String xpath_expression) {
        NodeList nodelist = null;

        try {
            nodelist = XMLhelper.getNodeList(xpath_expression, document);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return nodelist;
    }

    /**
     * Returns the value of the node corresponding to the given xpath expression or null if there is no
     * such node.
     * @param xpath_expression
     * @return the value of the node corresponding to the given xpath expression or null if there is no
     * such node.
     */
    public String getTextValue(String xpath_expression) {
        String value = null;

        try {
        	value = XMLhelper.getTextValue(xpath_expression, document);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * Evaluates the given xpath expression.
     * Returns true if the given xpath expression can be found on this document. False if not.
     * This method is mainly used when we don't know if an element is present in the document.
     * @param xpath_expression the expression to evaluate
     * @return true if the exression can be evaluated, false if not.
     */
    public boolean evaluate(String xpath_expression) {
    	boolean b = false;
    	
    	try {
    		b = XMLhelper.evaluate(xpath_expression, document);
    	} catch (XPathExpressionException e) {
    		e.printStackTrace();
    	}
    	
    	return b;
    }

    /**
     * @see java.lang.Object#toString()
     * Returns a String representation of the DOM
     */
    public String toString() {
    	String s = null;
        try {
			s = XMLhelper.toString(document);
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
    }

}
