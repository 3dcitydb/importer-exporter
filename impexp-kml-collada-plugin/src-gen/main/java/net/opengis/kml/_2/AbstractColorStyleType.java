//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für AbstractColorStyleType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractColorStyleType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractSubStyleType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}color" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}colorMode" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractColorStyleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractColorStyleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractColorStyleType", propOrder = {
    "color",
    "colorMode",
    "abstractColorStyleSimpleExtensionGroup",
    "abstractColorStyleObjectExtensionGroup"
})
@XmlSeeAlso({
    LineStyleType.class,
    LabelStyleType.class,
    PolyStyleType.class,
    IconStyleType.class
})
public abstract class AbstractColorStyleType
    extends AbstractSubStyleType
{

    @XmlElement(type = String.class, defaultValue = "ffffffff")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] color;
    @XmlElement(defaultValue = "normal")
    protected ColorModeEnumType colorMode;
    @XmlElement(name = "AbstractColorStyleSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> abstractColorStyleSimpleExtensionGroup;
    @XmlElement(name = "AbstractColorStyleObjectExtensionGroup")
    protected List<AbstractObjectType> abstractColorStyleObjectExtensionGroup;

    /**
     * Ruft den Wert der color-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getColor() {
        return color;
    }

    /**
     * Legt den Wert der color-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(byte[] value) {
        this.color = value;
    }

    public boolean isSetColor() {
        return (this.color!= null);
    }

    /**
     * Ruft den Wert der colorMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ColorModeEnumType }
     *     
     */
    public ColorModeEnumType getColorMode() {
        return colorMode;
    }

    /**
     * Legt den Wert der colorMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ColorModeEnumType }
     *     
     */
    public void setColorMode(ColorModeEnumType value) {
        this.colorMode = value;
    }

    public boolean isSetColorMode() {
        return (this.colorMode!= null);
    }

    /**
     * Gets the value of the abstractColorStyleSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractColorStyleSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractColorStyleSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractColorStyleSimpleExtensionGroup() {
        if (abstractColorStyleSimpleExtensionGroup == null) {
            abstractColorStyleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.abstractColorStyleSimpleExtensionGroup;
    }

    public boolean isSetAbstractColorStyleSimpleExtensionGroup() {
        return ((this.abstractColorStyleSimpleExtensionGroup!= null)&&(!this.abstractColorStyleSimpleExtensionGroup.isEmpty()));
    }

    public void unsetAbstractColorStyleSimpleExtensionGroup() {
        this.abstractColorStyleSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the abstractColorStyleObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractColorStyleObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractColorStyleObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractColorStyleObjectExtensionGroup() {
        if (abstractColorStyleObjectExtensionGroup == null) {
            abstractColorStyleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractColorStyleObjectExtensionGroup;
    }

    public boolean isSetAbstractColorStyleObjectExtensionGroup() {
        return ((this.abstractColorStyleObjectExtensionGroup!= null)&&(!this.abstractColorStyleObjectExtensionGroup.isEmpty()));
    }

    public void unsetAbstractColorStyleObjectExtensionGroup() {
        this.abstractColorStyleObjectExtensionGroup = null;
    }

    public void setAbstractColorStyleSimpleExtensionGroup(List<Object> value) {
        this.abstractColorStyleSimpleExtensionGroup = value;
    }

    public void setAbstractColorStyleObjectExtensionGroup(List<AbstractObjectType> value) {
        this.abstractColorStyleObjectExtensionGroup = value;
    }

}
