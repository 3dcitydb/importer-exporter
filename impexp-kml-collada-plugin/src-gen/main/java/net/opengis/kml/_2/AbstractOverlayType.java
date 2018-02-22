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
 * <p>Java-Klasse für AbstractOverlayType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractOverlayType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractFeatureType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}color" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}drawOrder" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Icon" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractOverlaySimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractOverlayObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractOverlayType", propOrder = {
    "color",
    "drawOrder",
    "icon",
    "abstractOverlaySimpleExtensionGroup",
    "abstractOverlayObjectExtensionGroup"
})
@XmlSeeAlso({
    ScreenOverlayType.class,
    GroundOverlayType.class,
    PhotoOverlayType.class
})
public abstract class AbstractOverlayType
    extends AbstractFeatureType
{

    @XmlElement(type = String.class, defaultValue = "ffffffff")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] color;
    @XmlElement(defaultValue = "0")
    protected Integer drawOrder;
    @XmlElement(name = "Icon")
    protected LinkType icon;
    @XmlElement(name = "AbstractOverlaySimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> abstractOverlaySimpleExtensionGroup;
    @XmlElement(name = "AbstractOverlayObjectExtensionGroup")
    protected List<AbstractObjectType> abstractOverlayObjectExtensionGroup;

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
     * Ruft den Wert der drawOrder-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDrawOrder() {
        return drawOrder;
    }

    /**
     * Legt den Wert der drawOrder-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDrawOrder(Integer value) {
        this.drawOrder = value;
    }

    public boolean isSetDrawOrder() {
        return (this.drawOrder!= null);
    }

    /**
     * Ruft den Wert der icon-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LinkType }
     *     
     */
    public LinkType getIcon() {
        return icon;
    }

    /**
     * Legt den Wert der icon-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkType }
     *     
     */
    public void setIcon(LinkType value) {
        this.icon = value;
    }

    public boolean isSetIcon() {
        return (this.icon!= null);
    }

    /**
     * Gets the value of the abstractOverlaySimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractOverlaySimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractOverlaySimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractOverlaySimpleExtensionGroup() {
        if (abstractOverlaySimpleExtensionGroup == null) {
            abstractOverlaySimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.abstractOverlaySimpleExtensionGroup;
    }

    public boolean isSetAbstractOverlaySimpleExtensionGroup() {
        return ((this.abstractOverlaySimpleExtensionGroup!= null)&&(!this.abstractOverlaySimpleExtensionGroup.isEmpty()));
    }

    public void unsetAbstractOverlaySimpleExtensionGroup() {
        this.abstractOverlaySimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the abstractOverlayObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractOverlayObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractOverlayObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractOverlayObjectExtensionGroup() {
        if (abstractOverlayObjectExtensionGroup == null) {
            abstractOverlayObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractOverlayObjectExtensionGroup;
    }

    public boolean isSetAbstractOverlayObjectExtensionGroup() {
        return ((this.abstractOverlayObjectExtensionGroup!= null)&&(!this.abstractOverlayObjectExtensionGroup.isEmpty()));
    }

    public void unsetAbstractOverlayObjectExtensionGroup() {
        this.abstractOverlayObjectExtensionGroup = null;
    }

    public void setAbstractOverlaySimpleExtensionGroup(List<Object> value) {
        this.abstractOverlaySimpleExtensionGroup = value;
    }

    public void setAbstractOverlayObjectExtensionGroup(List<AbstractObjectType> value) {
        this.abstractOverlayObjectExtensionGroup = value;
    }

}
