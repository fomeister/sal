//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.03.16 at 02:56:25 PM EST 
//


package jcu.sal.config.plugins.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HelperNameClassType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HelperNameClassType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HelperName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="class" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HelperNameClassType", propOrder = {
    "helperName",
    "clazz"
})
public class HelperNameClassType {

    @XmlElement(name = "HelperName", required = true)
    protected String helperName;
    @XmlElement(name = "class", required = true)
    protected String clazz;

    /**
     * Gets the value of the helperName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHelperName() {
        return helperName;
    }

    /**
     * Sets the value of the helperName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHelperName(String value) {
        this.helperName = value;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

}
