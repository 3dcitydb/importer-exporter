//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für PairType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PairType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}key" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}styleUrl" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractStyleSelectorGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PairSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PairObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PairType", propOrder = {
    "key",
    "styleUrl",
    "abstractStyleSelectorGroup",
    "pairSimpleExtensionGroup",
    "pairObjectExtensionGroup"
})
public class PairType
    extends AbstractObjectType
{

    @XmlElement(defaultValue = "normal")
    @XmlSchemaType(name = "string")
    protected StyleStateEnumType key;
    @XmlSchemaType(name = "anyURI")
    protected String styleUrl;
    @XmlElementRef(name = "AbstractStyleSelectorGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractStyleSelectorType> abstractStyleSelectorGroup;
    @XmlElement(name = "PairSimpleExtensionGroup")
    protected List<Object> pairSimpleExtensionGroup;
    @XmlElement(name = "PairObjectExtensionGroup")
    protected List<AbstractObjectType> pairObjectExtensionGroup;

    /**
     * Ruft den Wert der key-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link StyleStateEnumType }
     *     
     */
    public StyleStateEnumType getKey() {
        return key;
    }

    /**
     * Legt den Wert der key-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link StyleStateEnumType }
     *     
     */
    public void setKey(StyleStateEnumType value) {
        this.key = value;
    }

    public boolean isSetKey() {
        return (this.key!= null);
    }

    /**
     * Ruft den Wert der styleUrl-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyleUrl() {
        return styleUrl;
    }

    /**
     * Legt den Wert der styleUrl-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyleUrl(String value) {
        this.styleUrl = value;
    }

    public boolean isSetStyleUrl() {
        return (this.styleUrl!= null);
    }

    /**
     * Ruft den Wert der abstractStyleSelectorGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StyleMapType }{@code >}
     *     {@link JAXBElement }{@code <}{@link StyleType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractStyleSelectorType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractStyleSelectorType> getAbstractStyleSelectorGroup() {
        return abstractStyleSelectorGroup;
    }

    /**
     * Legt den Wert der abstractStyleSelectorGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StyleMapType }{@code >}
     *     {@link JAXBElement }{@code <}{@link StyleType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractStyleSelectorType }{@code >}
     *     
     */
    public void setAbstractStyleSelectorGroup(JAXBElement<? extends AbstractStyleSelectorType> value) {
        this.abstractStyleSelectorGroup = value;
    }

    public boolean isSetAbstractStyleSelectorGroup() {
        return (this.abstractStyleSelectorGroup!= null);
    }

    /**
     * Gets the value of the pairSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pairSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPairSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getPairSimpleExtensionGroup() {
        if (pairSimpleExtensionGroup == null) {
            pairSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.pairSimpleExtensionGroup;
    }

    public boolean isSetPairSimpleExtensionGroup() {
        return ((this.pairSimpleExtensionGroup!= null)&&(!this.pairSimpleExtensionGroup.isEmpty()));
    }

    public void unsetPairSimpleExtensionGroup() {
        this.pairSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the pairObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pairObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPairObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getPairObjectExtensionGroup() {
        if (pairObjectExtensionGroup == null) {
            pairObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.pairObjectExtensionGroup;
    }

    public boolean isSetPairObjectExtensionGroup() {
        return ((this.pairObjectExtensionGroup!= null)&&(!this.pairObjectExtensionGroup.isEmpty()));
    }

    public void unsetPairObjectExtensionGroup() {
        this.pairObjectExtensionGroup = null;
    }

    public void setPairSimpleExtensionGroup(List<Object> value) {
        this.pairSimpleExtensionGroup = value;
    }

    public void setPairObjectExtensionGroup(List<AbstractObjectType> value) {
        this.pairObjectExtensionGroup = value;
    }

}
