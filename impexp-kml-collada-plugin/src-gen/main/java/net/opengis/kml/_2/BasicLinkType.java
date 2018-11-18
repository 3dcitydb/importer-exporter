//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BasicLinkType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BasicLinkType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}href" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}BasicLinkSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}BasicLinkObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BasicLinkType", propOrder = {
    "href",
    "basicLinkSimpleExtensionGroup",
    "basicLinkObjectExtensionGroup"
})
@XmlSeeAlso({
    LinkType.class
})
public class BasicLinkType
    extends AbstractObjectType
{

    protected String href;
    @XmlElement(name = "BasicLinkSimpleExtensionGroup")
    protected List<Object> basicLinkSimpleExtensionGroup;
    @XmlElement(name = "BasicLinkObjectExtensionGroup")
    protected List<AbstractObjectType> basicLinkObjectExtensionGroup;

    /**
     * Ruft den Wert der href-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Legt den Wert der href-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    public boolean isSetHref() {
        return (this.href!= null);
    }

    /**
     * Gets the value of the basicLinkSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the basicLinkSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBasicLinkSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getBasicLinkSimpleExtensionGroup() {
        if (basicLinkSimpleExtensionGroup == null) {
            basicLinkSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.basicLinkSimpleExtensionGroup;
    }

    public boolean isSetBasicLinkSimpleExtensionGroup() {
        return ((this.basicLinkSimpleExtensionGroup!= null)&&(!this.basicLinkSimpleExtensionGroup.isEmpty()));
    }

    public void unsetBasicLinkSimpleExtensionGroup() {
        this.basicLinkSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the basicLinkObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the basicLinkObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBasicLinkObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getBasicLinkObjectExtensionGroup() {
        if (basicLinkObjectExtensionGroup == null) {
            basicLinkObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.basicLinkObjectExtensionGroup;
    }

    public boolean isSetBasicLinkObjectExtensionGroup() {
        return ((this.basicLinkObjectExtensionGroup!= null)&&(!this.basicLinkObjectExtensionGroup.isEmpty()));
    }

    public void unsetBasicLinkObjectExtensionGroup() {
        this.basicLinkObjectExtensionGroup = null;
    }

    public void setBasicLinkSimpleExtensionGroup(List<Object> value) {
        this.basicLinkSimpleExtensionGroup = value;
    }

    public void setBasicLinkObjectExtensionGroup(List<AbstractObjectType> value) {
        this.basicLinkObjectExtensionGroup = value;
    }

}
