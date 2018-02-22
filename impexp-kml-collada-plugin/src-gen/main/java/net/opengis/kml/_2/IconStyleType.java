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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für IconStyleType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="IconStyleType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractColorStyleType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}scale" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}heading" minOccurs="0"/>
 *         &lt;element name="Icon" type="{http://www.opengis.net/kml/2.2}BasicLinkType" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}hotSpot" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}IconStyleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}IconStyleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IconStyleType", propOrder = {
    "scale",
    "heading",
    "icon",
    "hotSpot",
    "iconStyleSimpleExtensionGroup",
    "iconStyleObjectExtensionGroup"
})
public class IconStyleType
    extends AbstractColorStyleType
{

    @XmlElement(defaultValue = "1.0")
    protected Double scale;
    @XmlElement(defaultValue = "0.0")
    protected Double heading;
    @XmlElement(name = "Icon")
    protected BasicLinkType icon;
    protected Vec2Type hotSpot;
    @XmlElement(name = "IconStyleSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> iconStyleSimpleExtensionGroup;
    @XmlElement(name = "IconStyleObjectExtensionGroup")
    protected List<AbstractObjectType> iconStyleObjectExtensionGroup;

    /**
     * Ruft den Wert der scale-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getScale() {
        return scale;
    }

    /**
     * Legt den Wert der scale-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setScale(Double value) {
        this.scale = value;
    }

    public boolean isSetScale() {
        return (this.scale!= null);
    }

    /**
     * Ruft den Wert der heading-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getHeading() {
        return heading;
    }

    /**
     * Legt den Wert der heading-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setHeading(Double value) {
        this.heading = value;
    }

    public boolean isSetHeading() {
        return (this.heading!= null);
    }

    /**
     * Ruft den Wert der icon-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BasicLinkType }
     *     
     */
    public BasicLinkType getIcon() {
        return icon;
    }

    /**
     * Legt den Wert der icon-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BasicLinkType }
     *     
     */
    public void setIcon(BasicLinkType value) {
        this.icon = value;
    }

    public boolean isSetIcon() {
        return (this.icon!= null);
    }

    /**
     * Ruft den Wert der hotSpot-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Vec2Type }
     *     
     */
    public Vec2Type getHotSpot() {
        return hotSpot;
    }

    /**
     * Legt den Wert der hotSpot-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Vec2Type }
     *     
     */
    public void setHotSpot(Vec2Type value) {
        this.hotSpot = value;
    }

    public boolean isSetHotSpot() {
        return (this.hotSpot!= null);
    }

    /**
     * Gets the value of the iconStyleSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the iconStyleSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIconStyleSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getIconStyleSimpleExtensionGroup() {
        if (iconStyleSimpleExtensionGroup == null) {
            iconStyleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.iconStyleSimpleExtensionGroup;
    }

    public boolean isSetIconStyleSimpleExtensionGroup() {
        return ((this.iconStyleSimpleExtensionGroup!= null)&&(!this.iconStyleSimpleExtensionGroup.isEmpty()));
    }

    public void unsetIconStyleSimpleExtensionGroup() {
        this.iconStyleSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the iconStyleObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the iconStyleObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIconStyleObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getIconStyleObjectExtensionGroup() {
        if (iconStyleObjectExtensionGroup == null) {
            iconStyleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.iconStyleObjectExtensionGroup;
    }

    public boolean isSetIconStyleObjectExtensionGroup() {
        return ((this.iconStyleObjectExtensionGroup!= null)&&(!this.iconStyleObjectExtensionGroup.isEmpty()));
    }

    public void unsetIconStyleObjectExtensionGroup() {
        this.iconStyleObjectExtensionGroup = null;
    }

    public void setIconStyleSimpleExtensionGroup(List<Object> value) {
        this.iconStyleSimpleExtensionGroup = value;
    }

    public void setIconStyleObjectExtensionGroup(List<AbstractObjectType> value) {
        this.iconStyleObjectExtensionGroup = value;
    }

}
